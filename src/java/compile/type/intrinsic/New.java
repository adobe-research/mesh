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
package compile.type.intrinsic;

import compile.type.TypeCons;
import compile.type.kind.Kinds;
import compile.type.IntrinsicType;

/**
 *
 */
public final class New extends IntrinsicType
{
    public final static java.lang.String NAME = New.class.getSimpleName();

    public final static New INSTANCE = new New();

    private New()
    {
        super(NAME, new TypeCons(NAME, Kinds.UNARY_CONS));
    }
}
