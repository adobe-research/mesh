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
 * zip over n-ary tuple of lists
 * Members:[*] => Tup(List @ Members) -> List(Tup(Members))
 *
 * @author Basil Hosmer
 */
public final class Zip extends IntrinsicLambda
{
    public static final String NAME = "zip";

    private static final TypeParam MEMBERS = new TypeParam("Ts", Kinds.STAR_LIST);
    private static final Type LIST_EACH_MEMBERS = Types.tmap(MEMBERS, Types.LIST);
    private static final Type TUP_MEMBER_LISTS = Types.app(Types.TUP, LIST_EACH_MEMBERS);
    private static final Type TUP_MEMBERS = Types.app(Types.TUP, MEMBERS);
    private static final Type LIST_MEMBER_TUPS = Types.list(TUP_MEMBERS);

    public static final Type TYPE = Types.fun(TUP_MEMBER_LISTS, LIST_MEMBER_TUPS);

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
        return invoke((Tuple)arg);
    }

    public static ListValue invoke(final Tuple lists)
    {
        final int wid = lists.size();

        // fast implementation of common case (pair of lists)
        if (wid == 2)
            return zip2((ListValue)lists.get(0), (ListValue)lists.get(1));

        // variable-width cases

        int size = 0;
        for (int i = 0; i < wid; i++)
        {
            final int listsize = ((ListValue)lists.get(i)).size();

            if (listsize == 0)
                return PersistentList.EMPTY;

            if (size < listsize)
                size = listsize;
        }

        final PersistentList result = PersistentList.alloc(size);

        final Iterator<?>[] iters = new Iterator<?>[wid];
        for (int j = 0; j < wid; j++)
            iters[j] = Iterators.cycle((ListValue)lists.get(j));

        for (int i = 0; i < size; i++)
        {
            final Object[] vals = new Object[wid];

            for (int j = 0; j < wid; j++)
                vals[j] = iters[j].next();

            result.updateUnsafe(i, Tuple.from(vals));
        }

        return result;
    }

    /**
     * Optimized version for list pairs.
     * TODO call this directly from CG where possible
     */
    public static ListValue zip2(final ListValue listx, final ListValue listy)
    {
        final int xsize = listx.size();
        final int ysize = listy.size();

        if (xsize == 0 || ysize == 0)
            return PersistentList.EMPTY;

        final Iterator<?> xiter = Iterators.cycle(listx);
        final Iterator<?> yiter = Iterators.cycle(listy);

        final int size = Math.max(xsize, ysize);
        final PersistentList listxy = PersistentList.alloc(size);

        for (int i = 0; i < size; i++)
            listxy.updateUnsafe(i, Tuple.from(xiter.next(), yiter.next()));

        return listxy;
    }
}