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
import runtime.rep.Tuple;

/**
 * conditional value selection (immediate if)
 *
 * @author Basil Hosmer
 */
public final class Iif extends IntrinsicLambda
{
    public static final String NAME = "iif";

    private static final Type T = new TypeParam("T");
    public static final Type TYPE =
        Types.fun(Types.tup(Types.BOOL, T, T), T);

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
        return invoke((Boolean)args.get(0), args.get(1), args.get(2));
    }

    public static Object invoke(final boolean cond, final Object t, final Object f)
    {
        return cond ? t : f;
    }
}