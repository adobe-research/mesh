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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Start running a block in a {@link Future} and return a lambda
 * that provides (blocking) access to the result.
 *
 * @author Basil Hosmer
 */
public final class _future extends IntrinsicLambda
{
    public static final _future INSTANCE = new _future(); 
    public static final String NAME = "future";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Lambda)arg);
    }

    public static Lambda invoke(final Lambda func)
    {
        final Callable<Object> callable = new Callable<Object>()
        {
            public Object call()
            {
                return func.apply(Tuple.UNIT);
            }
        };

        final FutureTask<Object> future = new FutureTask<Object>(callable);

        ConcurrencyManager.execute(future);

        return new Lambda()
        {
            public Object apply(final Object x)
            {
                try
                {
                    return future.get();
                }
                catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                return null;
            }
        };
    }
}
