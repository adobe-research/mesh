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
 * string length
 * TODO should be an overload of size(), or vice versa
 *
 * @author Basil Hosmer
 */
public final class _strlen extends IntrinsicLambda
{
    public static final _strlen INSTANCE = new _strlen(); 
    public static final String NAME = "strlen";

    public String getName()
    {
        return NAME;
    }

    public final Integer apply(final Object arg)
    {
        return invoke((String)arg);
    }

    public static int invoke(final String s)
    {
        return s.length();
    }
}
