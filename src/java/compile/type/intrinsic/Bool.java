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
 * true/false boolean
 */
public final class Bool extends IntrinsicType
{
    public final static java.lang.String NAME = Bool.class.getSimpleName();

    public final static Bool INSTANCE = new Bool();

    private Bool() 
    {
        super(NAME, new TypeCons(NAME));
    }
}
