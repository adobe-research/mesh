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
package compile.type.kind;

import compile.Dumpable;
import compile.Located;
import compile.Loc;

/**
 * Common super for kinds. Kinds classify types.
 *
 * @author Basil Hosmer
 */
public abstract class Kind implements Located, Dumpable
{
    public final Loc loc;

    public Kind(final Loc loc)
    {
        this.loc = loc;
    }

    public Loc getLoc()
    {
        return loc;
    }
}
