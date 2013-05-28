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
 * integer log base 2
 *
 * @author Keith McGuigan
 */
public final class _ilog2 extends IntrinsicLambda
{
    public static final _ilog2 INSTANCE = new _ilog2();
    public static final String NAME = "ilog2";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Integer)arg);
    }

    public static int invoke(final int bits)
    {
        return bits < 0 ? 0 : (31 - Integer.numberOfLeadingZeros(bits));
    }
}
