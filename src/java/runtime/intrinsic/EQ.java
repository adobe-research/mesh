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
 * (==), defined over all values. Value equality for
 * everything except functions and boxes, which are
 * identity.
 *
 * @author Basil Hosmer
 */
public final class EQ extends IntrinsicLambda
{
    public static final String NAME = "eq";

    private static Type T = new TypeParam("T");
    public static final Type TYPE = Types.fun(Types.tup(T, T), Types.BOOL);

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
        return invoke(args.get(0), args.get(1));
    }

    public static boolean invoke(final Object arg0, final Object arg1)
    {
        return arg0.hashCode() == arg1.hashCode() && arg0.equals(arg1);
    }
}