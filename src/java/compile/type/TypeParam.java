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
package compile.type;

import compile.Loc;
import compile.Session;
import compile.module.Scope;
import compile.term.visit.BindingVisitor;
import compile.term.TypeBinding;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

/**
 * Type param binding.
 *
 * @author Basil Hosmer
 */
public final class TypeParam extends TypeBinding
{
    private final Kind kind;
    private ScopeType typeScope;

    public TypeParam(final Loc loc, final String name, final Kind kind)
    {
        super(loc, name);
        this.kind = kind;
        this.typeScope = null;
    }

    public TypeParam(final String name, final Kind kind)
    {
        this(Loc.INTRINSIC, name, kind);
    }

    public TypeParam(final String name)
    {
        this(Loc.INTRINSIC, name, Kinds.STAR);
    }

    public TypeParam(final TypeParam param)
    {
        this(param.getLoc(), param.getName(), param.getKind());
    }

    public boolean hasTypeScope()
    {
        return typeScope != null;
    }

    public ScopeType getTypeScope()
    {
        if (typeScope == null)
            assert false : "null type scope on type param";

        return typeScope;
    }

    /**
     * Note: we allow params to be transferred from one
     * ScopeType to another. see e.g. {@link compile.type.visit.TypeVarSubstitutor}
     */
    public void setTypeScope(final ScopeType typeScope)
    {
        this.typeScope = typeScope;
    }

    // Type

    public Kind getKind()
    {
        return kind;
    }

    public SubstMap unify(final Loc loc, final Type other, final TypeEnv env)
    {
        Session.error(loc, "internal error: TypeParam.unify()");
        return null;
    }

    public <T> T accept(final TypeVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    public boolean equiv(final Type other, final EquivState state)
    {
        final Type otherDeref = other.deref();

        if (otherDeref instanceof TypeParam)
        {
            final TypeParam otherParam = (TypeParam)otherDeref;
            return state.matchParam(this, otherParam);
        }

        return false;
    }

    // Binding

    public Scope getScope()
    {
        assert false : "getScope() on type param";
        return null;
    }

    public void setScope(final Scope scope)
    {
        assert false;
    }

    public <T> T accept(final BindingVisitor<T> visitor)
    {
        assert false;
        return null;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final TypeParam typeParam = (TypeParam)o;

        if (kind != null ? !kind.equals(typeParam.kind) : typeParam.kind != null)
            return false;

        if (typeScope != null ? typeScope != typeParam.typeScope :
            typeParam.typeScope != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        return result;
    }
}