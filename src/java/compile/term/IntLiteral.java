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
package compile.term;

import compile.Loc;
import compile.term.visit.TermVisitor;
import compile.type.Type;
import compile.type.Types;

/**
 * Int literal term
 *
 * @author Basil Hosmer
 */
public final class IntLiteral extends SimpleLiteralTerm
{
    private final int value;

    public IntLiteral(final Loc loc, final int value)
    {
        super(loc);
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    // Term

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Typed

    public Type getType()
    {
        return Types.INT;
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final IntLiteral that = (IntLiteral)o;

        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return value;
    }
}
