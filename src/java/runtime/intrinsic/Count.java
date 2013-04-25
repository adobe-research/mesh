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
import runtime.rep.list.IntRange;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;

/**
 * count(n) returns a list of ints [0, ..., n - 1]
 *
 * @author Basil Hosmer
 */
public final class Count extends IntrinsicLambda
{
    public static final String NAME = "count";

    public static final Type TYPE =
        Types.fun(Types.INT, Types.list(Types.INT));

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
        return invoke((Integer)arg);
    }

    public static ListValue invoke(final int n)
    {
        return IntRange.create(0, n);
    }
}