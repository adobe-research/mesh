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
package runtime.intrinsic.demo.processing;

import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prcolor extends IntrinsicLambda
{
    public static final _prcolor INSTANCE = new _prcolor(); 
    public static final String NAME = "prcolor";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (Integer)args.get(1), (Integer)args.get(2),
            (Integer)args.get(3));
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static int invoke(final int x, final int y, final int z,
        final int a)
    {
        if (Processing.INSTANCE != null)
        {
            return Processing.INSTANCE.color(x, y, z, a);
        }

        return 0;
    }
}
