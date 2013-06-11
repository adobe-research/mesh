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
 * int to string
 *
 * @author Basil Hosmer
 */
public final class _i2s extends IntrinsicLambda
{
    public static final _i2s INSTANCE = new _i2s(); 
    public static final String NAME = "i2s";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Integer)arg);
    }

    public static String invoke(final int i)
    {
        return String.valueOf(i);
    }
}
