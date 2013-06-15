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
package runtime.intrinsic.tran;

import runtime.intrinsic.IntrinsicLambda;
import runtime.intrinsic._tostr;
import runtime.rep.Lambda;
import runtime.rep.Tuple;
import runtime.sys.Logging;
import runtime.tran.Box;
import runtime.tran.TransactionManager;

import java.util.LinkedList;

/**
 * Transactional wait/notify against a tuple of boxes.
 * awaits(boxes, pred) puts current thread into wait state
 * until/unless pred(gets(boxes)) returns true. pred() is
 * called each time a value is committed to any box.
 * TODO restructure signature to take (box, pred) pairs
 *
 * @author Basil Hosmer
 */
public final class _awaits extends IntrinsicLambda
{
    public static final _awaits INSTANCE = new _awaits();
    public static final String NAME = "awaits";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Tuple)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Tuple boxes, final Lambda pred)
    {
        if (TransactionManager.getTransaction() == null)
            new MultiWaiter(boxes).await(pred);

        return Tuple.UNIT;
    }

    /**
     * Used by {@link _awaits}. For a given box,
     * our method {@link #await(runtime.rep.Lambda)} implements
     * wait(box, pred) by by attaching ourselves as a watcher to the box,
     * then doing a thread wait until pred(newval) returns true on box commit.
     * (We remove ourselves from the box's watcher list before returning.)
     *
     * @author Basil Hosmer
     */
    private static final class MultiWaiter
    {
        private final int size;
        private final Tuple boxes;

        private Tuple vals;
        private Lambda[] reactors;
        private LinkedList<Object> updates;
        private LinkedList<Integer> indexes;

        public MultiWaiter(final Tuple boxes)
        {
            this.size = boxes.size();
            this.boxes = boxes;
        }

        /**
         * For our boxes and a given pred, implement awaits(boxes, pred) by attaching
         * watchers to our boxes, then doing a thread wait until
         * pred(newval) returns true.
         */
        public synchronized void await(final Lambda pred)
        {
            // within a transaction, test predicate and (if it doesn't pass)
            // add ourselves as a reactor. Because our apply queues updates,
            // we're guaranteed not to lose any between our commit and

            if ((Boolean)TransactionManager.apply(new Lambda()
            {
                public Object apply(final Object unit)
                {
                    vals = TransactionManager.owns(boxes);

                    if (!(Boolean)pred.apply(vals))
                    {
                        makeReactors();

                        updates = new LinkedList<Object>();
                        indexes = new LinkedList<Integer>();

                        for (int i = 0; i < size; i++)
                            ((Box)boxes.get(i)).addReactor(reactors[i]);

                        return true;
                    }

                    return false;
                }
            }))
            {
                // predicate failed--go into wait loop.
                // note that any updates that happened between adding ourselves and
                // this code have been queued in updates

                do
                {
                    if (updates.isEmpty())
                    {
                        try
                        {
                            wait();
                        }
                        catch (InterruptedException e)
                        {
                            Logging.warning(Thread.currentThread().getId() +
                                ": interrupted during wait()");
                        }
                    }

                    final Object update = updates.remove();
                    final int index = indexes.remove();

                    vals.set(index, update);
                }
                while (!(Boolean)pred.apply(vals));

                // remove ourselves
                TransactionManager.apply(new Lambda()
                {
                    public Object apply(final Object unit)
                    {
                        TransactionManager.owns(boxes);

                        for (int i = 0; i < size; i++)
                            ((Box)boxes.get(i)).addReactor(reactors[i]);

                        return null;
                    }
                });
            }
        }

        /**
         *
         */
        private void makeReactors()
        {
            reactors = new Lambda[size];
            for (int i = 0; i < size; i++)
            {
                final int index = i;

                reactors[i] = new Lambda()
                {
                    public Object apply(final Object val)
                    {
                        updates.add(val);
                        indexes.add(index);

                        synchronized (MultiWaiter.this)
                        {
                            MultiWaiter.this.notify();
                        }

                        return null;
                    }
                };
            }
        }
    }
}
