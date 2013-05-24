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
 * int min()
 *
 * @author Keith McGuigan
 */
public final class _log2 extends IntrinsicLambda
{
    public static final _log2 INSTANCE = new _log2(); 
    public static final String NAME = "log2";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Integer)arg);
    }

    public static int invoke(int bits)
    {
        int log = 0;
        int tstval = 0x00010000;
        int factor = 16;

        while (factor > 0) 
        {
            if (bits >= tstval)
            {
                bits >>>= factor;
                log += factor;
            }
            factor >>>= 1;
            tstval >>>= factor;
        }
        return log;
    }
}
