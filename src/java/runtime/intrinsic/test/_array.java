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

import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * Temporary FFI to mutable typed arrays, testing inevitability.
 * TODO redo using type classes
 *
 * @author Basil Hosmer
 */
public final class _array extends IntrinsicLambda
{
    public static final _array INSTANCE = new _array(); 
    public static final String NAME = "array";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), args.get(1));
    }

    public static Object invoke(final int len, final Object val)
    {
        if (val instanceof Boolean)
        {
            final boolean a[] = new boolean[len];
            final boolean v = (Boolean)val;
            for (int i = 0; i < len; i++)
                a[i] = v;
            return a;
        }
        else if (val instanceof Integer)
        {
            final int a[] = new int[len];
            final int v = (Integer)val;
            for (int i = 0; i < len; i++)
                a[i] = v;
            return a;
        }
        else if (val instanceof Long)
        {
            final long a[] = new long[len];
            final long v = (Long)val;
            for (int i = 0; i < len; i++)
                a[i] = v;
            return a;
        }
        else if (val instanceof Double)
        {
            final double a[] = new double[len];
            final double v = (Double)val;
            for (int i = 0; i < len; i++)
                a[i] = v;
            return a;
        }
        else
        {
            final Object a[] = new Object[len];
            for (int i = 0; i < len; i++)
                a[i] = val;
            return a;
        }
    }
}
