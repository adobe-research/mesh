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

/**
 * exp()
 *
 * @author Basil Hosmer
 */
public final class _exp extends IntrinsicLambda
{
    public static final _exp INSTANCE = new _exp(); 
    public static final String NAME = "exp";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Double)arg);
    }

    public static double invoke(final double arg)
    {
        return Math.exp(arg);
    }
}
