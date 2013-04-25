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
import runtime.rep.Symbol;
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prbeginshapemode extends IntrinsicLambda
{
    public static final _prbeginshapemode INSTANCE = new _prbeginshapemode(); 
    public static final String NAME = "prbeginshapemode";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Symbol)arg);
    }

    // POINTS, LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
    private static final Symbol POINTS = Symbol.get("POINTS");
    private static final Symbol LINES = Symbol.get("LINES");
    private static final Symbol TRIANGLES = Symbol.get("TRIANGLES");
    private static final Symbol TRIANGLE_FAN = Symbol.get("TRIANGLE_FAN");
    private static final Symbol TRIANGLE_STRIP = Symbol.get("TRIANGLE_STRIP");
    private static final Symbol QUADS = Symbol.get("QUADS");
    private static final Symbol QUAD_STRIP = Symbol.get("QUAD_STRIP");

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Tuple invoke(final Symbol sym)
    {
        if (Processing.INSTANCE != null)
        {
            Processing.INSTANCE.beginShape(
                sym.equals(POINTS) ? Processing.POINTS :
                sym.equals(LINES) ? Processing.LINES :
                sym.equals(TRIANGLES) ? Processing.TRIANGLES :
                sym.equals(TRIANGLE_FAN) ? Processing.TRIANGLE_FAN :
                sym.equals(TRIANGLE_STRIP) ? Processing.TRIANGLE_STRIP :
                sym.equals(QUADS) ? Processing.QUADS :
                sym.equals(QUAD_STRIP) ? Processing.QUAD_STRIP :
                Processing.POLYGON);
        }

        return Tuple.UNIT;
    }
}
