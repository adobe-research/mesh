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

import runtime.rep.Lambda;
import runtime.rep.Tuple;
import runtime.sys.Logging;

import java.util.LinkedList;

/**
 * Used by {@link runtime.intrinsic.tran._await}. For a given box,
 * our method {@link #await(runtime.rep.Lambda)} implements
 * wait(box, pred) by by attaching ourselves as a watcher to the box,
 * then doing a thread wait until pred(newval) returns true on box commit.
 * (We remove ourselves from the box's watcher list before returning.)
 *
 * @author Basil Hosmer
 */
public final class Waiter implements Lambda
{
    private final Box box;
    private final LinkedList<Object> updates;

    public Waiter(final Box box)
    {
        this.box = box;
        this.updates = new LinkedList<Object>();
    }

    /**
     * For our box and a given pred, implement wait(box, pred) by attaching
     * ourselves as a watcher to our box, then doing a thread wait until
     * pred(newval) returns true.
     */
    public void await(final Lambda pred)
    {
        // hold box's write lock while we install ourselves
        boolean locked = true;
        box.acquireWriteLock();

        try
        {
            // if pred passes current value, release read lock and
            // don't wait the thread. otherwise, set up and add a
            // box watcher lambda while we still have the read lock.

            if (!(Boolean)pred.apply(box.getValue()))
            {
                try
                {
                    // Note: once we synchronize on ourselves, any calls from
                    // a committing transaction to our (synchronized) apply()
                    // will block.

                    synchronized (this)
                    {
                        // add ourselves as a watcher on this box
                        box.addWatcher(this);

                        //Logging.info("added watcher, count = {0}", box.getWatchers().size());

                        // when we release the lock, box updates will start
                        // to flow again. but because we're in a synchronized block,
                        // post-commit calls to our apply() will continue to block
                        // until we go into our wait().

                        box.releaseWriteLock();
                        locked = false;

                        Object newValue;
                        do
                        {
                            if (updates.isEmpty())
                                wait();

                            newValue = updates.remove();
                        }
                        while (!(Boolean)pred.apply(newValue));

                        //Logging.info("out of loop");

                        // btw this here is why watchers is a versioned structure.
                        // We're probably in the middle of iterating through the
                        // watchers in runWatchers()
                        locked = true;
                        box.acquireWriteLock();
                        box.removeWatcher(this);

                        //final Set<Object> watchers = box.getWatchers();
                        //Logging.info("removed watcher, count = {0}", watchers == null ? 0 : watchers.size());

                        box.releaseWriteLock();
                        locked = false;
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
            if (locked)
                box.releaseWriteLock();
        }
    }

    // Lambda impl

    /**
     * As a watcher, apply() will get an (old, new) value pair when a new value
     * is committed to our box. At that point ew enqueue the new value and call
     * {@link #notify}, which will wake up the waiting thread to test the
     * wait predicate against the new value(s).
     * Note: our apply() is synchronized so we don't lose calls during setup -
     * see {@link #await}.
     */
    synchronized public Object apply(final Object values)
    {
        final Tuple args = (Tuple)values;
        final Object oldValue = args.get(0);
        final Object newValue = args.get(1);

        if (updates.isEmpty() || !oldValue.equals(newValue))
        {
            updates.add(newValue);
            notify();
        }

        return null;
    }
}
