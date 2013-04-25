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

import processing.core.PImage;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.intrinsic.demo.Image;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _primageheight extends IntrinsicLambda
{
    public static final _primageheight INSTANCE = new _primageheight(); 
    public static final String NAME = "primageheight";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke(arg);
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static int invoke(final Object arg)
    {
        return ((PImage)arg).height;
    }
}
