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
import compile.module.Scope;
import compile.term.visit.BindingVisitor;
import compile.type.Type;

/**
 * Instances correspond directly to simple (non-decomposing) lets
 * in the surface language. Decomposing lets are currently desugared
 * into simple lets at parse time.
 *
 * @author Basil Hosmer
 */
public final class LetBinding extends ValueBinding
{
    /**
     * arbitrary, fixed-identity dummy RHS for intrinsics
     */
    private static final Term INTRINSIC_VALUE = new TupleTerm(Loc.INTRINSIC);

    //
    // instance
    //

    private Scope scope;
    private Term value;

    /**
     * after type checking, this will be the same as value.getType()
     * when value is non-null.
     */
    private Type type;

    /**
     *
     */
    public LetBinding(final Loc loc, final String name,
        final Type declaredType, final Term value)
    {
        super(loc, name, declaredType);
        this.value = value;

        if (value == INTRINSIC_VALUE)
        {
            assert declaredType != null;
            this.type = declaredType;
        }
        else
        {
            // most of the time rhs type is null at this point,
            // but not always.
            this.type = value.getType();
        }
    }

    /**
     * implicitly typed
     */
    public LetBinding(final Loc loc, final String name, final Term value)
    {
        this(loc, name, null, value);
    }

    /**
     * intrinsics
     */
    public LetBinding(final Loc loc, final String name, final Type declaredType)
    {
        this(loc, name, declaredType, INTRINSIC_VALUE);
    }

    public boolean isIntrinsic()
    {
        return value == INTRINSIC_VALUE;
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

    // Binding

    public boolean isLet()
    {
        return true;
    }

    public Scope getScope()
    {
        return scope;
    }

    public void setScope(final Scope scope)
    {
        this.scope = scope;
    }

    // Typed

    public Type getType()
    {
        return type;
    }

    public void setType(final Type type)
    {
        this.type = type;
    }

    // Statement

    public String dump()
    {
        return dump(this.name);
    }

    public String dump(final String name) 
    {
        final StringBuilder buf = new StringBuilder(name);

        if (hasDeclaredType())
            buf.append(" : ").append(declaredType.dump());

        buf.append(" = ");

        buf.append(isIntrinsic() ? "<intrinsic>" : value.dump());

        return buf.toString();
    }

    public <T> T accept(final BindingVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LetBinding that = (LetBinding)o;

        if (!name.equals(that.name)) return false;
        if (scope != that.scope) return false;
        if (hasDeclaredType() ? !declaredType.equals(that.declaredType) :
            that.hasDeclaredType()) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + (hasDeclaredType() ? declaredType.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
