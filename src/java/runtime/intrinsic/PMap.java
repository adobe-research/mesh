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

import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.conc.ConcurrencyManager;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Parallel version of {@link Map}. Aliased to infix
 * operator '|:' in {@link compile.parse.Ops}.
 *
 * @author Basil Hosmer
 */
public class PMap extends IntrinsicLambda
{
    public static final String NAME = "pmap";

    private static final TypeParam X = new TypeParam("X");
    private static final TypeParam Y = new TypeParam("Y");
    private static final Type LIST_X = Types.list(X);
    private static final Type FUNC_X_Y = Types.fun(X, Y);
    private static final Type LIST_Y = Types.list(Y);

    public static final Type TYPE = Types.fun(Types.tup(LIST_X, FUNC_X_Y), LIST_Y);

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
    }

    public Object apply(final Object arg)
    {
        final Object[] args = (Object[])arg;
        return invoke((ListValue)args[0], (Lambda)args[1]);
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
