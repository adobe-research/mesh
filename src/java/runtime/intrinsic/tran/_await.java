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
import runtime.tran.*;

import java.util.LinkedList;

/**
 * Transactional wait/notify.
 * wait(box, pred) puts current thread into wait state
 * until/unless pred(get(box)) returns true. pred() is
 * called each time a value is committed to box.
 *
 * @author Basil Hosmer
 */
public final class _await extends IntrinsicLambda
{
    public static final _await INSTANCE = new _await();
    public static final String NAME = "await";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Box)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Box box, final Lambda pred)
    {
        if (TransactionManager.getTransaction() == null)
            new Waiter(box).await(pred);

        return Tuple.UNIT;
    }

    /**
     * For a given box,
     * our method {@link #await(runtime.rep.Lambda)} implements
     * await(box, pred) by by attaching ourselves as a watcher to the box,
     * then doing a thread wait until pred(val) returns true on box commit.
     * (We remove ourselves from the box's watcher list before returning.)
     *
     * @author Basil Hosmer
     */
    private static final class Waiter implements Lambda
    {
        private final Box box;
        private LinkedList<Object> updates;

        public Waiter(final Box box)
        {
            this.box = box;
            this.updates = null;
        }

        /**
         * For our box and a given pred, implement wait(box, pred) by attaching
         * ourselves as a watcher to our box, then doing a thread wait until
         * pred(newval) returns true.
         */
        public synchronized void await(final Lambda pred)
        {
            // within a transaction, test predicate and (if it doesn't pass)
            // add ourselves as a reactor. Because our apply queues updates,
            // we're guaranteed not to lose any between our commit and

            if ((Boolean)TransactionManager.apply(new Lambda() {

                public Object apply(final Object unit)
                {
                    TransactionManager.own(box);

                    if (!(Boolean)pred.apply(box.getValue()))
                    {
                        updates = new LinkedList<Object>();
                        box.addReactor(Waiter.this);
                        return true;
                    }

                    return false;
                }
            }))
            {
                // predicate failed--go into wait loop.
                // note that any updates that happened between adding ourselves and
                // this code have been queued in updates
                Object val;
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
                            Logging.warning("interrupted during wait()");
                        }
                    }

                    val = updates.remove();
                }
                while (!(Boolean)pred.apply(val));

                // remove ourselves
                TransactionManager.apply(new Lambda() {

                    public Object apply(final Object unit)
                    {
                        TransactionManager.own(box);
                        box.removeReactor(Waiter.this);
                        return null;
                    }
                });
            }
        }

        // Lambda impl

        /**
         * As a reactor, apply() will get called when a new value is
         * committed to our box. At that point we enqueue the new value
         * and call {@link #notify}, which will wake up the waiting thread
         * to test the wait predicate against the new value(s).
         */
        public synchronized Object apply(final Object val)
        {
            updates.add(val);
            notify();
            return null;
        }
    }
}
