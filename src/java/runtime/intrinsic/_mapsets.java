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
public final class _mapsets extends IntrinsicLambda
{
    public static final _mapsets INSTANCE = new _mapsets(); 
    public static final String NAME = "mapsets";

    public String getName()
    {
        return NAME;
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
