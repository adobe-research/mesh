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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An enum type is an enumerated subset of a base type.
® *
 * @author Basil Hosmer
 */
public final class EnumType extends ScopeType
{
    private Type baseType;
    private final LinkedHashSet<Term> values;

    public EnumType(final Loc loc, final Type baseType, final Set<Term> values)
    {
        super(loc);
        this.baseType = baseType;

        this.values = (values instanceof LinkedHashSet) ?
            (LinkedHashSet<Term>)values :
            new LinkedHashSet<Term>(values);
    }

    /**
     * singleton choice set
     */
    public EnumType(final Loc loc, final Type baseType, final Term value)
    {
        this(loc, baseType, Collections.singleton(value));
    }

    public Type getBaseType()
    {
        return baseType;
    }

    public void setBaseType(final Type baseType)
    {
        this.baseType = baseType;
    }

    public int getSize()
    {
        return values.size();
    }

    public LinkedHashSet<Term> getValues()
    {
        return values;
    }

    public int indexOf(final Term term)
    {
        int i = 0;
        for (final Term value : values)
        {
            if (value.equals(term))
                return i;
            i++;
        }

        return -1;
    }

    public Pair<EnumType, SubstMap>
        merge(final EnumType otherEnum, final TypeEnv env)
    {
        final SubstMap subst =
            baseType.unify(loc, otherEnum.getBaseType(), env);

        if (subst == null)
            return null;

        final Set<Term> mergedValues = new LinkedHashSet<Term>(values);
        mergedValues.addAll(otherEnum.values);

        final EnumType merged = new EnumType(loc, baseType, mergedValues);

        return Pair.create(merged, subst);
    }

    public SubstMap subsume(final Loc loc, final Type type, final TypeEnv env)
    {
        if (!(type instanceof EnumType))
            return null;

        final EnumType otherEnum = (EnumType)type;

        final SubstMap subst =
            baseType.unify(loc, otherEnum.getBaseType(), env);

        if (subst == null)
            return null;

        return values.containsAll(otherEnum.values) ?
            subst : null;
    }

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
