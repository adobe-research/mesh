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
 * Font - opaque type for Processing PFont
 */
public final class Font extends IntrinsicType
{
    public final static String NAME = Font.class.getSimpleName();

    public final static Font INSTANCE = new Font();

    private Font()
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
