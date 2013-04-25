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

import runtime.conc.ConcurrencyManager;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;
import runtime.tran.TransactionManager;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * parallel for
 *
 * @author Basil Hosmer
 */
public final class _pfor extends IntrinsicLambda
{
    public static final _pfor INSTANCE = new _pfor(); 
    public static final String NAME = "pfor";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((ListValue)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final ListValue indexes, final Lambda func)
    {
        // if (TransactionManager.inTransaction())
        //    throw new RuntimeException("pfor not supported within transactions");

        final int size = indexes.size();

        final ArrayList<FutureTask<Object>> futures =
            new ArrayList<FutureTask<Object>>(size);

        for (int i = 0; i < size; i++)
        {
            final int index = i;

            final Callable<Object> callable = new Callable<Object>()
            {
                public Object call()
                {
                    func.apply(indexes.get(index));
                    return null;
                }
            };

            final FutureTask<Object> future = new FutureTask<Object>(callable);

            futures.add(future);

            ConcurrencyManager.execute(future);
        }

        // wait for all threads to complete
        for (final FutureTask<Object> future : futures)
        {
            try
            {
                future.get();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        return Tuple.UNIT;
    }
}
