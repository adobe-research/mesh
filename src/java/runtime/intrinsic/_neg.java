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
 * int negation
 *
 * @author Basil Hosmer
 */
public final class _neg extends IntrinsicLambda
{
    public static final _neg INSTANCE = new _neg(); 
    public static final String NAME = "neg";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Integer)arg);
    }

    public static int invoke(final int n)
    {
        return -n;
    }
}
