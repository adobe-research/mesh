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

import com.google.common.collect.Lists;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.Tuple;

import java.util.ArrayList;

/**
 * where(list, pred) -> indexes
 *
 * @author Basil Hosmer
 */
public final class _where extends IntrinsicLambda
{
    public static final _where INSTANCE = new _where(); 
    public static final String NAME = "where";

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
        final int len = list.size();

        if (len == 0)
            return PersistentList.EMPTY;

        final ArrayList<Integer> indexes = Lists.newArrayList();

        int i = 0;
        for (final Object item : list)
        {
            if ((Boolean)pred.apply(item))
                indexes.add(i);

            i++;
        }

        return PersistentList.init(indexes.iterator(), indexes.size());
    }
}
