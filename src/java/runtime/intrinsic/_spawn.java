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
import runtime.rep.Tuple;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;

/**
 * thread(block) runs block asynchronously in a new thread.
 *
 * @author Basil Hosmer
 */
public final class _spawn extends IntrinsicLambda
{
    public static final _spawn INSTANCE = new _spawn(); 
    public static final String NAME = "spawn";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Lambda)arg);
    }

    public static Tuple invoke(final Lambda func)
    {
        ConcurrencyManager.execute(new Runnable()
        {
            public void run()
            {
                func.apply(Tuple.UNIT);
            }
        });

        return Tuple.UNIT;
    }
}
