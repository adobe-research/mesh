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

/**
 * Gets environment variable values
 *
 * @author Keith McGuigan
 */
public final class _getenv extends IntrinsicLambda
{
    public static final _getenv INSTANCE = new _getenv();
    public static final String NAME = "getenv";

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
        final String value = System.getenv(s);
        // TOOD: Use variant to indicate null vs. empty value?
        return value == null ? "" : value;
    }
}
