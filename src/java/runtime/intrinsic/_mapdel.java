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

import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

/**
 * create a new map with original's contents, minus the given key
 *
 * @author Basil Hosmer
 */
public final class _mapdel extends IntrinsicLambda
{
    public static final _mapdel INSTANCE = new _mapdel(); 
    public static final String NAME = "mapdel";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((MapValue)args.get(0), args.get(1));
    }

    public static MapValue invoke(final MapValue map, final Object k)
    {
        return map.unassoc(k);
    }
}
