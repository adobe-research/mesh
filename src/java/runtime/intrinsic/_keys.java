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

import runtime.rep.list.ListValue;
import runtime.rep.map.MapValue;
import runtime.rep.list.PersistentList;

/**
 * returns the keyset of a map as a list
 *
 * @author Basil Hosmer
 */
public final class _keys extends IntrinsicLambda
{
    public static final _keys INSTANCE = new _keys(); 
    public static final String NAME = "keys";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object obj)
    {
        return invoke((MapValue)obj);
    }

    public static ListValue invoke(final MapValue map)
    {
        return PersistentList.init(map.keySet().iterator(), map.size());
    }
}
