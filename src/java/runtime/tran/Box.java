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

import runtime.Logging;
import runtime.rep.lambda.Lambda;
import runtime.rep.map.PersistentMap;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Runtime representation class for values of type Box(T).
 * Transactional single-value store. Works in conjunction
 * with {@link Transaction}.
 *
 * @author Basil Hosmer
 */
public final class Box
{
    /**
     * Current committed value.
     */
    private Object currentValue;

    /**
     * Commit tick for the current value.
     */
    private long commitTick;

    /**
     * Set to current attempt when we're owned by a transaction.
     */
    private Attempt owner;

    /**
     * Set when a transaction wants to put itself "next in line"
     * for ownership. See {@link #queueOwner(Attempt)},
     * {@link Transaction#own(Box)}.
     */
    private Attempt queuedOwner;

    /**
     * Used internally during value lookup, and by {@link Transaction}
     * via {@link #acquireReadLock()} and {@link #acquireWriteLock}.
     */
    private final ReentrantReadWriteLock lock;

    /**
     * Set of watcher functions to be notified on value changes.
     * {@link #addWatcher} and {@link #removeWatcher} will be called
     * concurrently by e.g. {@link runtime.intrinsic.tran.Watch},
     * {@link Waiter#await}
     */
    private AtomicReference<PersistentMap> watchers;

    /**
     * Note that Boxes are never without a current value.
     */
    public Box(final Object initialValue)
    {
        this.currentValue = initialValue;
        this.commitTick = 0;
        this.owner = null;
        this.queuedOwner = null;
        this.lock = new ReentrantReadWriteLock();
        this.watchers = null;

        // behave as if owned by a transaction that created us.
        final Transaction tran = TransactionManager.getTransaction();
        if (tran != null)
            this.owner = tran.attempt;

    }

    /**
     * Return our current value. If a transaction is running in this
     * thread, we delegate to it, in case an update for us is pending.
     * Note: we currently have no way of telling the difference between
     * read-only and read-write transactions, so we pin ourselves under
     * any running transaction. This is conservative in the sense that
     * it may pin boxes needlessly. We can't rule out false positives
     * entirely, but we can do much better with a little static analysis.
     * TODO per above
     */
    public Object getValue()
    {
        final Transaction tran = TransactionManager.getTransaction();

        // if tran is running, ensure that we're pinned
        // then get our current value
        if (tran != null)
        {
            // see comment
            tran.pin(this);
            return tran.get(this);
        }

        // prevent a new value from being committed while we read
        acquireReadLock();
        try
        {
            return currentValue;
        }
        finally
        {
            releaseReadLock();
        }
    }

    /**
     * Return our current value, without pinning.
     */
    public Object snapValue()
    {
        final Transaction tran = TransactionManager.getTransaction();

        // if tran is running, get our current value
        if (tran != null)
            return tran.get(this);

        // prevent a new value from being committed while we read
        acquireReadLock();
        try
        {
            return currentValue;
        }
        finally
        {
            releaseReadLock();
        }
    }

    /**
     * Return current committed value.
     * Caller must have read or write lock.
     */
    Object getCurrentValue()
    {
        return currentValue;
    }

    /**
     * Return current commit tick.
     * Caller must have read or write lock.
     */
    long getCommitTick()
    {
        return commitTick;
    }

    /**
     * Return true if current value has been committed since the given tick.
     * Caller must have read or write lock.
     */
    boolean committedSince(final long tick)
    {
        return commitTick > tick;
    }

    /**
     * Return our value as of a given tick, if we have it.
     */
    Object getValueAt(final long tick)
    {
        acquireReadLock();

        try
        {
            return this.commitTick <= tick ? currentValue : null;
        }
        finally
        {
            releaseReadLock();
        }
    }

    /**
     * Commit a new value, as of a given tick.
     * May create, save to, and/or grow history list.
     * Caller must have write lock.
     */
    void commit(final Object value, final long tick)
    {
        currentValue = value;
        commitTick = tick;
    }

    /**
     * Returns current set of watchers, or null if there are none.
     * Caler must have read or write lock.
     */
    Set<Object> getWatchers()
    {
        if (watchers == null)
            return null;

        final PersistentMap currentWatchers = watchers.get();
        return currentWatchers.isEmpty() ? null : currentWatchers.keySet();
    }

