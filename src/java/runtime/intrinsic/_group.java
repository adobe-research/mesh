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
import runtime.rep.list.PersistentList;
import runtime.rep.map.MapValue;
import runtime.rep.map.PersistentMap;
import runtime.rep.Tuple;

import java.util.Iterator;

/**
 * group by - given a list of keys and a list of values,
 * returns a map from keys to collections of values.
 * Note that we roll over the key list.
 *
 * @author Basil Hosmer
 */
public final class _group extends IntrinsicLambda
{
    public static final _group INSTANCE = new _group(); 
    public static final String NAME = "group";

    public String getName()
    {
        return NAME;
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

        final Iterator<?> keyiter = keys.size() >= vals.size() ?
            keys.iterator() : Iterators.cycle(keys);

        for (final Object val : vals)
        {
            final Object key = keyiter.next();

            final PersistentList keyvals = (PersistentList)result.get(key);

            if (keyvals == null)
            {
                result.assocUnsafe(key, PersistentList.single(val));
            }
            else
            {
                result.assocUnsafe(key, keyvals.appendUnsafe(val));
            }
        }

        return result;
    }
}
