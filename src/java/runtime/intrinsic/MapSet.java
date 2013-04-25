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
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

/**
 * create a new map with original's contents, with given key
 * now associated with value
 *
 * @author Basil Hosmer
 */
public final class MapSet extends IntrinsicLambda
{
    public static final String NAME = "mapset";

    private static final TypeParam K = new TypeParam("K");
    private static final TypeParam V = new TypeParam("V");
    private static final Type MAP_KV = Types.map(K, V);
    private static final Type PARAM_TYPE = Types.tup(MAP_KV, K, V);

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
        return invoke((MapValue)args.get(0), args.get(1), args.get(2));
    }

    public static MapValue invoke(final MapValue map, final Object k, final Object v)
    {
        return map.assoc(k, v);
    }
}