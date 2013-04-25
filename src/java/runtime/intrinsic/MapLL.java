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
 * Apply a list of indexes to a list of items, yielding a selection list.
 * E.g. mapll([0, 2, 4], ["Zero", "One", "Two", "Three", "Four"])
 * => ["Zero", "Two", "Four"]
 *
 * @author Basil Hosmer
 */
public final class MapLL extends IntrinsicLambda
{
    public static final String NAME = "mapll";

    private static final TypeParam T = new TypeParam("T");
    private static final Type LIST_INT = Types.list(Types.INT);
    private static final Type LIST_T = Types.list(T);

    public static final Type TYPE = Types.fun(Types.tup(LIST_INT, LIST_T), LIST_T);

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
        return invoke((ListValue)args.get(0), (ListValue)args.get(1));
    }

    public static ListValue invoke(final ListValue indexes, final ListValue items)
    {
        return indexes.select(items);
    }
}