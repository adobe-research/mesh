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
import runtime.rep.Tuple;
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

import java.util.ArrayList;

/**
 * where() for strings
 *
 * @author Basil Hosmer
 */
public final class _strwhere extends IntrinsicLambda
{
    public static final _strwhere INSTANCE = new _strwhere(); 
    public static final String NAME = "strwhere";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((String)args.get(0), (Lambda)args.get(1));
    }

    public static ListValue invoke(final String s, final Lambda pred)
    {
        final int len = s.length();

        if (len == 0)
            return PersistentList.EMPTY;

        final ArrayList<Integer> indexes = Lists.newArrayList();

        for (int i = 0; i < len; i++)
            if ((Boolean)pred.apply(s.substring(i, i + 1)))
                indexes.add(i);

        return PersistentList.init(indexes.iterator(), indexes.size());
    }
}
