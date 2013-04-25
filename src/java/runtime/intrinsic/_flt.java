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
import runtime.rep.Tuple;

/**
 * intrinsic less-than over floats
 *
 * @author Basil Hosmer
 */
public final class _flt extends IntrinsicLambda
{
    public static final _flt INSTANCE = new _flt(); 
    public static final String NAME = "flt";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Double)args.get(0), (Double)args.get(1));
    }

    public static boolean invoke(final double x, final double y)
    {
        return x < y;
    }
}
