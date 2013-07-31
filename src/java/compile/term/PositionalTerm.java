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

import java.util.List;

/**
 * Common super for positional literal terms {@link ListTerm}
 * and {@link TupleTerm}.
 *
 * @author Basil Hosmer
 */
public abstract class PositionalTerm extends AbstractTypedTerm
{
    protected final List<Term> items;

    protected PositionalTerm(final Loc loc, final List<Term> items)
    {
        super(loc);
        this.items = items;
    }

    public final List<Term> getItems()
    {
        return items;
    }

    // Term

    public final boolean isConstant()
    {
        for (final Term item : items)
            if (!item.isConstant())
                return false;

        return true;
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PositionalTerm listTerm = (PositionalTerm)o;

        if (!items.equals(listTerm.items)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return items.hashCode();
    }
}
