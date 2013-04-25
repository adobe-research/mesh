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
import processing.core.PImage;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;
import runtime.intrinsic.demo.Image;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class PrImage extends IntrinsicLambda
{
    public static final String NAME = "primage";

    public static final Type TYPE =
        Types.fun(
            Types.tup(
                Image.INSTANCE.getType(),
                Types.DOUBLE,
                Types.DOUBLE,
                Types.DOUBLE,
                Types.DOUBLE),
            Types.unit());

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
        return invoke(args.get(0), (Double)args.get(1), (Double)args.get(2), (Double)args.get(3),
            (Double)args.get(4));
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Tuple invoke(final Object img, final double x, final double y,
        final double w, final double h)
    {
        if (Processing.INSTANCE != null)
        {
            Processing.INSTANCE.image((PImage)img,
                (float)x, (float)y, (float)w, (float)h);
        }

        return Tuple.UNIT;
    }
}