    /**
     * Add a watcher function.
     * Caller must have write lock.
     */
    public void addWatcher(final Lambda watcher)
    {
        if (watchers == null)
            watchers = new AtomicReference<PersistentMap>(PersistentMap.EMPTY);

        watchers.set(watchers.get().assoc(watcher, null));
    }

    /**
     * Remove a watcher function.
     * Caller must have write lock.
     */
    public void removeWatcher(final Lambda watcher)
    {
        if (watchers != null)
            watchers.set(watchers.get().unassoc(watcher));
    }

    /**
     * Get current owner or queued owner.
     * Caller must have read or write lock.
     */
    Attempt getOwner()
    {
        return owner != null && !owner.isDone() ? owner :
            queuedOwner != null && !queuedOwner.isDone() ? queuedOwner :
                null;
    }

    /**
     * Get current owner or promote queued owner. Since the promotion
     * is an effectful operation, the caller must have the box write lock.
     * If non-null, the owner is the attempt object of the current or
     * most recent owning transaction. "Or most recent" because references
     * to completed attempts are not cleared--if {@link Attempt#isDone()}
     * returns true on this attempt, then we are no longer owned.
     */
    Attempt checkOwner()
    {
        if (owner != null && !owner.isDone())
            return owner;

        if (queuedOwner != null)
        {
            if (!queuedOwner.isDone())
                owner = queuedOwner;

            queuedOwner = null;
            return owner;
        }

        return null;
    }

    /**
     * Set owner to the specified transaction attempt.
     * Caller must have write lock, and must have called
     * {@link #checkOwner} and received a null result
     * before calling. See e.g. {@link Transaction#own}.
     */
    void setOwner(final Attempt attempt)
    {
        assert owner == null || owner.isDone();
        assert queuedOwner == null;

        this.owner = attempt;
    }

    /**
     * Queue attempt to take ownership once current owner has completed.
     * If an attempt is already queued, the older of the two attempts is
     * chosen. This avoids livelock.
     */
    boolean queueOwner(final Attempt attempt)
    {
        if (queuedOwner == null || queuedOwner.isDone())
        {
            if (Logging.isDebug())
                Logging.debug("box {0} queuing owner {1}", this, attempt.tran.threadId);

            queuedOwner = attempt;
            return true;
        }

        if (attempt.tran.tranStart < queuedOwner.tran.tranStart)
        {
            if (Logging.isDebug())
                Logging.debug("box {0} queuing owner {1}, bumping {2}",
                    this, attempt.tran.threadId, queuedOwner.tran.threadId);

            queuedOwner.bump();
            queuedOwner = attempt;
            return true;
        }

        if (Logging.isDebug())
            Logging.debug(
                "box {0} refusing queued owner {1}, current queued owner {2} is older",
                this, attempt.tran.threadId, queuedOwner.tran.threadId);

        return false;
    }

    /**
     * Get box read lock.
     */
    void acquireReadLock()
    {
        lock.readLock().lock();
    }

    /**
     * Release box read lock.
     */
    void releaseReadLock()
    {
        lock.readLock().unlock();
    }

    /**
     * Acquire box write lock, waiting given number of milliseconds before giving up.
     * Traps {@link InterruptedException}. Returns success.
     */
    boolean acquireWriteLock(final int waitMillis)
    {
        try
        {
            final boolean success =
                lock.writeLock().tryLock(waitMillis, TimeUnit.MILLISECONDS);

            if (!success)
            {
                if (Logging.isDebug())
                    Logging.debug("timeout getting write lock" +
                        ", is write locked = " + lock.isWriteLocked() +
                        ", read lock count = " + lock.getReadLockCount());
            }

            return success;
        }
        catch (InterruptedException e)
        {
            return false;
        }
    }

    /**
     * Acquire box write lock, no time limit.
     */
    void acquireWriteLock()
    {
        lock.writeLock().lock();
    }

    /**
     * Release write lock.
     */
    void releaseWriteLock()
    {
        lock.writeLock().unlock();
    }
}
