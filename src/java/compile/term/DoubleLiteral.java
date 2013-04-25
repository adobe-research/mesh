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
 * Double literal term
 *
 * @author Basil Hosmer
 */
public final class DoubleLiteral extends SimpleLiteralTerm
{
    private final double value;

    public DoubleLiteral(final Loc loc, final double value)
    {
        super(loc);
        this.value = value;
    }

    public double getValue()
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
        return Types.DOUBLE;
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DoubleLiteral that = (DoubleLiteral)o;

        if (Double.compare(that.value, value) != 0) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        final long temp = value != +0.0d ? Double.doubleToLongBits(value) : 0L;
        return (int)(temp ^ (temp >>> 32));
    }
}