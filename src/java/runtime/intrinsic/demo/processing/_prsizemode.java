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
import runtime.rep.Symbol;
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prsizemode extends IntrinsicLambda
{
    public static final _prsizemode INSTANCE = new _prsizemode(); 
    public static final String NAME = "prsizemode";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (Integer)args.get(1), (Symbol)args.get(2));
    }

    // POINTS, LINES, TRIANGLES, TRIANGLE_FAN, TRIANGLE_STRIP, QUADS, and QUAD_STRIP
    private static final Symbol P2D = Symbol.get("P2D");
    private static final Symbol P3D = Symbol.get("P3D");
    private static final Symbol JAVA2D = Symbol.get("JAVA2D");
    private static final Symbol OPENGL = Symbol.get("OPENGL");

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Tuple invoke(final int w, final int h, final Symbol mode)
    {
        if (Processing.INSTANCE != null)
        {
            Processing.INSTANCE.size(w, h,
                mode.equals(P2D) ? Processing.P2D :
                    mode.equals(P3D) ? Processing.P3D :
                        mode.equals(OPENGL) ? Processing.OPENGL :
                            Processing.JAVA2D);
        }

        return Tuple.UNIT;
    }
}
