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
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

/**
 * apply a map of keys to a map of values, yielding a map of selected values.
 * > mapmm([#a: 1, #b: 2], [1: "One", 2: "Two"])
 * => [#a: "One", #b: "Two"]
 *
 * @author Basil Hosmer
 */
public final class _mapmm extends IntrinsicLambda
{
    public static final _mapmm INSTANCE = new _mapmm(); 
    public static final String NAME = "mapmm";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((MapValue)args.get(0), (MapValue)args.get(1));
    }

    public static MapValue invoke(final MapValue keys, final MapValue items)
    {
        return keys.select(items);
    }
}
