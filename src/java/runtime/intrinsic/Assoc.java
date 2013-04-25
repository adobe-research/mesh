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
import compile.type.*;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.map.MapValue;
import runtime.rep.map.PersistentMap;
import runtime.rep.Tuple;

import java.util.Iterator;

/**
 * Create map from key and value lists.
 * For duplicate keys, last instance wins.
 * Equal list length is not required, value
 * list is cycled over if necessary.
 *
 * @author Basil Hosmer
 */
public final class Assoc extends IntrinsicLambda
{
    public static final String NAME = "assoc";

    private static final TypeParam K = new TypeParam("K");
    private static final Type LIST_K = Types.list(K);

    private static final TypeParam V = new TypeParam("V");
    private static final Type LIST_V = Types.list(V);

    private static final Type MAP_KV = Types.map(K, V);

    public static final Type TYPE = Types.fun(Types.tup(LIST_K, LIST_V), MAP_KV);

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

    public static MapValue invoke(final ListValue keys, final ListValue vals)
    {
        if (vals.size() == 0)
            return PersistentMap.EMPTY;

        final PersistentMap result = PersistentMap.fresh();

        final Iterator<?> valiter = Iterators.cycle(vals);

        for (final Object key : keys)
            result.assocUnsafe(key, valiter.next());

        return result;
    }
}