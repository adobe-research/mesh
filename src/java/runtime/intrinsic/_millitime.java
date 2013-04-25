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
 * Return current time in millis.
 *
 * @author Basil Hosmer
 */
public final class _millitime extends IntrinsicLambda
{
    public static final _millitime INSTANCE = new _millitime(); 
    public static final String NAME = "millitime";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke();
    }

    public static long invoke()
    {
        return System.currentTimeMillis();
    }
}
