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

import compile.Loc;

/**
 * Used for kind inference. Status in flux.
 *
 * @author Basil Hosmer
 */
public final class KindVar extends Kind
{
    private final String name;
    
    public KindVar(final Loc loc, final String name)
    {
        super(loc);
        this.name = name;
    }

    public KindVar(final String name)
    {
        this(Loc.INTRINSIC, name);
    }

    public String getName()
    {
        return name;
    }
    
    // Dumpable

    public String dump()
    {
        return name;
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final KindVar kindVar = (KindVar)o;

        if (!name.equals(kindVar.name)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
