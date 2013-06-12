/**
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2009-2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute
 * this file in accordance with the terms of the MIT license,
 * a copy of which can be found in the LICENSE.txt file or at
 * http://opensource.org/licenses/MIT.
 */
package runtime.tran;

import runtime.ConfigUtils;
import runtime.Logging;
import runtime.rep.Tuple;
import runtime.rep.lambda.Lambda;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-thread transaction class. Works in conjunction with
 * {@link TransactionManager} and {@link Box} classes.
 * Currently, serializable consistency is guaranteed, at
 * the cost of foregoing lightweight reads in read-only
 * transactions (except when they take the form of naked
 * calls to {@link runtime.intrinsic.tran._get} or
 * {@link runtime.intrinsic.tran._gets}).
 * TODO static analysis to mark read-only transactions.
 *
 * NOTE: the use of STM over ML-style managed containers,
 * with persistent data structures providing practical copy-
 * on-write semantics, is inspired by Rich Hickey's prioneering
 * work in Clojure.
 *
 * @author Basil Hosmer
 */
final class Transaction
{
    private static final AtomicLong TICK = new AtomicLong();

    /**
     * Generate tick values for transaction events in this VM.
     */
    private static long getTick()
    {
        return TICK.incrementAndGet();
    }

    /**
     * Max number of retry attempts before a top-level error is raised.
     */
    private static final int MAX_ATTEMPTS =
        ConfigUtils.parseIntProp(Transaction.class.getName() + ".MAX_ATTEMPTS", 10000);

    /**
     * Time to wait to acquire a box's write lock.
     */
    private static final int OWNERSHIP_WRITELOCK_TIMEOUT_MILLIS =
        ConfigUtils.parseIntProp(
            Transaction.class.getName() + ".OWNERSHIP_WRITELOCK_TIMEOUT_MILLIS", 10);

    /**
     * Backoff interval for retry pauses.
     */
    private static final int RETRY_PAUSE_MILLIS =
        ConfigUtils.parseIntProp(Transaction.class.getName() + ".RETRY_PAUSE_MILLIS", 10);

    /**
     * Chunk size for growing updated arrays
     */
    private static final int UPDATED_CHUNK_SIZE =
        ConfigUtils.parseIntProp(Transaction.class.getName() + ".UPDATED_CHUNK_SIZE", 8);

    /**
     * Chunk size for growing pinned array
     */
    private static final int PINNED_CHUNK_SIZE =
        ConfigUtils.parseIntProp(Transaction.class.getName() + ".PINNED_CHUNK_SIZE", 8);


    //
    // instance
    //

    /**
     * Diagnostics only, set when transaction object is created.
     */
    final long threadId = Thread.currentThread().getId();

    /**
     * Transaction start tick, used for arbitration based on transaction age.
     * Package local, used by {@link Box#queueOwner}.
     */
    long tranStart = 0;

    /**
     * Attempt start tick, determines the world state within an attempt.
     */
    private long attemptStart = 0;

    /**
     * Holds the state of our current attempt, or null if we're not running.
     * Note: non-private so that {@link Box} constructor can access.
     */
    Attempt attempt = null;

    /**
     * Arrays of in-transaction box updates.
     * Note that here we're designing for most transactions
     * to update a small number of boxes. This seems true so
     * far, emprically, but we do have a warning in place.
     * See {@link #addUpdate(Box, Object)}.
     */
    private int updatedCount = 0;
    private Box[] updated = new Box[UPDATED_CHUNK_SIZE];
    private Object[] updates = new Object[UPDATED_CHUNK_SIZE];

    /**
     * Array of in-transaction pinned boxes.
     * Note that here we're designing for most transactions
     * to pin a small number of boxes. This seems true so
     * far, emprically, but we do have a warning in place.
     * See {@link #addPinned(Box)}.
     */
    private int pinnedCount = 0;
    private Box[] pinned = new Box[PINNED_CHUNK_SIZE];

