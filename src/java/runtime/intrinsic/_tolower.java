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
 * string tolower
 *
 * @author Basil Hosmer
 */
public final class _tolower extends IntrinsicLambda
{
    public static final _tolower INSTANCE = new _tolower(); 
    public static final String NAME = "tolower";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((String)arg);
    }

    public static String invoke(final String s)
    {
        return s.toLowerCase();
    }
}
