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
 * long to string
 *
 * @author Basil Hosmer
 */
public final class _l2s extends IntrinsicLambda
{
    public static final _l2s INSTANCE = new _l2s(); 
    public static final String NAME = "l2s";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Long)arg);
    }

    public static String invoke(final long l)
    {
        return String.valueOf(l);
    }
}
