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
import compile.type.Types;
import runtime.rep.Tuple;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

import java.util.ArrayList;

/**
 * where() for strings
 * NOTE passes substring to pred, not char
 *
 * @author Basil Hosmer
 */
public final class StrWhere extends IntrinsicLambda
{
    public static final String NAME = "strwhere";

    private static final Type LIST_INT = Types.list(Types.INT);

    private static final Type PRED_STR = Types.fun(Types.STRING, Types.BOOL);

    public static final Type TYPE =
        Types.fun(Types.tup(Types.STRING, PRED_STR), LIST_INT);

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
            if ((Boolean)pred.apply(s.substring(i)))
                indexes.add(i);

        return PersistentList.init(indexes.iterator(), indexes.size());
    }
}