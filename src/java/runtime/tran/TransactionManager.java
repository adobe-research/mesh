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

/**
 * Transaction manager. Each thread has a single transaction
 * object, created on demand and dormant between transactions.
 * This class provides the bulk of the transaction API used by
 * the intrinsic functions that define it in the surface language.
 *
 * @author Basil Hosmer
 */
public final class TransactionManager
{
    /**
     * Singleton exception object, thrown to initiate retries.
     */
    static final Retry GENERIC_RETRY = new Retry();

    /**
     * Thread-local transaction object.
     */
    private static final ThreadLocal<Transaction> TRAN = new ThreadLocal<Transaction>()
    {
        @Override
        protected Transaction initialValue()
        {
            return new Transaction();
        }
    };

    /**
     * Return thread's running transaction, or null.
     */
    public static Transaction getTransaction()
    {
        final Transaction tran = TRAN.get();

        return tran.isRunning() ? tran : null;
    }

    /**
     * Return true if there is a running transaction.
     */
    public static boolean inTransaction()
    {
        return TRAN.get().isRunning();
    }

    /**
     * Return thread's running transaction, or throw.
     */
    private static Transaction requireTransaction()
    {
        final Transaction tran = TRAN.get();

        if (!tran.isRunning())
            throw new IllegalStateException("No transaction running");

        return tran;
    }

    /**
     * Run a function inside a transaction.
     * Note that nested calls are simply run in the context
     * of the outer transaction.
     */
    public static Object apply(final Lambda f)
    {
        final Transaction tran = TRAN.get();

        return tran.isRunning() ? f.apply(Tuple.UNIT) : tran.runApply(f);
    }

    /**
     * Tuplized version of {@link runtime.tran.Box#getValue()}.
     * Return a synchronized tuple of values from a tuple of
     * boxes. Note: pinning is per {@link runtime.tran.Box#getValue()},
     * see comment there.
     */
    public static Tuple gets(final Tuple boxes)
    {
        final Transaction tran = TRAN.get();

        if (tran.isRunning())
            return pins(boxes);

        final Lambda gets = new Lambda()
        {
            public Object apply(final Object ignored)
            {
                return tran.gets(boxes);
            }
        };

        return (Tuple)tran.runApply(gets);
    }

    /**
     * Tuplized version of {@link runtime.tran.Box#snapValue()}. Return
     * a synchronized tuple of snapshot values from a tuple of boxes,
     * read without pinning.
     */
    public static Tuple snaps(final Tuple boxes)
    {
        final Transaction tran = TRAN.get();

        if (tran.isRunning())
            return tran.gets(boxes);

        final Lambda gets = new Lambda()
        {
            public Object apply(final Object ignored)
            {
                return tran.gets(boxes);
            }
        };

        return (Tuple)tran.runApply(gets);
    }

    /**
     * Perform an in-transaction read with serializable consistency semantics
     * (a guarantee that we won't be updated by another transaction before the
     * pinning transaction commits). A transaction must be running when this
     * function is called.
     */
    public static Object pin(final Box box)
    {
        final Transaction tran = requireTransaction();
        tran.pin(box);

        return tran.get(box);
    }

    /**
     * Tuplized version of pin()
     */
    public static Tuple pins(final Tuple boxes)
    {
        final Transaction tran = requireTransaction();

        final int size = boxes.size();
        for (int i = 0; i < size; i++)
        {
            final Box box = (Box)boxes.get(i);
            tran.pin(box);
        }

        return tran.gets(boxes);
    }

    /**
     * Perform an in-transaction read after acquiring ownership.
     * A transaction must be running when this function is called.
     */
    public static Object own(final Box box)
    {
        final Transaction tran = requireTransaction();
        tran.own(box);

        return tran.get(box);
    }

    /**
     * Tuplized version of {@link #own}.
     */
    public static Tuple owns(final Tuple boxes)
    {
        final Transaction tran = requireTransaction();

        final int size = boxes.size();
        for (int i = 0; i < size; i++)
        {
            final Box box = (Box)boxes.get(i);
            tran.own(box);
        }

        return tran.gets(boxes);
    }

    /**
     * Set box to a given value. A transaction will be created if none
     * is running when this function is called.
     */
    public static Tuple put(final Box box, final Object val)
    {
        final Transaction tran = TRAN.get();

        if (tran.isRunning())
            tran.put(box, val);
        else
            tran.runPut(box, val);

        return Tuple.UNIT;
    }

    /**
     * Tuplized version of update--set values of a tuple of boxes
     * to the scattered results of a function from tuple to tuple
     */
    public static Tuple puts(final Tuple boxes, final Tuple vals)
    {
        // final Transaction tran = requireTransaction();
        final Transaction tran = TRAN.get();

        // return tran.put(box, val);
        if (tran.isRunning())
            return tran.puts(boxes, vals);

        final Lambda puts = new Lambda()
        {
            public Tuple apply(final Object ignore)
            {
                return tran.puts(boxes, vals);
            }
        };

        return (Tuple)tran.runApply(puts);
    }

    /**
     * Set box's value to the result of a function applied to current value.
     * A transaction will be created if none is running when this function
     * is called.
     */
    public static Tuple update(final Box box, final Lambda f)
    {
        final Transaction tran = TRAN.get();

        if (tran.isRunning())
            tran.update(box, f);
        else
            tran.runUpdate(box, f);

        return Tuple.UNIT;
    }

    /**
     * Tuplized version of update--set values of a tuple of boxes
     * to the scattered results of a function from tuple to tuple
     */
    public static Tuple updates(final Tuple boxes, final Lambda f)
    {
        final Transaction tran = TRAN.get();

        if (tran.isRunning())
            return tran.updates(boxes, f);

        final Lambda updates = new Lambda()
        {
            public Tuple apply(final Object ignore)
            {
                return tran.updates(boxes, f);
            }
        };

        return (Tuple)tran.runApply(updates);
    }

    /**
     * Sets write box's value to the result of a function applied
     * to read box's current value. A transaction will be created
     * if none is running when this function is called.
     */
    public static Object transfer(
        final Box write, final Lambda f, final Box read)
    {
        final Transaction tran = TRAN.get();

        if (tran.isRunning())
            return tran.transfer(write, f, read);

        final Lambda assign = new Lambda()
        {
            public Object apply(final Object ignore)
            {
                return tran.transfer(write, f, read);
            }
        };

        return tran.runApply(assign);
    }

    /**
     *
     */
    public static Tuple transfers(
        final Tuple writes, final Lambda f, final Tuple reads)
    {
        final Transaction tran = TRAN.get();

        if (tran.isRunning())
            return tran.transfers(writes, f, reads);

        final Lambda assign = new Lambda()
        {
            public Object apply(final Object ignore)
            {
                return tran.transfers(writes, f, reads);
            }
        };

        return (Tuple)tran.runApply(assign);
    }
}