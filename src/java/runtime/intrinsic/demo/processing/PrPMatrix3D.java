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

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class PrPMatrix3D extends IntrinsicLambda
{
    public static final String NAME = "prpmatrix3d";

    public static final Type TYPE = Types.fun(Types.unit(), Matrix3D.INSTANCE.getType());

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
        return invoke();
    }

    public static PMatrix3D invoke()
    {
        return new PMatrix3D();
    }
}
