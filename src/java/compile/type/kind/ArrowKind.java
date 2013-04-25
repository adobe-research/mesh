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
 * Arrow-kinded types are type constructors.
 *
 * @author Basil Hosmer
 */
public final class ArrowKind extends Kind
{
    public final Kind paramKind;
    public final Kind resultKind;

    public ArrowKind(final Loc loc, final Kind paramKind, final Kind resultKind)
    {
        super(loc);
        this.paramKind = paramKind;
        this.resultKind = resultKind;
    }

    public ArrowKind(final Kind paramKind, final Kind resultKind)
    {
        this(Loc.INTRINSIC, paramKind, resultKind);
    }

    public Kind getParamKind()
    {
        return paramKind;
    }

    public Kind getResultKind()
    {
        return resultKind;
    }
    
    // Dumpable
    
    public String dump()
    {
        return paramKind.dump() + " ~> " + resultKind.dump();
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ArrowKind that = (ArrowKind)o;

        if (!paramKind.equals(that.paramKind)) return false;
        if (!resultKind.equals(that.resultKind)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = paramKind.hashCode();
        result = 31 * result + resultKind.hashCode();
        return result;
    }
}
