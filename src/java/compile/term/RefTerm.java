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
 * Term representing a named reference to a binding.
 *
 * @author Basil Hosmer
 */
public final class RefTerm extends AbstractTerm
{
    private String name;
    private ValueBinding binding;

    public RefTerm(final Loc loc, final String name)
    {
        super(loc);
        this.name = name;
    }

    public RefTerm(final Loc loc, final RefTerm ref, final String name)
    {
        super(loc);
        this.name = ref.getName() + "." + name;
    }

    public String getName()
    {
        return name;
    }


    /**
     * used by {@link compile.analyze.BindingCollector}
     * when repointing inline param refs
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    public ValueBinding getBinding()
    {
        return binding;
    }

    public void setBinding(final ValueBinding binding)
    {
        this.binding = binding;
    }

    public Term deref()
    {
        if (!binding.isLet())
            return this;

        final LetBinding let = (LetBinding)binding;
        if (let.isIntrinsic())
            return this;

        final Term rhs = let.getValue();

        return rhs instanceof RefTerm ?
            ((RefTerm)rhs).deref() : rhs;
    }

    // Term

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Typed

    public Type getType()
    {
        return binding != null ? binding.getType() : null;
    }
    
    public void setType(final Type type)
    {
        assert false : "RefTerm.setType()";
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RefTerm that = (RefTerm)o;

        if (!that.getName().equals(name)) 
            return false;

        return binding == that.binding;

    }

    @Override
    public int hashCode()
    {
        return getName().hashCode();
    }
}
