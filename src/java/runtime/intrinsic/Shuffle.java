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
import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

import java.util.Collections;
import java.util.List;

/**
 * shuffle a list.
 *
 * @author Basil Hosmer
 */
public final class Shuffle extends IntrinsicLambda
{
    public static final String NAME = "shuffle";

    private static final Type LIST_T = Types.list(new TypeParam("T"));

    public static final Type TYPE = Types.fun(LIST_T, LIST_T);

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
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