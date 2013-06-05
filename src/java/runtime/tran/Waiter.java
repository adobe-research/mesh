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

import java.util.LinkedList;

/**
 * Used by {@link runtime.intrinsic.tran._await} and
 * {@link runtime.intrinsic.tran._awaits}. For a given box (or set of boxes),
 * we implement await(box(es), pred) by attaching ourselves as a watcher to the
 * box(es), then doing a thread wait until pred(newval) returns true on box commit.
 * (We remove ourselves from the boxs' watcher list before returning.)
 *
 * @author Basil Hosmer
 */
public final class Waiter extends Watcher
{
    private LinkedList<Object> updates;

    public Waiter(final Boxes boxes, final Lambda pred)
    {
        super(boxes, pred);
        updates = new LinkedList<Object>();
    }

    /**
     * For our box and a given pred, implement wait(box, pred) by attaching
     * ourselves as a watcher to our box, then doing a thread wait until
     * pred(newval) returns true.
     */
    public void start()
    {
        boxes.acquireWriteLocks();
        stash = boxes.getValues();

        try
        {
            // if pred passes current value, release read lock and
            // don't wait the thread. otherwise, set up and add a
            // box watcher lambda while we still have the read lock.
            if (!(Boolean)action.apply(stash))
            {
                try
                {
                    // Note: once we synchronize on ourselves, any calls from a
                    // committing transaction to our (synchronized)
                    // commitAction() will block.
                    synchronized (this)
                    {
                        // add ourselves as a watcher on these boxes
                        boxes.addWatcher(this);

                        // when we release the lock, box updates will start to
                        // flow again. but because we're in a synchronized
                        // block, post-commit calls to commitAction() will
                        // continue to block until we go into our wait().

                        boxes.releaseWriteLocks();

                        Object newValues;
                        do
                        {
                            while (updates.isEmpty())
                                wait();

                            newValues = updates.remove();
                        }
                        while (!(Boolean)action.apply(newValues));

                        // btw this here is why watchers is a versioned structure.
                        // We're probably in the middle of iterating through the
                        // watchers.
                        boxes.acquireWriteLocks();
                        boxes.removeWatcher(action);
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
            boxes.releaseWriteLocks();
        }
    }

    /**
     * As a watcher, commitAction() will get an (old, new) value pair when a
     * new value is committed to our box. At that point ew enqueue the new
     * value and call {@link #notify}, which will wake up the waiting thread to
     * test the wait predicate against the new value(s).
     */
    public void commitAction(final Tuple args)
    {
        synchronized (this)
        {
            final Object oldValues = args.get(0);
            final Object newValues = args.get(1);

            if (updates.isEmpty() || !oldValues.equals(newValues))
            {
                updates.add(newValues);
                notify();
            }
        }
    }
}
