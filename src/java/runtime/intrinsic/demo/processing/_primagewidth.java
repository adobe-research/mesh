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
import runtime.intrinsic.IntrinsicLambda;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _primagewidth extends IntrinsicLambda
{
    public static final _primagewidth INSTANCE = new _primagewidth(); 
    public static final String NAME = "primagewidth";

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
        return ((PImage)arg).width;
    }
}
