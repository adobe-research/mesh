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
import runtime.rep.lambda.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.Tuple;

import java.util.Iterator;

/**
 * mapz(lists, f) == map(zip(lists), f), but doesn't
 * create the intermediate list of tuples.
 * TODO experimental, decide in or out.
 *
 * @author Basil Hosmer
 */
public final class Mapz extends IntrinsicLambda
{
    public static final String NAME = "mapz";

    // T.. => ((T..)->X, ([T]..)) -> [X]
    private static final TypeParam MEMBERS = new TypeParam("T", Kinds.STAR_LIST);

    private static final Type TUP_MEMBERS = Types.app(Types.TUP, MEMBERS);

    private static final TypeParam X = new TypeParam("X", Kinds.STAR);

    private static final Type FUN_TUP_MEMBERS_X = Types.fun(TUP_MEMBERS, X);

    private static final Type LIST_EACH_MEMBERS = Types.tmap(MEMBERS, Types.LIST);

    // TODO had Types.tup(LIST_EACH_MEMBERS) by mistake, but no kind error, why?
    private static final Type TUP_LISTS = Types.app(Types.TUP, LIST_EACH_MEMBERS);

    private static final Type LIST_X = Types.list(X);

    public static final Type TYPE =
        Types.fun(Types.tup(TUP_LISTS, FUN_TUP_MEMBERS_X), LIST_X);

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
        return invoke((Tuple)args.get(0), (Lambda)args.get(1));
    }

    public static ListValue invoke(final Tuple lists, final Lambda func)
    {
        final int wid = lists.size();

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

            result.updateUnsafe(i, func.apply(Tuple.from(vals)));
        }

        return result;
    }
}