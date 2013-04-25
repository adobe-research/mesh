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

import com.google.common.collect.Iterators;
import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

import java.util.Iterator;

/**
 * Create a new map with original's contents, but with
 * given keys associated with values.
 * Note that we roll over the value list.
 *
 * @author Basil Hosmer
 */
public final class MapSets extends IntrinsicLambda
{
    public static final String NAME = "mapsets";

    private static final TypeParam K = new TypeParam("K");
    private static final TypeParam V = new TypeParam("V");
    private static final Type MAP_KV = Types.map(K, V);
    private static final Type LIST_K = Types.list(K);
    private static final Type LIST_V = Types.list(V);
    private static final Type PARAM_TYPE = Types.tup(MAP_KV, LIST_K, LIST_V);

    public static final Type TYPE = Types.fun(PARAM_TYPE, MAP_KV);

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
        return invoke((MapValue)args.get(0), (ListValue)args.get(1), (ListValue)args.get(2));
    }

    public static MapValue invoke(final MapValue map, final ListValue keys,
        final ListValue vals)
    {
        MapValue result = map;

        final Iterator<?> valiter = Iterators.cycle(vals);

        for (final Object key : keys)
            result = result.assoc(key, valiter.next());

        return result;
    }
}