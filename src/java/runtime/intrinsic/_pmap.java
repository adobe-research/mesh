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
package runtime.intrinsic;

import runtime.sys.ConcurrencyManager;
import runtime.rep.Tuple;
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Parallel version of {@link _map}. Aliased to infix
 * operator '|:' in {@link compile.parse.Ops}.
 *
 * @author Basil Hosmer
 */
public final class _pmap extends IntrinsicLambda
{
    public static final _pmap INSTANCE = new _pmap(); 
    public static final String NAME = "pmap";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((ListValue)args.get(0), (Lambda)args.get(1));
    }

    public static ListValue invoke(final ListValue args, final Lambda func)
    {
        // if (TransactionManager.inTransaction())
        //    throw new RuntimeException("pfor not supported within transactions");

        final int size = args.size();

        if (size == 0)
            return PersistentList.EMPTY;

        PersistentList result = PersistentList.alloc(size);

        final ArrayList<FutureTask<Object>> futures =
            new ArrayList<FutureTask<Object>>(size);

        for (int i = 0; i < size; i++)
        {
            final int index = i;

            final Callable<Object> task = new Callable<Object>()
            {
                public Object call()
                {
                    return func.apply(args.get(index));
                }
            };

            final FutureTask<Object> future = new FutureTask<Object>(task);

            futures.add(future);

            ConcurrencyManager.execute(future);
        }

        // collect results
        int i = 0;
        for (final FutureTask<Object> future : futures)
        {
            try
            {
                result = result.updateUnsafe(i, future.get());
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            i++;
        }

        return result;
    }
}
