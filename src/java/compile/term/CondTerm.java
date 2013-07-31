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
 * Term representing a conditional selection expression.
 *
 * @author Basil Hosmer
 */
public final class CondTerm extends AbstractTypedTerm
{
    private final Term sel;
    private final Term cases;

    public CondTerm(final Loc loc, final Term sel, final Term cases)
    {
        super(loc);
        this.sel = sel;
        this.cases = cases;
    }

    public Term getSel()
    {
        return sel;
    }

    public Term getCases()
    {
        return cases;
    }

    // Term

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    public final boolean isConstant()
    {
        return sel.isConstant() && cases.isConstant();
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CondTerm that = (CondTerm)o;

        if (!sel.equals(that.sel)) return false;
        if (!cases.equals(that.cases)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = sel.hashCode();
        result = 31 * result + cases.hashCode();
        return result;
    }
}