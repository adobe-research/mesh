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

/**
 * intrinsic greater-than over floats
 *
 * @author Basil Hosmer
 */
public final class _fgt extends IntrinsicLambda
{
    public static final _fgt INSTANCE = new _fgt(); 
    public static final String NAME = "fgt";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Double)args.get(0), (Double)args.get(1));
    }

    public static boolean invoke(final double arg0, final double arg1)
    {
        return arg0 > arg1;
    }
}
