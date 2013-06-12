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
 * intrinsic greater-than-or-equal over ints
 *
 * @author Basil Hosmer
 */
public final class _ge extends IntrinsicLambda
{
    public static final _ge INSTANCE = new _ge(); 
    public static final String NAME = "ge";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (Integer)args.get(1));
    }

    public static boolean invoke(final int arg0, final int arg1)
    {
        return arg0 >= arg1;
    }
}
