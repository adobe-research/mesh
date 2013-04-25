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
public final class _prmouse extends IntrinsicLambda
{
    public static final _prmouse INSTANCE = new _prmouse(); 
    public static final String NAME = "prmouse";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke();
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Tuple invoke()
    {
        final int x, y;

        if (Processing.INSTANCE != null)
        {
            x = Processing.INSTANCE.mouseX;
            y = Processing.INSTANCE.mouseY;
        }
        else
        {
            x = y = 0;
        }

        return Tuple.from(x, y);
    }
}
