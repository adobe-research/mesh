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

import compile.Loc;
import compile.type.Type;
import compile.type.Types;

import java.util.Collections;

/**
 * Unit type -- synonym for 0-tuple
 */
public final class Unit
{
    public final static Type INSTANCE =
        Types.tup(Loc.INTRINSIC, Collections.<Type>emptyList());
}
