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
 * return current time in nanos
 *
 * @author Basil Hosmer
 */
public final class _nanotime extends IntrinsicLambda
{
    public static final _nanotime INSTANCE = new _nanotime(); 
    public static final String NAME = "nanotime";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke();
    }

    public static long invoke()
    {
        return System.nanoTime();
    }
}
