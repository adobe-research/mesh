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
import runtime.rep.Tuple;

import java.util.Iterator;

/**
 * the inverse of {@link _zip} (mod ragged lists)
 *
 * @author Basil Hosmer
 */
public final class _unzip extends IntrinsicLambda
{
    public static final _unzip INSTANCE = new _unzip(); 
    public static final String NAME = "unzip";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((ListValue)arg);
    }

    public static Tuple invoke(final ListValue list)
    {
        final int len = list.size();

        if (len == 0)
            return Tuple.UNIT;

        final Iterator<?> iter = list.iterator();
        final Tuple first = (Tuple)iter.next();
        final int wid = first.size();

        final PersistentList[] lists = new PersistentList[wid];

        for (int j = 0; j < wid; j++)
        {
            final Object item = first.get(j);
            lists[j] = PersistentList.alloc(len);
            lists[j].updateUnsafe(0, item);
        }

        for (int i = 1; iter.hasNext(); i++)
        {
            final Tuple tup = (Tuple)iter.next();
            for (int j = 0; j < wid; j++)
                lists[j].updateUnsafe(i, tup.get(j));
        }

        return Tuple.from(lists);
    }
}
