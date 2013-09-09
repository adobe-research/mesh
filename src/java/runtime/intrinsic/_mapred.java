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

import runtime.rep.Lambda;
import runtime.rep.Tuple;
import runtime.rep.list.ListValue;

/**
 * map-reduce. mapred(r, i, args, f) produces the
 * same result as reduce(r, i, args | f), without
 * generating the intermediate list.
 *
 * @author Basil Hosmer
 */
public final class _mapred extends IntrinsicLambda
{
    public static final _mapred INSTANCE = new _mapred();
    public static final String NAME = "mapred";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Lambda)args.get(0), args.get(1),
            (ListValue)args.get(2), (Lambda)args.get(3));
    }

    public static Object invoke(final Lambda r, final Object init,
        final ListValue args, final Lambda f)
    {
        Object result = init;

        for (final Object arg : args)
            result = r.apply(Tuple.from(result, f.apply(arg)));

        return result;
    }
}
