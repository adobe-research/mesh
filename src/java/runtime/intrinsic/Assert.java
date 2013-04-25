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
 * temp: throw with message if argument is false
 * TODO variant
 *
 * @author Basil Hosmer
 */
public final class Assert extends IntrinsicLambda
{
    public static final String NAME = "assert";

    public static final Type TYPE =
        Types.fun(Types.tup(Types.BOOL, Types.STRING), Types.unit());

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
        return invoke((Boolean)args.get(0), (String)args.get(1));
    }

    public static Object invoke(final boolean arg, final String msg)
    {
        if (!arg)
        {
            throw new AssertionError(msg);
        }

        return Tuple.UNIT;
    }
}