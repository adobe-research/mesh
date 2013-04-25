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
import runtime.rep.Tuple;

/**
 * Temporary FFI to mutable typed arrays, testing inevitability.
 * TODO redo using type classes
 *
 * @author Basil Hosmer
 */
public final class _aget extends IntrinsicLambda
{
    public static final _aget INSTANCE = new _aget(); 
    public static final String NAME = "aget";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke(args.get(0), (Integer)args.get(1));
    }

    public static Object invoke(final Object a, final int i)
    {
        if (a instanceof boolean[])
        {
            return ((boolean[])a)[i];
        }
        else if (a instanceof int[])
        {
            return ((int[])a)[i];
        }
        else if (a instanceof long[])
        {
            return ((long[])a)[i];
        }
        else if (a instanceof double[])
        {
            return ((double[])a)[i];
        }
        else
        {
            return ((Object[])a)[i];
        }
    }
}
