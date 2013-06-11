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
package runtime.intrinsic.demo;

import runtime.intrinsic.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * Demo support.
 *
 * @author Basil Hosmer
 */
public final class _rgb2hsb extends IntrinsicLambda
{
    public static final _rgb2hsb INSTANCE = new _rgb2hsb(); 
    public static final String NAME = "rgb2hsb";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Integer)arg);
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Tuple invoke(final int rgb)
    {
        final int r = (rgb & 0xff0000) >> 16;
        final int g = (rgb & 0xff00) >> 8;
        final int b = (rgb & 0xff);
        final float[] hsb = RGBtoHSB(r, g, b, null);
        return Tuple.from((double)hsb[0], (double)hsb[1], (double)hsb[2]);
    }

    /**
     * java.awt.Color.RGBtoHSB
     */
    private static float[] RGBtoHSB(final int r, final int g, final int b,
        float[] hsbvals)
    {
        float hue;
        final float saturation;
        final float brightness;
        if (hsbvals == null)
        {
            hsbvals = new float[3];
        }
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        brightness = ((float)cmax) / 255.0f;
        if (cmax != 0)
            saturation = ((float)(cmax - cmin)) / ((float)cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else
        {
            final float redc = ((float)(cmax - r)) / ((float)(cmax - cmin));
            final float greenc = ((float)(cmax - g)) / ((float)(cmax - cmin));
            final float bluec = ((float)(cmax - b)) / ((float)(cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }
}