    /**
     * used to provide multiwatcher/waiter functions access
     * to current commit set, via {@link #getUpdatedStash()}
     */
    private Box[] updatedStash = null;

    //
    // TransactionManager API
    //

    /**
     * Note: not thread-safe, called only in current thread
     * from {@link TransactionManager}
     */
    boolean isRunning()
    {
        return attempt != null;
    }

    /**
     * Create a transaction, run f, commit the transaction and return the result.
     * Called from {@link TransactionManager#apply}
     */
    Object runApply(final Lambda f)
    {
        return run(null, f, null);
    }

    /**
     * Create a transaction, put v into b, commit the transaction and return b.
     * Called from {@link TransactionManager#apply}
     */
    Object runPut(final Box b, final Object v)
    {
        return run(b, null, v);
    }

    /**
     * Create a transaction, run f, put its result into b, commit the transaction
     * and return b. Called from {@link TransactionManager#apply}
     */
    Object runUpdate(final Box b, final Lambda f)
    {
        return run(b, f, null);
    }

    /**
     * Main work routine. Performs different top-level transactional
     * operation based on inputs:
     * <ul>
     * <li/>if box is null, then f is run and its value returned
     * <li/>if box and f are non-null, then box is updated with the
     * result of running f
     * <li/>if box is non-null and f is null, then val is put to box
     * </ul>
     */
    private Object run(final Box box, final Lambda f, final Object val)
    {
        // events to be fired after a successful commit.
        ChangeEvent event = null;

        // holds the result of the transactional operation, per above
        Object result = null;

        // non-null when retry is due to contention with a blocking attempt
        Attempt blocker = null;

        // true if we're queued to retry after blocker, acquisitions intact
        boolean queued = false;

        // true until we've successfully committed
        boolean retry = true;

        // transaction start tick is picked up on first attempt
        tranStart = 0;

        // attempt loop
        for (int tries = 0; retry && tries < MAX_ATTEMPTS; ++tries)
        {
            if (tries >= 100 && tries % 100 == 0)
                Logging.warning("retry count {0}", tries);

            if (blocker != null)
            {
                // we're blocked by a specific transaction, wait for it to finish
                blocker.await();

                if (queued)
                {
                    // if queued, we've retained acquisitions and can retry immediately
                    queued = false;

                    // unless another (even older) attempt has bumped us
                    if (attempt.isBumped())
                    {
                        // if so, release acquisitions and pause
                        if (Logging.isDebug())
                            Logging.debug("bumped while awaiting blocker");

                        endAttempt();
                        pause(tries);
                    }
                }
                else
                {
                    // if we weren't queued, we may be one of many transactions
                    // awaiting this blocker. pause before retry
                    pause(tries);
                }

                // clear for next attempt
                blocker = null;
            }
            else if (tries > 0)
            {
                // we may have queued even without a blocking transaction
                if (queued)
                {
                    // if queued, we've retained acquisitions and can retry immediately
                    queued = false;

                    // unless another (even older) attempt has bumped us
                    if (attempt.isBumped())
                    {
                        // if so, release acquisitions and pause
                        if (Logging.isDebug())
                            Logging.debug("bumped while pausing");

                        endAttempt();
                        pause(tries);
                    }
                }
                else
                {
                    // no blocker, pause before retry
                    pause(tries);
                }
            }

            // establish our start tick, ensure attempt object (may have been retained)
            beginAttempt();

            try
            {
                // main action depends on combination of arguments (box, f, val)
                result =
                    box == null ? f.apply(Tuple.UNIT) :
                        f != null ? update(box, f) :
                            put(box, val);

                // commit box updates and collect change events
                event = commit();

                // all done, proceed to finally for cleanup
                retry = false;
            }
            catch (Retry ex)
            {
                // retain reference to attempt that blocked us, if any
                blocker = ex.blocker;

                // if queued, we keep our acquisitions for next attempt
                queued = ex.queued;

                if (Logging.isDebug())
                    Logging.debug("retry on attempt {0}, {1}", tries,
                        queued ? "queued" : "not queued");
            }
            finally
            {
                // release read locks on pinned boxes
                releasePinned();

                // clear update map 
                clearUpdates();

                // if we were bumped as a queued owner between our throw and now,
                // clear attempt state
                if (attempt.isBumped())
                {
                    if (Logging.isDebug())
                        Logging.debug("bumped before await");

                    queued = false;
                }

                // if we're not queued as the next owner of a blocking attempt,
                // clear attempt state (releases acquisitions)
                if (!queued)
                    endAttempt();
            }
        }

        // if we got here without succeeding, we're in bad shape
        if (retry)
            throw new RuntimeException(
                "transaction fails after max attempts: " +
                    MAX_ATTEMPTS);

        // fire accumulated change events
        if (event != null)
            event.fire();

        // return user function's result
        return result;
    }

