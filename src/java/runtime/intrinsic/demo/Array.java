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
import compile.type.kind.Kinds;
import compile.type.IntrinsicType;

/**
 * Array - generic type for intrinsics that take and return java Arrays
 */
public final class Array extends IntrinsicType
{
    public final static String NAME = Array.class.getSimpleName();

    public final static Array INSTANCE = new Array();

    private Array()
    {
        super(NAME, new TypeCons(NAME, Kinds.UNARY_CONS));
    }
}
