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
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;

/**
 * reduce
 *
 * @author Basil Hosmer
 */
public final class _reduce extends IntrinsicLambda
{
    public static final _reduce INSTANCE = new _reduce(); 
    public static final String NAME = "reduce";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Lambda)args.get(0), args.get(1), (ListValue)args.get(2));
    }

    public static Object invoke(final Lambda f, final Object init, final ListValue list)
    {
        Object result = init;

        for (final Object item : list)
            result = f.apply(Tuple.from(result, item));

        return result;
    }
}
