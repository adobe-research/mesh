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
import runtime.intrinsic.demo.Matrix3D;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prmattranslate extends IntrinsicLambda
{
    public static final _prmattranslate INSTANCE = new _prmattranslate(); 
    public static final String NAME = "prmattranslate";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke(args.get(0), (Double)args.get(1), (Double)args.get(2), (Double)args.get(3));
    }

    public static Object invoke(final Object arg0, final double x, final double y,
        final double z)
    {
        final PMatrix3D mat = (PMatrix3D)arg0;

        mat.translate((float)x, (float)y, (float)z);

        return mat;
    }
}
