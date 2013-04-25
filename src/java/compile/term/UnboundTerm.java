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
import compile.type.Type;

/**
 * Unbound terms are the term representation of expressions
 * used as statements, e.g. print("hey");
 *
 * @author Basil Hosmer
 */
public final class UnboundTerm implements ValueStatement
{
    private Term value;

    public UnboundTerm(final Term value)
    {
        this.value = value;
    }

    /**
     * Used by optimizers like {@link compile.analyze.ConstantReducer}.
     * Caller is on the honor system to replace like for like.
     */
    public void setValue(final Term value)
    {
        this.value = value;
    }

    // ValueStatement

    public Term getValue()
    {
        return value;
    }

    // Typed

    public Type getType()
    {
        return value.getType();
    }

    public final void setType(final Type type)
    {
        throw new UnsupportedOperationException("UnboundTerm.setType()");
    }

    public boolean hasDeclaredType()
    {
        return false;
    }

    public Type getDeclaredType()
    {
        return null;
    }

    public final void setDeclaredType(final Type type)
    {
        throw new UnsupportedOperationException("UnboundTerm.setDeclaredType()");
    }

    // Statement

    public boolean isBinding()
    {
        return false;
    }

    // Dumpable

    public String dump()
    {
        return value.dump();
    }

    // Located

    public Loc getLoc()
    {
        return value.getLoc();
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final UnboundTerm that = (UnboundTerm)o;

        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return value.hashCode();
    }
}
