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
import runtime.rep.Tuple;

import java.util.Iterator;

/**
 * Return new list with original list's contents,
 * but with values at indexes replaced.
 * Note that we roll over the value list.
 */
public final class _listsets extends IntrinsicLambda
{
    public static final _listsets INSTANCE = new _listsets(); 
    public static final String NAME = "listsets";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;

        return invoke((ListValue)args.get(0), (ListValue)args.get(1),
            (ListValue)args.get(2));
    }

    public static ListValue invoke(final ListValue list,
        final ListValue indexes, final ListValue vals)
    {
        ListValue result = list;

        final Iterator<?> valiter = vals.size() >= indexes.size() ?
            vals.iterator() : Iterators.cycle(vals);

        for (final Object index : indexes)
            result = result.update((Integer)index, valiter.next());

        return result;
    }
}
