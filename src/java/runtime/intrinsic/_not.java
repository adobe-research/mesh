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
 * boolean not()
 *
 * @author Basil Hosmer
 */
public final class _not extends IntrinsicLambda
{
    public static final _not INSTANCE = new _not(); 
    public static final String NAME = "not";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Boolean)arg);
    }

    public static boolean invoke(final boolean b)
    {
        return !b;
    }
}
