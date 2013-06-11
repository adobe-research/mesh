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
 * our method {@link #wait(runtime.rep.Lambda)} implements
 * wait(box, pred) by by attaching ourselves as a watcher to the box,
 * then doing a thread wait until pred(newval) returns true on box commit.
 * (We remove ourselves from the box's watcher list before returning.)
 *
 * @author Basil Hosmer
 */
public final class MultiWaiter implements Lambda
{
    private static final class IndexedValue
    {
        final public Object value;
        final public int index;

        public IndexedValue(final Object obj, final int index)
        {
            this.value = obj;
            this.index = index;
        }
    }

    //
    // instance
    //

    private final Tuple boxes;
    private LinkedList<IndexedValue> updates;

    public MultiWaiter(final Tuple boxes)
    {
        this.boxes = boxes;
        this.updates = new LinkedList<IndexedValue>();
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
            ((Box)boxes.get(i)).acquireWriteLock();

        boolean writeLocked = true;

        try
        {
            // if pred passes current value, release read lock and
            // don't wait the thread. otherwise, set up and add a
            // box watcher lambda while we still have the read lock.
            final Tuple tuple = TransactionManager.gets(boxes);

            if (!(Boolean)pred.apply(tuple))
            {
                try
                {
                    final Object[] values = new Object[boxes.size()];
                    for (int i = 0; i < boxes.size(); ++i)
                        values[i] = tuple.get(i);


                    // Note: once we synchronize on ourselves, any calls from
                    // a committing transaction to our (synchronized) apply()
                    // will block.

                    synchronized (this)
                    {
                        // add ourselves as a watcher on boxes. we track the box's
                        // tuple position with an extra index argument assoc'd to
                        // watcher. TODO make this part of the watcher API?
                        for (int i = 0; i < boxes.size(); i++)
                            ((Box)boxes.get(i)).addWatcher(this, i);

                        // when we release the read lock, box updates will start
                        // to flow again. but because we're in a synchronized block,
                        // post-commit calls to our apply() will continue to block
                        // until we go into our wait().

                        for (int i = 0; i < boxes.size(); i++)
                            ((Box)boxes.get(i)).releaseWriteLock();

                        writeLocked = false;

                        // on a commit to the box, our apply() gets called because
                        // we're a watcher. this notifies us and we test the new value.
                        do
                        {
                            if (updates.isEmpty())
                                wait();

                            final IndexedValue newValue = updates.remove();
                            values[newValue.index] = newValue.value;
                        }
                        while (!(Boolean)pred.apply(Tuple.from(values)));

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
            if (writeLocked)
            {
                for (int i = 0; i < boxes.size(); i++)
                    ((Box)boxes.get(i)).releaseWriteLock();
            }
        }
    }

    // Lambda impl

    /**
     * As a watcher, our apply() will get the argument:
     * ((oldvalue, newvalue), cargo)
     * when a new value is committed to our box. (Cargo is the index
     * value we supplied when adding the watcher to the box.)
     * When called, we call {@link #notify},
     * which will wake up the waiting thread to retest the wait predicate
     * against the new tuple of values (which we've saved to a member variable).
     * Note: our apply() is synchronized so we don't lose calls during setup -
     * see {@link #wait}.
     */
    synchronized public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;

        final Tuple values = (Tuple)args.get(0);
        final Object oldValue = values.get(0);
        final Object newValue = values.get(1);

        final int index = (Integer)args.get(1);

        if (updates.isEmpty() || !oldValue.equals(newValue))
        {
            updates.add(new IndexedValue(newValue, index));
            notify();
        }

        return null;
    }
}
