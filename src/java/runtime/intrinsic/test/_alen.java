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
package runtime.intrinsic.test;

import runtime.intrinsic.demo.Array;
import runtime.rep.lambda.IntrinsicLambda;

/**
 * Temporary FFI to mutable typed arrays, testing inevitability.
 * TODO redo using type classes
 *
 * @author Basil Hosmer
 */
public final class _alen extends IntrinsicLambda
{
    public static final _alen INSTANCE = new _alen(); 
    public static final String NAME = "alen";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke(arg);
    }

    public static int invoke(final Object a)
    {
        if (a instanceof boolean[])
        {
            return ((boolean[])a).length;
        }
        else if (a instanceof int[])
        {
            return ((int[])a).length;
        }
        else if (a instanceof long[])
        {
            return ((long[])a).length;
        }
        else if (a instanceof double[])
        {
            return ((double[])a).length;
        }
        else
        {
            return ((Object[])a).length;
        }
    }
}
