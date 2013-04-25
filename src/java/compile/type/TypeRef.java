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
import compile.term.TypeBinding;
import compile.term.TypeDef;
import compile.type.kind.Kind;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

/**
 * reference to a named type
 *
 * @author Basil Hosmer
 */
public final class TypeRef extends ScopeType
{
    private final String name;
    private TypeBinding binding;

    private TypeRef(final Loc loc, final String name, final TypeBinding binding)
    {
        super(loc);
        this.name = name;
        this.binding = binding;
    }

    public TypeRef(final Loc loc, final String name)
    {
        this(loc, name, null);
    }

    public TypeRef(final Loc loc, final TypeParam param)
    {
        this(loc, param.getName(), param);
    }

    public TypeRef(final Loc loc, final TypeDef binding)
    {
        this(loc, binding.getName(), binding);
    }

    public String getName()
    {
        return name;
    }

    public TypeBinding getBinding()
    {
        return binding;
    }

    public void setBinding(final TypeBinding binding)
    {
        assert !isResolved();
        this.binding = binding;
    }

    /**
     * patch our current resolved type param to point to another, equivalent object.
     */
    public void patchInlineParam(final TypeParam param)
    {
        assert name.equals(param.getName());
        assert binding instanceof TypeParam;
        assert ((TypeParam)binding).getTypeScope() == param.getTypeScope();

        this.binding = param;
    }

    public boolean isResolved()
    {
        return binding != null;
    }

    public boolean isParamRef()
    {
        return binding instanceof TypeParam;
    }

    // Type

    public Kind getKind()
    {
        return binding != null ? binding.getKind() : null;
    }

    public Type deref()
    {
        return isResolved() ? binding.deref() : null;
    }

    public SubstMap unify(final Loc loc, final Type other, final TypeEnv env)
    {
        assert isResolved();

        if (env.checkVisited(this, other))
            return SubstMap.EMPTY;

        return equals(other) ?
            SubstMap.EMPTY : getBinding().unify(loc, other, env);
    }

    public boolean equiv(final Type other, final EquivState state)
    {
        assert isResolved();
        return state.checkVisited(this, other) ||
            getBinding().equiv(other, state);
    }

    public <T> T accept(final TypeVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TypeRef typeRef = (TypeRef)o;

        if (!name.equals(typeRef.name)) return false;
        if (binding != null ? !binding.equals(typeRef.binding) : typeRef.binding != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
