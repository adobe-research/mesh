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
 * Return number of available processors.
 *
 * @author Basil Hosmer
 */
public final class _availprocs extends IntrinsicLambda
{
    public static final _availprocs INSTANCE = new _availprocs(); 
    public static final String NAME = "availprocs";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke();
    }

    public static int invoke()
    {
        return Runtime.getRuntime().availableProcessors();
    }
}
