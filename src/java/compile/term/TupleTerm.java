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
import compile.type.Types;

import java.util.Arrays;
import java.util.List;

/**
 * Term representing a tuple literal expression.
 *
 * @author Basil Hosmer
 */
public final class TupleTerm extends PositionalTerm
{
    public static final TupleTerm UNIT = new TupleTerm(Loc.INTRINSIC);

    static
    {
        UNIT.setType(Types.unit());
    }

    public TupleTerm(final Loc loc, final List<Term> items)
    {
        super(loc, items);
    }

    public TupleTerm(final Loc loc, final Term... items)
    {
        this(loc, Arrays.asList(items));
    }

    // Term

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }
}