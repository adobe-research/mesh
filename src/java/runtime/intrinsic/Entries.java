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
import runtime.rep.list.PersistentList;
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

import java.util.Map;

/**
 * Return the values of a map as a list.
 *
 * @author Basil Hosmer
 */
public final class Entries extends IntrinsicLambda
{
    public static final String NAME = "entries";

    private static final TypeParam K = new TypeParam("K");
    private static final TypeParam V = new TypeParam("V");
    private static final Type MAP_KV = Types.map(K, V);
    private static final Type PAIR_KV = Types.tup(K, V);
    private static final Type LIST_PAIR_KV = Types.list(PAIR_KV);

    public static final Type TYPE = Types.fun(MAP_KV, LIST_PAIR_KV);

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
        return invoke((MapValue)arg);
    }

    public static ListValue invoke(final MapValue map)
    {
        final PersistentList result = PersistentList.alloc(map.size());

        int i = 0;
        for (final Map.Entry<Object, Object> entry : map.entrySet())
            result.updateUnsafe(i++,
                Tuple.from(entry.getKey(), entry.getValue()));

        return result;
    }
}