    /**
     * stashes a copy of the current {@link #updated} box array
     * to {@link #updatedStash}.
     */
    private void stashUpdated()
    {
        updatedStash = Arrays.copyOf(updated, updated.length);
    }

    /**
     * clear {@link #updatedStash}
     */
    private void unstashUpdated()
    {
        updatedStash = null;
    }

    /**
     * package local: provides {@link #updatedStash} access to
     * multiwatcher/waiter functions
     */
    Box[] getUpdatedStash()
    {
        return updatedStash;
    }

    /**
     * Initiate a new attempt. This always sets the attempt
     * start tick, and sometimes creates a new attempt object. (We
     * keep the old attempt object if we're retaining ownership of
     * its resources on the next try, rather than reacquiring them.)
     * On our first attempt we also set the start tick for the entire
     * transaction, used to track transaction age for arbitration.
     */
    private void beginAttempt()
    {
        // whether or not we create a new object, attempt starts now
        attemptStart = getTick();

        // create a new Attempt object with initial state, if needed
        if (attempt == null)
            attempt = new Attempt(this);
        else
            assert !attempt.isDone();

        // first time through, capture transaction start tick
        if (tranStart == 0)
            tranStart = attemptStart;
    }

    /**
     * Set attempt object's state to done and clear our
     * reference to it. Ending the attempt has the side effect of
     * "freeing" any boxes whose owner property points to this
     * attempt object, since {@link runtime.tran.Attempt#isDone}
     * will now return true.
     */
    private void endAttempt()
    {
        attempt.end();
        attempt = null;
    }

    /**
     * Commit updates to boxes and collect change events.
     * Return collected event list or null if none.
     * Note: updates to a box's watcher list are not transactional,
     * so we are not guaranteed to run exactly the watchers present
     * at commit time.
     */
    private ChangeEvent commit()
    {
        final int n = updatedCount;

        if (n == 0)
            return null;

        for (int i = 0; i < n; i++)
            updated[i].acquireWriteLock();

        final long commitTick = getTick();

        boolean needsChangeEvent = false;

        for (int i = 0; i < n; i++)
        {
            final Box box = updated[i];

            final Object newValue = updates[i];
            assert newValue != null;

            box.commit(newValue, commitTick);

            needsChangeEvent |= box.getWatchers() != null;
        }

        for (int j = n - 1; j >= 0; j--)
            updated[j].releaseWriteLock();

        if (needsChangeEvent)
            return new ChangeEvent(updated, updates, updatedCount);
        else
            return null;
    }

    /**
     * Release read locks on all pinned boxes,
     * and clear the transaction's pinned set.
     */
    private void releasePinned()
    {
        for (int i = 0; i < pinnedCount; i++)
        {
            final Box b = pinned[i];

            // NOTE: in-tran release of pinned box leaves a hole
            if (b != null)
                b.releaseReadLock();
        }

        clearPinned();
    }

    /**
     * Pause for a jittered multiple of configured retry pause.
     */
    private static void pause(final int tries)
    {
        try
        {
            final int jitter = (int)Math.floor(Math.random() * RETRY_PAUSE_MILLIS);
            final int pause = tries * (RETRY_PAUSE_MILLIS / 2 + jitter);

            Thread.sleep(pause);
        }
        catch (InterruptedException ignored)
        {
        }
    }

