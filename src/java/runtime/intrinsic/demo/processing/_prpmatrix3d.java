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

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prpmatrix3d extends IntrinsicLambda
{
    public static final _prpmatrix3d INSTANCE = new _prpmatrix3d(); 
    public static final String NAME = "prpmatrix3d";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke();
    }

    public static Object invoke()
    {
        return new PMatrix3D();
    }
}
