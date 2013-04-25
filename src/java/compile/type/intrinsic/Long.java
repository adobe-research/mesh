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

import compile.type.*;
import compile.type.IntrinsicType;

/**
 * 64-bit integer value
 */
public final class Long extends IntrinsicType
{
    public final static java.lang.String NAME = Long.class.getSimpleName();

    public final static Long INSTANCE = new Long();

    private Long()
    {
        super(NAME, new TypeCons(NAME));
    }
}
