
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

import compile.type.*;
import compile.type.intrinsic.Opaque;
import compile.type.kind.Kinds;

/**
 * Matrix3D - opaque type for Processing PMatrix3D
 */
public final class Matrix3D extends IntrinsicType
{
    public final static String NAME = Matrix3D.class.getSimpleName();

    public final static Matrix3D INSTANCE = new Matrix3D();

    private Matrix3D()
    {
        super(NAME, initType());
    }

    private static TypeApp initType()
    {
        final TypeApp app = Types.newType(Opaque.INSTANCE.getType());
        app.setKind(Kinds.STAR);
        return app;
    }
}
