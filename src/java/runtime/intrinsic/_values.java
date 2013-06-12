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
 * returns the values of a map as a list
 *
 * @author Basil Hosmer
 */
public final class _values extends IntrinsicLambda
{
    public static final _values INSTANCE = new _values(); 
    public static final String NAME = "values";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((MapValue)arg);
    }

    public static ListValue invoke(final MapValue map)
    {
        return PersistentList.init(map.values().iterator(), map.size());
    }
}
