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

import compile.type.Type;
import compile.type.Types;
import processing.core.PFont;
import runtime.intrinsic.demo.Font;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class PrTextFont extends IntrinsicLambda
{
    public static final String NAME = "prtextfont";

    public static final Type TYPE = Types.fun(Font.INSTANCE.getType(), Types.unit());

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
        return invoke(arg);
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Tuple invoke(final Object arg)
    {
        if (Processing.INSTANCE != null)
        {
            final PFont font = (PFont)arg;
            if (font != null)
                Processing.INSTANCE.textFont(font);
        }

        return Tuple.UNIT;
    }
}
