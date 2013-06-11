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
 * Evolve (reduce) until stop condition is met, or inputs
 * are exhausted.
 *
 * @author Basil Hosmer
 */
public final class _evolve_while extends IntrinsicLambda
{
    public static final _evolve_while INSTANCE = new _evolve_while(); 
    public static final String NAME = "evolve_while";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke(
            (Lambda)args.get(0),
            args.get(1),
            (Lambda)args.get(2),
            (ListValue)args.get(3));
    }

    public static Object invoke(
        final Lambda pred,
        final Object init,
        final Lambda f,
        final ListValue list)
    {
        Object result = init;

        for (final Object item : list)
        {
            if (!(Boolean)pred.apply(result))
                break;

            result = f.apply(Tuple.from(result, item));
        }

        return result;
    }
}
