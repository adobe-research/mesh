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
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

/**
 * Apply a list of keys to a map of items, yielding a selection list.
 * E.g. > maplm([0, 1, 2], [0: "Zero", 1: "One", 2: "Two"])
 * => ["Zero", "One", "Two"]
 *
 * @author Basil Hosmer
 */
public final class MapLM extends IntrinsicLambda
{
    public static final String NAME = "maplm";

    private static final TypeParam K = new TypeParam("K");
    private static final TypeParam V = new TypeParam("V");
    private static final Type LIST_K = Types.list(K);
    private static final Type MAP_K_V = Types.map(K, V);
    private static final Type LIST_V = Types.list(V);

    public static final Type TYPE =
        Types.fun(Types.tup(LIST_K, MAP_K_V), LIST_V);

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
        return invoke((ListValue)args.get(0), (MapValue)args.get(1));
    }

    public static ListValue invoke(final ListValue keys, final MapValue map)
    {
        return keys.select(map);
    }
}