    /**
     * Find a given box in the array of in-transaction updates.
     * Return array index, or current count if not found.
     */
    private int findUpdated(final Box box)
    {
        final int n = updatedCount;

        for (int i = 0; i < n; i++)
            if (updated[i] == box)
                return i;

        return n;
    }

    /**
     * Return true if given box has been updated in this transaction.
     */
    private boolean isUpdated(final Box box)
    {
        return findUpdated(box) < updatedCount;
    }

    /**
     * Add a box and value to the in-transaction updates arrays.
     * If box has a prior in-transaction value, it is overwritten.
     * Otherwise the box/value pair is added to the array pair.
     * Arrays are grown if at capacity.
     */
    private void addUpdate(final Box box, final Object val)
    {
        final int i = findUpdated(box);

        if (i < updatedCount)
        {
            updates[i] = val;
            return;
        }

        if (updatedCount == updated.length)
        {
            final Box[] newUpdated = new Box[updatedCount + UPDATED_CHUNK_SIZE];
            System.arraycopy(updated, 0, newUpdated, 0, updatedCount);
            updated = newUpdated;

            final Object[] newUpdates = new Object[updatedCount + UPDATED_CHUNK_SIZE];
            System.arraycopy(updates, 0, newUpdates, 0, updatedCount);
            updates = newUpdates;
        }

        updated[updatedCount] = box;
        updates[updatedCount] = val;
        updatedCount++;
    }

    /**
     * Clear in-transaction update arrays
     */
    private void clearUpdates()
    {
        if (updatedCount > 0)
        {
            updatedCount = 0;
            Arrays.fill(updates, null);
            Arrays.fill(updated, null);
        }
    }

    /**
     * Return true if given box has been pinned in this transaction.
     */
    private boolean isPinned(final Box box)
    {
        final int n = pinnedCount;

        for (int i = 0; i < n; i++)
            if (pinned[i] == box)
                return true;

        return false;
    }

    /**
     * Add given box to pinned array.
     * CAUTION: does not check whether box has already been pinned.
     * Caller must guard with call to {@link #isPinned}.
     */
    private void addPinned(final Box box)
    {
        if (pinnedCount == pinned.length)
        {
            final Box[] newPinned = new Box[pinnedCount + PINNED_CHUNK_SIZE];
            System.arraycopy(pinned, 0, newPinned, 0, pinnedCount);
            pinned = newPinned;
        }

        pinned[pinnedCount++] = box;
    }

    /**
     * Remove given box from pinned array and return true, if box
     * was pinned. Otherwise, return false. Note that removal leaves
     * a gap, which must be checked for when iterating over this
     * array.
     */
    private boolean removePinned(final Box box)
    {
        final int n = pinnedCount;

        for (int i = 0; i < n; i++)
        {
            if (pinned[i] == box)
            {
                pinned[i] = null;
                return true;
            }
        }

        return false;
    }

    /**
     * Clear array of in-transaction pinned boxes
     */
    private void clearPinned()
    {
        Arrays.fill(pinned, null);
        pinnedCount = 0;
    }

    //
    // API
    //

    /**
     * Get box's value as of the start of our attempt, if available.
     * Note that we don't enforce serializability here, in e.g.
     * {@link runtime.tran.Box#getValue()}.
     */
    Object get(final Box box)
    {
        // return in-transaction value, if we have updated the box
        final int i = findUpdated(box);
        if (i < updatedCount)
            return updates[i];

        // otherwise return value as of transaction start, if available
        final Object value = box.getValueAt(attemptStart);
        if (value != null)
            return value;

        // no value available that far back, must retry
        if (Logging.isDebug())
            Logging.debug("value as of transaction start not available, retrying ({0})",
                box.toString());

        throw TransactionManager.GENERIC_RETRY;
    }

