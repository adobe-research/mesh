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
 * bool to int
 *
 * @author Basil Hosmer
 */
public final class _b2i extends IntrinsicLambda
{
    public static final _b2i INSTANCE = new _b2i(); 
    public static final String NAME = "b2i";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Boolean)arg);
    }

    public static int invoke(final boolean b)
    {
        return b ? 1 : 0;
    }
}
