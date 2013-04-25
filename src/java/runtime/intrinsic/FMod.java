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
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * float mod()
 *
 * @author Basil Hosmer
 */
public final class FMod extends IntrinsicLambda
{
    public static final String NAME = "fmod";

    public static final Type TYPE = Types.fun(
        Types.tup(Types.DOUBLE, Types.DOUBLE), Types.DOUBLE);

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
        return invoke((Double)args.get(0), (Double)args.get(1));
    }

    public static double invoke(final double arg0, final double arg1)
    {
        return arg0 % arg1;
    }
}