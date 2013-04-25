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
import processing.core.PMatrix3D;
import runtime.intrinsic.demo.Matrix3D;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class PrMatRotateX extends IntrinsicLambda
{
    public static final String NAME = "prmatrotatex";

    public static final Type TYPE =
        Types.fun(Types.tup(Matrix3D.INSTANCE.getType(), Types.DOUBLE), Matrix3D.INSTANCE.getType());

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
        final Tuple args = (Tuple)arg;
        return invoke(args.get(0), (Double)args.get(1));
    }

    public static PMatrix3D invoke(final Object arg0, final double angle)
    {
        final PMatrix3D mat = (PMatrix3D)arg0;

        mat.rotateX((float)angle);

        return mat;
    }
}
