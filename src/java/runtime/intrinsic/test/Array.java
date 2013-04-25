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

import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * Temporary FFI to mutable typed arrays, testing inevitability.
 * TODO redo using type classes
 *
 * @author Basil Hosmer
 */
public final class Array extends IntrinsicLambda
{
    public static final String NAME = "array";

    private static final TypeParam T = new TypeParam("T");
    private static final Type PARAM_TYPE = Types.tup(Types.INT, T);

    private static final Type tarray = runtime.intrinsic.demo.Array.INSTANCE.getType();
    public static final Type TYPE = Types.fun(PARAM_TYPE, Types.app(tarray, T));

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
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
