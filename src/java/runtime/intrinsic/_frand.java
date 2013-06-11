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
 * frand()
 *
 * @author Basil Hosmer
 */
public final class _frand extends IntrinsicLambda
{
    public static final _frand INSTANCE = new _frand(); 
    public static final String NAME = "frand";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke();
    }

    public static double invoke()
    {
        return Math.random();
    }
}
