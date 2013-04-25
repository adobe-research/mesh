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
import runtime.rep.Tuple;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;

/**
 * thread(block) runs block asynchronously in a new thread.
 *
 * @author Basil Hosmer
 */
public final class Spawn extends IntrinsicLambda
{
    public static final String NAME = "spawn";

    private static final TypeParam T = new TypeParam("T");
    private static final Type FUNC_UNIT_T = Types.fun(Types.tup(), T);
    public static final Type TYPE = Types.fun(FUNC_UNIT_T, Types.unit());

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