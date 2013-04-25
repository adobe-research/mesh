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

import processing.core.PApplet;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;


/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prendshape extends IntrinsicLambda
{
    public static final _prendshape INSTANCE = new _prendshape(); 
    public static final String NAME = "prendshape";

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
        if (Processing.INSTANCE != null)
        {
            Processing.INSTANCE.endShape(PApplet.CLOSE);
        }

        return Tuple.UNIT;
    }
}
