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
 * true if key argument is valid key for map argument
 *
 * @author Basil Hosmer
 */
public final class _iskey extends IntrinsicLambda
{
    public static final _iskey INSTANCE = new _iskey(); 
    public static final String NAME = "iskey";

    public String getName()
    {
        return NAME;
    }

    public final Boolean apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((MapValue)args.get(0), args.get(1));
    }

    public static boolean invoke(final MapValue map, final Object key)
    {
        return map.keySet().contains(key);
    }
}
