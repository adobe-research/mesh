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
 * rand(max) returns an integer between 0 and max - 1
 *
 * @author Basil Hosmer
 */
public final class _rand extends IntrinsicLambda
{
    public static final _rand INSTANCE = new _rand(); 
    public static final String NAME = "rand";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Integer)arg);
    }

    public static int invoke(final int max)
    {
        return (int)Math.floor(Math.random() * max);
    }
}
