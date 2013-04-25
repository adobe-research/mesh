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
import runtime.intrinsic.demo.Image;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class PrLoadImage extends IntrinsicLambda
{
    public static final String NAME = "prloadimage";

    public static final Type TYPE = Types.fun(Types.STRING, Image.INSTANCE.getType());

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
        return invoke((String)arg);
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static PImage invoke(final String path)
    {
        return Processing.INSTANCE != null ?
            Processing.INSTANCE.loadImage(path) :
            null;
    }
}
