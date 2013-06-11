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

import processing.core.PMatrix3D;
import runtime.intrinsic.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prmatmult extends IntrinsicLambda
{
    public static final _prmatmult INSTANCE = new _prmatmult(); 
    public static final String NAME = "prmatmult";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke(args.get(0), (Double)args.get(1), (Double)args.get(2),
            (Double)args.get(3));
    }

    public static Tuple invoke(final Object arg0,
        final double x, final double y, final double z)
    {
        final PMatrix3D mat = (PMatrix3D)arg0;

        final float[] in = new float[]{(float)x, (float)y, (float)z};
        final float[] out = new float[3];
        mat.mult(in, out);

        return Tuple.from((double)out[0], (double)out[1], (double)out[2]);
    }
}
