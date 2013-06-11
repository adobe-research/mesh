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

import runtime.intrinsic.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prstroke extends IntrinsicLambda
{
    public static final _prstroke INSTANCE = new _prstroke(); 
    public static final String NAME = "prstroke";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (Integer)args.get(1), (Integer)args.get(2));
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Tuple invoke(final int r, final int g, final int b)
    {
        if (Processing.INSTANCE != null)
        {
            Processing.INSTANCE.stroke(r, g, b);
        }

        return Tuple.UNIT;
    }
}
