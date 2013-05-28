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

        // fast implementation of common case (pair of lists)
        if (wid == 2)
            return invoke2((ListValue)lists.get(0), (ListValue)lists.get(1), func);

        int size = 0;
        boolean ragged = false;

        if (wid > 0)
        {
            size = ((ListValue)lists.get(0)).size();

            // early bailout--any empty component list yields empty zip
            if (size == 0)
                return PersistentList.EMPTY;

            for (int i = 1; i < wid; i++)
            {
                final int listsize = ((ListValue)lists.get(i)).size();

                // early bailout--any empty component list yields empty zip
                if (listsize == 0)
                    return PersistentList.EMPTY;

                if (listsize != size)
                {
                    // ragged lists must use a slower iterator
                    ragged = true;

                    // size of zipped list is size of longest list
                    size = Math.max(size, listsize);
                }
            }
        }

        final PersistentList result = PersistentList.alloc(size);

        final Iterator<?>[] iters = new Iterator<?>[wid];
        for (int j = 0; j < wid; j++)
        {
            final ListValue list = (ListValue)lists.get(j);
            iters[j] = ragged ? Iterators.cycle(list) : list.iterator();
        }

        for (int i = 0; i < size; i++)
        {
            final Object[] vals = new Object[wid];

            for (int j = 0; j < wid; j++)
                vals[j] = iters[j].next();

            result.updateUnsafe(i, func.apply(Tuple.from(vals)));
        }

        return result;
    }

    public static ListValue invoke2(
        final ListValue listx, final ListValue listy, final Lambda func)
    {
        final int xsize = listx.size();
        final int ysize = listy.size();

        if (xsize == 0 || ysize == 0)
            return PersistentList.EMPTY;

        final Iterator<?> xiter, yiter;
        final int size;
        if (xsize == ysize)
        {
            xiter = listx.iterator();
            yiter = listy.iterator();
            size = xsize;
        }
        else
        {
            xiter = Iterators.cycle(listx);
            yiter = Iterators.cycle(listy);
            size = Math.max(xsize, ysize);
        }

        final PersistentList result = PersistentList.alloc(size);

        for (int i = 0; i < size; i++)
            result.updateUnsafe(i, func.apply(Tuple.from(xiter.next(), yiter.next())));

        return result;
    }
}
