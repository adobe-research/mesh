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
public final class _mapz extends IntrinsicLambda
{
    public static final _mapz INSTANCE = new _mapz(); 
    public static final String NAME = "mapz";

    public String getName()
    {
        return NAME;
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
