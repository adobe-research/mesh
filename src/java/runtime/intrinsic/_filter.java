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
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.Tuple;

/**
 * filter(list, pred) -> list items where pred
 *
 * @author Basil Hosmer
 */
public final class _filter extends IntrinsicLambda
{
    public static final _filter INSTANCE = new _filter(); 
    public static final String NAME = "filter";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((ListValue)args.get(0), (Lambda)args.get(1));
    }

    public static ListValue invoke(final ListValue list, final Lambda pred)
    {
        final int listsize = list.size();

        if (listsize == 0)
            return PersistentList.EMPTY;

        final Object[] items = new Object[listsize];

        int i = 0;
        for (final Object item : list)
            if ((Boolean)pred.apply(item))
                items[i++] = item;

        if (i == 0)
            return PersistentList.EMPTY;

        return PersistentList.init(Iterators.forArray(items), i);
    }
}
