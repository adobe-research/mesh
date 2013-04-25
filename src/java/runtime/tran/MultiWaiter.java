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
import runtime.rep.Tuple;
import runtime.rep.lambda.Lambda;

/**
 * Used by {@link runtime.intrinsic.tran.Await}. For a given box,
 * our method {@link #wait(runtime.rep.lambda.Lambda)} implements
 * wait(box, pred) by by attaching ourselves as a watcher to the box,
 * then doing a thread wait until pred(newval) returns true on box commit.
 * (We remove ourselves from the box's watcher list before returning.)
 *
 * @author Basil Hosmer
 */
public final class MultiWaiter implements Lambda
{
    private final Tuple boxes;

    public MultiWaiter(final Tuple boxes)
    {
        this.boxes = boxes;
    }

    /**
     * For our box and a given pred, implement wait(box, pred) by attaching
     * ourselves as a watcher to our box, then doing a thread wait until
     * pred(newval) returns true.
     */
    public void wait(final Lambda pred)
    {
        // hold box read locks while we install ourselves
        for (int i = 0; i < boxes.size(); i++)
            ((Box)boxes.get(i)).acquireReadLock();

        boolean readLocked = true;

        try
        {
            // if pred passes current value, release read lock and
            // don't wait the thread. otherwise, set up and add a
            // box watcher lambda while we still have the read lock.

            if (!(Boolean)pred.apply(TransactionManager.gets(boxes)))
            {
                try
                {
                    // Note: once we synchronize on ourselves, any calls from
                    // a committing transaction to our (synchronized) apply()
                    // will block.

                    synchronized (this)
                    {
                        // add ourselves as a watcher on boxes
                        for (int i = 0; i < boxes.size(); i++)
                            ((Box)boxes.get(i)).addWatcher(this);

                        // when we release the read lock, box updates will start
                        // to flow again. but because we're in a synchronized block,
                        // post-commit calls to our apply() will continue to block
                        // until we go into our wait().

                        for (int i = 0; i < boxes.size(); i++)
                            ((Box)boxes.get(i)).releaseReadLock();

                        readLocked = false;

                        // on a commit to the box, our apply() gets called because
                        // we're a watcher. this notifies us and we test the new value.
                        do
                        {
                            wait();
                        }
                        while (!(Boolean)pred.apply(TransactionManager.gets(boxes)));

                        // btw this here is why watchers is a versioned structure.
                        // We're probably in the middle of iterating through the
                        // watchers in runWatchers()

                        for (int i = 0; i < boxes.size(); i++)
                            ((Box)boxes.get(i)).removeWatcher(this);
                    }
                }
                catch (InterruptedException e)
                {
                    Logging.warning("interrupted during wait()");
                }
            }
        }
        finally
        {
            if (readLocked)
            {
                for (int i = 0; i < boxes.size(); i++)
                    ((Box)boxes.get(i)).releaseReadLock();
            }
        }
    }

    // Lambda impl

    /**
     * As a watcher, apply() will get an (old, new) value pair when a new
     * value is committed to our box. At that point we call {@link #notify},
     * which will wake up the waiting thread to retest the wait predicate
     * against the new value (which we've saved to a member variable).
     * Note: our apply() is synchronized so we don't lose calls during setup -
     * see {@link #wait}.
     */
    synchronized public Object apply(final Object args)
    {
        notify();
        return null;
    }
}