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

import runtime.rep.Tuple;
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

import java.util.concurrent.ConcurrentHashMap;

/**
 * transformer for binary functions, returns a version of the
 * given function vectorized over its right (second) argument.
 * E.g. given f : (X, Y) -> Z, returns a function (X, [Y]) -> [Z],
 * which returns the result of applying f(x, y) for each y.
 * The postfix attribute ~ on infix operators desugars to this,
 * e.g. for +, x +~ ys => eachright(+)(x, ys)
 *
 * @author Basil Hosmer
 */
public final class _eachright extends IntrinsicLambda
{
    // cache
    // TODO size limit
    private static final ConcurrentHashMap<Lambda, Lambda> POOL =
        new ConcurrentHashMap<Lambda, Lambda>();

    public static final _eachright INSTANCE = new _eachright();
    public static final String NAME = "eachright";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Lambda)arg);
    }

    public static Lambda invoke(final Lambda f)
    {
        final Lambda eached = POOL.get(f);

        if (eached != null)
            return eached;

        synchronized (POOL)
        {
            final Lambda oldEached = POOL.get(f);

            if (oldEached != null)
                return oldEached;

            final Lambda newEached = build(f);

            POOL.put(f, newEached);

            return newEached;
        }
    }

    private static Lambda build(final Lambda f)
    {
        return new Lambda()
        {
            public ListValue apply(final Object arg)
            {
                final Tuple args = (Tuple)arg;
                final Object left = args.get(0);
                final ListValue rights = (ListValue)args.get(1);

                final int size = rights.size();
                final PersistentList result = PersistentList.alloc(size);

                for (int i = 0; i < size; i++)
                    result.updateUnsafe(i, f.apply(Tuple.from(left, rights.get(i))));

                return result;
            }

            public String toString()
            {
                return NAME + "(" + f.toString() + ")";
            }
        };
    }
}
