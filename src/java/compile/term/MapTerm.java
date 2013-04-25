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

import java.util.LinkedHashMap;

/**
 * Term representing a map literal expression.
 *
 * @author Basil Hosmer
 */
public final class MapTerm extends KeyedTerm
{
    public MapTerm(final Loc loc, final LinkedHashMap<Term, Term> items)
    {
        super(loc, items);
    }

    // Term

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Typed

}
