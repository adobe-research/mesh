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
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

import java.util.Collections;
import java.util.List;

/**
 * shuffle a list.
 *
 * @author Basil Hosmer
 */
public final class _shuffle extends IntrinsicLambda
{
    public static final _shuffle INSTANCE = new _shuffle(); 
    public static final String NAME = "shuffle";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((ListValue)arg);
    }

    public static ListValue invoke(final ListValue list)
    {
        final List<Object> temp = Lists.newArrayList(list);
        Collections.shuffle(temp);

        return PersistentList.init(temp.iterator(), list.size());
    }
}
