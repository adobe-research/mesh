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
 * intrinsic div() - adds two ints
 *
 * @author Basil Hosmer
 */
public final class _div extends IntrinsicLambda
{
    public static final _div INSTANCE = new _div(); 
    public static final String NAME = "div";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (Integer)args.get(1));
    }

    public static int invoke(final int n, final int d)
    {
        return n / d;
    }
}
