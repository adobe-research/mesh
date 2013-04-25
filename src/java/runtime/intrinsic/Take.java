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
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.Tuple;

/**
 * take(list, n) = first n if n > 0, last -n if n < 0.
 * n > list size wraps
 *
 * @author Basil Hosmer
 */
public final class Take extends IntrinsicLambda
{
    public static final String NAME = "take";

    private static final Type T = new TypeParam("T");
    private static final Type LIST_T = Types.list(T);
    public static final Type TYPE = Types.fun(Types.tup(Types.INT, LIST_T), LIST_T);

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
        return invoke((Integer)args.get(0), (ListValue)args.get(1));
    }

    public static ListValue invoke(final int n, final ListValue list)
    {
        final int size = list.size();

        if (size == 0)
        {
            return list;
        }
        else
        {
            final int extent = Math.abs(n);

            if (extent == size)
            {
                return list;
            }
            else if (extent < size)
            {
                // sublist cases
                return n >= 0 ? list.subList(0, n) : list.subList(size + n, size);
            }
            else
            {
                // rollover cases
                if (n >= 0)
                {
                    return PersistentList.init(Iterators.cycle(list), n);
                }
                else
                {
                    final PersistentList result = PersistentList.alloc(extent);

                    int ri = 0;
                    for (int i = n + 1; i <= 0; i++)
                        result.updateUnsafe(ri++, list.get(size + (i % size) - 1));

                    return result;
                }
            }
        }
    }
}