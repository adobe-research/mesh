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
 * numeric negation
 *
 * @author Basil Hosmer
 */
public final class _fneg extends IntrinsicLambda
{
    public static final _fneg INSTANCE = new _fneg(); 
    public static final String NAME = "fneg";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Double)arg);
    }

    public static double invoke(final double d)
    {
        return -d;
    }
}
