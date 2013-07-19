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
import compile.Pair;
import compile.term.Term;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

/**
 * An enum type is an enumerated subset of a base type.
 * Subs {@link ChoiceType} and {@link ExtentType} exploit
 * differences in how the subset is specified.
 *
 * @author Basil Hosmer
 */
public abstract class EnumType extends ScopeType
{
    protected Type baseType;

    public EnumType(final Loc loc, final Type baseType)
    {
        super(loc);
        this.baseType = baseType;
    }

    public Type getBaseType()
    {
        return baseType;
    }

    public void setBaseType(final Type baseType)
    {
        this.baseType = baseType;
    }

    public abstract boolean isExplicit();

    public abstract int getSize();

    public abstract Iterable<Term> getValues();

    public abstract Pair<? extends EnumType, SubstMap> merge(EnumType otherEnum);

    public abstract SubstMap subsume(Loc loc, Type type, TypeEnv env);

    // Type


    public final Kind getKind()
    {
        return Kinds.STAR;
    }

    public final SubstMap unify(final Loc loc, Type other, final TypeEnv env)
    {
        other = other.deref().eval();

        if (other == this)
        {
            return SubstMap.EMPTY;
        }
        else if (other instanceof EnumType)
        {
            final EnumType enumType = (EnumType)other;
            final SubstMap substMap = baseType.unify(loc, enumType.baseType, env);

            return substMap != null && getValues().equals(enumType.getValues()) ?
                substMap : null;
        }
        else if (other instanceof TypeVar)
        {
            return SubstMap.bindVar(loc, (TypeVar)other, this, env);
        }
        else
        {
            return null;
        }
    }

    public final boolean equiv(final Type other, final EquivState state)
    {
        return equals(other.deref());
    }

    public final <T> T accept(final TypeVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    @Override
    public final boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || !(o instanceof EnumType)) return false;

        final EnumType enumType = (EnumType)o;

        return getSize() == enumType.getSize() &&
            getValues().equals(enumType.getValues());
    }

    @Override
    public final int hashCode()
    {
        return getValues().hashCode();
    }
}
