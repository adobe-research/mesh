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
import runtime.rep.Tuple;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

/**
 * remove(list, item) -> removes all occurences of item from list
 *
 * @author Basil Hosmer
 */
public final class _remove extends IntrinsicLambda
{
    public static final _remove INSTANCE = new _remove(); 
    public static final String NAME = "remove";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((ListValue)args.get(0), args.get(1));
    }

    public static ListValue invoke(final ListValue list, final Object item)
    {
        final int n = list.size();

        if (n == 0)
            return PersistentList.EMPTY;

        final Object[] items = new Object[n];

        int i = 0;
        for (final Object listItem : list)
            if (!_eq.invoke(listItem, item))
                items[i++] = listItem;

        if (i == 0)
            return PersistentList.EMPTY;

        return PersistentList.init(Iterators.forArray(items), i);
    }
}
