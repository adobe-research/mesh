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

import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

/**
 * Apply a map of indexes to a list of items, yielding a map of selected items.
 * E.g. mapml([#a: 0, #c: 2], ["Zero", "One", "Two"])
 * => [#a: "Zero", #c: "Two"]
 *
 * @author Basil Hosmer
 */
public final class _mapml extends IntrinsicLambda
{
    public static final _mapml INSTANCE = new _mapml(); 
    public static final String NAME = "mapml";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((MapValue)args.get(0), (ListValue)args.get(1));
    }

    public static MapValue invoke(final MapValue keys, final ListValue items)
    {
        return keys.select(items);
    }
}
