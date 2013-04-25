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

import java.util.LinkedHashMap;

/**
 * Common superclass of literal terms representing keyed values,
 * i.e. {@link RecordTerm records} and {@link MapTerm maps}.
 *
 * @author Basil Hosmer
 */
public abstract class KeyedTerm extends AbstractTypedTerm
{
    protected final LinkedHashMap<Term, Term> items;

    protected KeyedTerm(final Loc loc, final LinkedHashMap<Term, Term> items)
    {
        super(loc);
        this.items = items;
    }

    public LinkedHashMap<Term, Term> getItems()
    {
        return items;
    }

    public boolean isConstant()
    {
        for (final Term item : items.values())
            if (!item.isConstant())
                return false;

        return true;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final KeyedTerm listTerm = (KeyedTerm)o;

        if (!items.equals(listTerm.items)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return items.hashCode();
    }
}