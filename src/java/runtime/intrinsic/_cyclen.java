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

import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;

/**
 * Like cycle, but with an additional upper limit on
 * the number of times function is called.
 *
 * @author Basil Hosmer
 */
public final class _cyclen extends IntrinsicLambda
{
    public static final _cyclen INSTANCE = new _cyclen(); 
    public static final String NAME = "cyclen";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke(args.get(0), (Integer)args.get(1), (Lambda)args.get(2));
    }

    public static Object invoke(final Object init, final int n, final Lambda f)
    {
        Object result = init;

        for (int i = 0; i < n; i++)
            result = f.apply(result);

        return result;
    }
}
