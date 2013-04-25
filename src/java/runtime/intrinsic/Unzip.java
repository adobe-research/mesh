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

import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import compile.type.kind.Kinds;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.Tuple;

import java.util.Iterator;

/**
 * the inverse of {@link Zip} (mod ragged lists)
 *
 * @author Basil Hosmer
 */
public final class Unzip extends IntrinsicLambda
{
    public static final String NAME = "unzip";

    private static final TypeParam MEMBERS = new TypeParam("Ts", Kinds.STAR_LIST);
    private static final Type LIST_EACH_MEMBERS = Types.tmap(MEMBERS, Types.LIST);
    private static final Type TUP_MEMBER_LISTS = Types.app(Types.TUP, LIST_EACH_MEMBERS);
    private static final Type TUP_MEMBERS = Types.app(Types.TUP, MEMBERS);
    private static final Type LIST_MEMBER_TUPS = Types.list(TUP_MEMBERS);

    public static final Type TYPE = Types.fun(LIST_MEMBER_TUPS, TUP_MEMBER_LISTS);

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