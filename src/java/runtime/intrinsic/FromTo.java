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
import runtime.rep.Tuple;

/**
 * create list of ints [from..to], ascending or descending
 *
 * @author Basil Hosmer
 */
public final class FromTo extends IntrinsicLambda
{
    public static final String NAME = "fromto";

    public static final Type TYPE =
        Types.fun(Types.tup(Types.INT, Types.INT), Types.list(Types.INT));

    public String getName()
    {
        return NAME;
    }

    public final Type getType()
    {
        return TYPE;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (Integer)args.get(1));
    }

    public static ListValue invoke(final int from, final int to)
    {
        return IntRange.create(from, from <= to ? to + 1 : to - 1);
    }
}