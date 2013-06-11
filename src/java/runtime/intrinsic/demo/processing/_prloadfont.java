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

import java.io.File;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prloadfont extends IntrinsicLambda
{
    public static final _prloadfont INSTANCE = new _prloadfont(); 
    public static final String NAME = "prloadfont";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((String)arg);
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Object invoke(final String path)
    {
        if (Processing.INSTANCE != null)
        {
            final File f = new File(path);
            if (f.exists())
                return Processing.INSTANCE.loadFont(f.getPath());
        }

        return null;
    }
}
