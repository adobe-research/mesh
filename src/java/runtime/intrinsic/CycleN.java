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
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;

/**
 * Like cycle, but with an additional upper limit on
 * the number of times function is called.
 *
 * @author Basil Hosmer
 */
public final class CycleN extends IntrinsicLambda
{
    public static final String NAME = "cyclen";

    private static final Type T = new TypeParam("T");
    private static final Type PARAM_TYPE = Types.tup(Types.INT, T, Types.fun(T, T));
    public static final Type TYPE = Types.fun(PARAM_TYPE, T);

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
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), args.get(1), (Lambda)args.get(2));
    }

    public static Object invoke(final int n, final Object init, final Lambda f)
    {
        Object result = init;

        for (int i = 0; i < n; i++)
            result = f.apply(result);

        return result;
    }
}