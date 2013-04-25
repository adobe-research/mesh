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

/**
 * Provides a way to force the type of an arbitrary term.
 * Completely unchecked, misuse will result in CG/runtime
 * errors.
 *
 * Currently used only to facilitate nominal type
 * constructors--see {@link TypeDef}.
 *
 * @author Basil Hosmer
 */
public final class CoerceTerm extends AbstractTypedTerm
{
    private Term term;

    public CoerceTerm(final Loc loc, final Term term, final Type type)
    {
        super(loc, type);
        this.term = term;
    }

    public Term getTerm()
    {
        return term;
    }

    // Term

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CoerceTerm that = (CoerceTerm)o;

        if (!term.equals(that.term)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = term.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