    /**
     * get a tuple of values from a tuple of boxes
     */
    Tuple gets(final Tuple boxes)
    {
        final int w = boxes.size();

        final Object[] vals = new Object[w];

        for (int i = 0; i < w; i++)
            vals[i] = get((Box)boxes.get(i));

        return Tuple.from(vals);
    }

    /**
     * Pin a box, i.e. perform a serializable in-transaction read.
     * The box must not have been updated by another transaction
     * since our transaction began. And the box must not be owned
     * (updated or reserved for updating) by an active transaction.
     * In either case we retry. In the latter case, we will await
     * the other transaction's completion before retrying. But
     * unlike failed ownership, we don't queue ourselves or
     * retain our other resources on failure to pin.
     */
    void pin(final Box box)
    {
        if (isPinned(box) || isUpdated(box))
            return;

        box.acquireReadLock();

        // success path will leave the read lock in place
        boolean unlock = true;

        // with box read-locked, check box commit tick and try to take ownership
        try
        {
            // if box has been updated since our attempt started, we must retry

            if (box.committedSince(attemptStart))
            {
                if (Logging.isDebug())
                    Logging.debug(
                        "can''t pin because {0} has been updated since attempt start",
                        box.toString());

                // if we can queue for ownership, retain our resources for next try
                // note that in this case we go from pinning to owning on next try.
                if (box.queueOwner(attempt))
                    throw new Retry(null, true);
                else
                    throw TransactionManager.GENERIC_RETRY;
            }

            // now we check for ownership. if box is owned, we must retry

            final Attempt owner = box.getOwner();

            if (owner == null)
            {
                // keep box read lock, add to pinned set
                addPinned(box);
                unlock = false;
            }
            else if (owner != attempt)
            {
                // box is owned by another transaction
                final Transaction ownerTran = owner.tran;
                final long agediff = ownerTran.tranStart - tranStart;

                if (Logging.isDebug())
                    Logging.debug(
                        "can''t pin {0}, owned by {1} age diff = {2} (them:{3} - us:{4})",
                        box.toString(), ownerTran.threadId, agediff,
                        ownerTran.tranStart, tranStart);

                // if we're older, attempt to queue for ownership.
                // note that in this case we go from pinning to owning.
                final boolean queued = agediff > 0 && box.queueOwner(attempt);

                // first argument causes us to await current owner,
                // second if true lets us retain owned resources for retry
                throw new Retry(owner, queued);
            }
        }
        finally
        {
            if (unlock)
                box.releaseReadLock();
        }
    }

    /**
     * Attempt to take ownership of a box. If the box is owned or
     * pinned by another transaction, our ownership attempt fails
     * and we retry.
     *
     * If the failure is due to the box being owned by another
     * transaction, our retry will await the completion of the
     * current owner. And if we're older than the current owner,
     * we will attempt to queue ourselves for immediate promotion
     * to owner once the current owner has completed. This succeeds
     * as long as no older transaction is already queued. If queued,
     * the main transaction loop will reuse our current attempt object
     * for the retry, meaning we retain ownership of any boxes already
     * acquired prior to this failure.
     *
     * Successful acquisition of ownership will leave our attempt
     * object attached to the owned box.
     */
    void own(final Box box)
    {
        if (isUpdated(box))
            return;

        // must release our own read lock if we'd pinned the box previously
        if (removePinned(box))
            box.releaseReadLock();

        // Note: in this region we're vulnerable to a pinned box getting swiped.
        // pin's logical guarantee against write skew is preserved, since if a
        // swipe occurs, our attempt to take ownership will fail and we'll retry.

        if (!box.acquireWriteLock(OWNERSHIP_WRITELOCK_TIMEOUT_MILLIS))
        {
            if (Logging.isDebug())
                Logging.debug(
                    "can''t take ownership because couldn''t acquire {0} write lock",
                    box.toString());

            throw TransactionManager.GENERIC_RETRY;
        }

        // with box write-locked, check box commit tick and try to take ownership
        try
        {
            // if box has been updated since our attempt started, we must retry

            if (box.committedSince(attemptStart))
            {
                if (Logging.isDebug())
                    Logging.debug(
                        "can''t take ownership because {0} has been updated since attempt start",
                        box.toString());

                // if we can queue for ownership, retain our resources for next try
                if (box.queueOwner(attempt))
                    throw new Retry(null, true);
                else
                    throw TransactionManager.GENERIC_RETRY;
            }

            // now we attempt to gain ownership

            final Attempt owner = box.checkOwner();

            if (owner == null)
            {
                box.setOwner(attempt);
            }
            else if (owner != attempt)
            {
                final Transaction ownerTran = owner.tran;
                final long agediff = ownerTran.tranStart - tranStart;

                if (Logging.isDebug())
                    Logging.debug(
                        "can''t take ownership of {0}, already owned by {1}, agediff = {2} (them:{3} - us:{4})",
                        box.toString(), ownerTran.threadId, agediff,
                        ownerTran.tranStart, tranStart);

                // if we're older, attempt to queue for ownership.
                final boolean queued = agediff > 0 && box.queueOwner(attempt);

                // first argument causes us to await current owner,
                // second if true lets us retain owned resources for retry
                throw new Retry(owner, queued);
            }
        }
        finally
        {
            box.releaseWriteLock();
        }
    }

