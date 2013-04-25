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

import compile.type.*;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;

/**
 * first(list) = first element of non-empty list.
 * empty list throws, currently.
 *
 * @author Basil Hosmer
 */
public final class First extends IntrinsicLambda
{
    public static final String NAME = "first";

    private static final Type T = new TypeParam("T");
    private static final Type LIST_T = Types.list(T);

    public static final Type TYPE = Types.fun(LIST_T, T);

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
        return invoke((ListValue)arg);
    }

    public static Object invoke(final ListValue list)
    {
        return list.get(0);
    }
}