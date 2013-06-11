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

import runtime.sys.ConfigUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The state of a transaction attempt, where a transaction
 * makes >= 1 attempts, with (only) the last one ending in
 * a commit.
 *
 * The transaction state isn't just this object's member
 * variables--boxes are tagged with Attempt objects to
 * designate ownership.
 *
 * Attempt objects may or may not be held over from one
 * transaction attempt to the next, depending on whether
 * or not owned boxes are retained.
 *
 * @author Basil Hosmer
 */
final class Attempt
{
    /**
     * Timeout for awaiting countdown.
     */
    private static final int COUNTDOWN_WAIT_MILLIS =
        ConfigUtils.parseIntProp(Attempt.class.getName() + ".COUNTDOWN_WAIT_MILLIS", 1000);

    /**
     * Attempting transaction.
     */
    final Transaction tran;

    /**
     * Count is 1 while attempt is active, and counted down on
     * termination. Contending transactions can call {@link #await}
     * to await the countdown. Note that an attempt may remain
     * active over several retries, see {@link Transaction#run},
     * {@link runtime.tran.Transaction#endAttempt()}.
     */
    final CountDownLatch state;

    /**
     * Boxes use this to signal us that we've been bumped out of
     * line for upcoming box ownership.
     */
    AtomicBoolean bumped = new AtomicBoolean();

    /**
     * Attempt begins in an active state.
     */
    Attempt(final Transaction tran)
    {
        this.tran = tran;
        this.state = new CountDownLatch(1);
    }

    /**
     * True if attempt has completed (successfully or not)
     */
    boolean isDone()
    {
        return state.getCount() == 0;
    }

    /**
     * End the current attempt. Called by parent transaction only.
     */
    void end()
    {
        state.countDown();
    }

    /**
     * Wait for {@link #state} to count down. Called by transactions
     * who are waiting for us to complete, to get to boxes we own.
     */
    void await()
    {
        try
        {
            state.await(COUNTDOWN_WAIT_MILLIS, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException ignored)
        {
        }
    }

    /**
     * called by a box to tell us that we are no longer a queued owner.
     */
    void bump()
    {
        bumped.set(true);
    }

    /**
     * true if we were bumped via {@link #bump()}
     */
    boolean isBumped()
    {
        return bumped.get();
    }
}