    /**
     * Set the in-transaction value of a box to a new value.
     */
    Object put(final Box box, final Object val)
    {
        own(box);
        addUpdate(box, val);

        return val;
    }

    /**
     * Tuplized version of {@link #put}
     */
    Tuple puts(final Tuple boxes, final Tuple vals)
    {
        final int size = boxes.size();
        for (int i = 0; i < size; i++)
            put((Box)boxes.get(i), vals.get(i));

        return boxes;
    }

    /**
     * Set the in-transaction value of a box to the result of applying
     * an update function to the current value.
     */
    Object update(final Box box, final Lambda f)
    {
        own(box);

        final Object newval = f.apply(get(box));
        addUpdate(box, newval);

        return newval;
    }

    /**
     * Tuplized version of {@link #update}--a tuple of boxes is updated via
     * a function that takes and returns a tuple of values.
     */
    Tuple updates(final Tuple boxes, final Lambda f)
    {
        final int size = boxes.size();
        final Object[] vals = new Object[size];

        for (int i = 0; i < size; i++)
        {
            final Box box = (Box)boxes.get(i);
            own(box);
            vals[i] = get(box);
        }

        // run function
        final Tuple newvals = (Tuple)f.apply(Tuple.from(vals));

        for (int i = 0; i < size; i++)
            addUpdate((Box)boxes.get(i), newvals.get(i));

        return newvals;
    }

    /**
     * general case of update: set the in-transaction value of
     * a write box to the result of applying a function to the
     * current value of a read box.
     */
    Object transfer(final Box write, final Lambda f, final Box read)
    {
        own(write);
        pin(read);

        final Object val = f.apply(get(read));

        addUpdate(write, val);

        return val;
    }

    /**
     * Tuplized version of {@link #transfer}--a tuple of boxes is updated
     * with the result of a function that takes a tuple of values.
     */
    Tuple transfers(final Tuple writes, final Lambda f, final Tuple reads)
    {
        final int nr = reads.size();
        final int nw = writes.size();

        // take ownership of output boxes. may fail
        // Note: own first and pin second, since own subsumes pin
        for (int i = 0; i < nw; i++)
            own((Box)writes.get(i));

        final Object[] inputs = new Object[nr];

        // pin and read input boxes. may fail
        for (int i = 0; i < nr; i++)
        {
            final Box box = (Box)reads.get(i);
            pin(box);
            inputs[i] = get(box);
        }

        // run function. may fail if further ownership is attempted
        final Tuple newvals = (Tuple)f.apply(Tuple.from(inputs));

        // scatter function outputs to boxes
        for (int i = 0; i < nw; i++)
            addUpdate((Box)writes.get(i), newvals.get(i));

        return writes;
    }
}
