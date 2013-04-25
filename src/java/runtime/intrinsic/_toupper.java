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
 * string toupper
 *
 * @author Basil Hosmer
 */
public final class _toupper extends IntrinsicLambda
{
    public static final _toupper INSTANCE = new _toupper(); 
    public static final String NAME = "toupper";

    public String getName()
    {
        return NAME;
    }

    public final String apply(final Object arg)
    {
        return invoke((String)arg);
    }

    public static String invoke(final String s)
    {
        return s.toUpperCase();
    }
}
