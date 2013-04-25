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
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;

/**
 * drop(list, n) = drops first n if n > 0, last -n if n < 0.
 * n is held to list size.
 *
 * @author Basil Hosmer
 */
public final class Drop extends IntrinsicLambda
{
    public static final String NAME = "drop";

    private static final Type T = new TypeParam("T");
    private static final Type LIST_T = Types.list(T);

    public static final Type TYPE = Types.fun(Types.tup(Types.INT, LIST_T), LIST_T);

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
        return invoke((Integer)args.get(0), (ListValue)args.get(1));
    }

    public static ListValue invoke(final int n, final ListValue list)
    {
        if (n == 0)
            return list;

        final int size = list.size();

        return n >= 0 ?
            list.subList(Math.min(size, n), size) :
            list.subList(0, Math.max(size + n, 0));
    }
}