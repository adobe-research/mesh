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
import runtime.rep.Lambda;
import runtime.rep.Tuple;
import runtime.sys.Logging;
import runtime.tran.Box;
import runtime.tran.TransactionManager;

import java.util.LinkedList;

/**
 * Transactional wait/notify against a tuple of boxes.
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
        return invoke((Tuple)args.get(0), (Tuple)args.get(1));
    }

    public static Tuple invoke(final Tuple boxes, final Tuple preds)
    {
        if (TransactionManager.getTransaction() == null)
            new MultiWaiter(boxes).await(preds);

        return Tuple.UNIT;
    }

    /**
     *
     */
    private static final class MultiWaiter
    {
        private final int size;
        private final Tuple boxes;

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
        public synchronized void await(final Tuple preds)
        {
            // within a transaction, test predicates and if none pass,
            // add ourselves as a reactor to each box.

            if ((Boolean)TransactionManager.apply(new Lambda()
            {
                public Object apply(final Object unit)
                {
                    final Tuple vals = TransactionManager.owns(boxes);

                    // if any predicate returns true, don't wait the thread
                    for (int i = 0; i < size; i++)
                        if ((Boolean)((Lambda)preds.get(i)).apply(vals.get(i)))
                            return false;

                    reactors = new Lambda[size];
                    updates = new LinkedList<Object>();
                    indexes = new LinkedList<Integer>();

                    for (int i = 0; i < size; i++)
                        ((Box)boxes.get(i)).addReactor(reactors[i] = makeReactor(i));

                    return true;
                }
            }))
            {
                // predicate failed--go into wait loop.
                // note that any updates that happened between adding ourselves and
                // this code have been queued in updates

                Object update;
                int index;
                do
                {
                    while (updates.isEmpty())
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

                    update = updates.remove();
                    index = indexes.remove();
                }
                while (!(Boolean)((Lambda)preds.get(index)).apply(update));

                // remove ourselves
                TransactionManager.apply(new Lambda()
                {
                    public Object apply(final Object unit)
                    {
                        TransactionManager.owns(boxes);

                        for (int i = 0; i < size; i++)
                            ((Box)boxes.get(i)).removeReactor(reactors[i]);

                        return null;
                    }
                });
            }
        }

        /**
         *
         */
        private Lambda makeReactor(final int index)
        {
            return new Lambda()
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
