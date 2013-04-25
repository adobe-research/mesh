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
import runtime.rep.list.PersistentList;
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

import java.util.Map;

/**
 * Return the values of a map as a list.
 *
 * @author Basil Hosmer
 */
public final class _entries extends IntrinsicLambda
{
    public static final _entries INSTANCE = new _entries(); 
    public static final String NAME = "entries";

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
        final PersistentList result = PersistentList.alloc(map.size());

        int i = 0;
        for (final Map.Entry<Object, Object> entry : map.entrySet())
            result.updateUnsafe(i++,
                Tuple.from(entry.getKey(), entry.getValue()));

        return result;
    }
}
