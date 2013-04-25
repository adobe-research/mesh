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
package runtime.intrinsic.tran;

import runtime.rep.lambda.IntrinsicLambda;
import runtime.tran.Box;

/**
 * Create a box with an initial value.
 *
 * @author Basil Hosmer
 */
public final class _box extends IntrinsicLambda
{
    public static final _box INSTANCE = new _box(); 
    public static final String NAME = "box";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke(arg);
    }

    public static Box invoke(final Object value)
    {
        return new Box(value);
    }
}
