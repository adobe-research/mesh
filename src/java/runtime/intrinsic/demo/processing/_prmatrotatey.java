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
public final class _prmatrotatey extends IntrinsicLambda
{
    public static final _prmatrotatey INSTANCE = new _prmatrotatey(); 
    public static final String NAME = "prmatrotatey";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke(args.get(0), (Double)args.get(1));
    }

    public static Object invoke(final Object arg0, final double angle)
    {
        final PMatrix3D mat = (PMatrix3D)arg0;

        mat.rotateY((float)angle);

        return mat;
    }
}
