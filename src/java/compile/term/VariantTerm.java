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

/**
 * Term representing a variant literal expression.
 *
 * @author Basil Hosmer
 */
public final class VariantTerm extends AbstractTypedTerm
{
    private final Term key;
    private final Term value;

    public VariantTerm(final Loc loc, final Term key, final Term value)
    {
        super(loc);
        this.key = key;
        this.value = value;
    }

    public Term getKey()
    {
        return key;
    }

    public Term getValue()
    {
        return value;
    }

    // Term

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    public final boolean isConstant()
    {
        return key.isConstant() && value.isConstant();
    }

    // Object

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final VariantTerm that = (VariantTerm)o;

        if (!key.equals(that.key)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}