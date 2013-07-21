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

import com.google.common.collect.Maps;
import compile.Loc;
import compile.Pair;
import compile.term.Term;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Type map term.
 * TODO should not inherit from NonScopeTYpe, nor should TypeList, TypeTuple
 *
 * @author Basil Hosmer
 */
public final class TypeMap extends NonScopeType
{
    private EnumType keyType;
    private final Map<Term, Type> members;

    public TypeMap(final Loc loc, final EnumType keyType, final Map<Term, Type> members)
    {
        super(loc);
        this.keyType = keyType;
        this.members = members;

        // verifyKeyAgreement();
    }

    public TypeMap(final Loc loc, final Map<Term, Type> members)
    {
        this(loc, new EnumType(loc, new WildcardType(loc), members.keySet()), members);
    }

    public Map<Term, Type> getMembers()
    {
        return members;
    }

    public EnumType getKeyType()
    {
        assert keyType != null;
        return keyType;
    }

    public void setKeyType(final EnumType keyType)
    {
        assert keyType == null;
        this.keyType = keyType;
    }

    public Collection<Type> getValueTypes()
    {
        return members.values();
    }

    /**
     * return substitution if we have a unifiable key type and
     * contain unifiable entries for all entries in another type map,
     * otherwise null
     */
    public SubstMap subsume(final Loc loc, final TypeMap map, final TypeEnv env)
    {
        if (members.size() < map.getMembers().size())
            return null;

        SubstMap subst =
            keyType.getBaseType().unify(loc, map.getKeyType().getBaseType(), env);

        if (subst == null)
            return null;

        for (final Map.Entry<Term, Type> entry : map.getMembers().entrySet())
        {
            final Term key = entry.getKey();
            final Type mapMember = entry.getValue();
            final Type member = members.get(key);

            if (member == null)
                return null;

            final SubstMap memberSubst =
                member.subst(subst).unify(loc, mapMember.subst(subst), env);

            if (memberSubst == null)
                return null;

            subst = subst.compose(loc, memberSubst);
        }

        return subst;
    }

    /**
     * return pair of merged type map and substitution map, if
     * we can be merged successfully with another type map,
     * otherwise null
     */
    public Pair<TypeMap, SubstMap> merge(final TypeMap map, final TypeEnv env)
    {
        // NOTE: these should always be enums over ground types
        SubstMap subst =
            keyType.getBaseType().unify(loc, map.getKeyType().getBaseType(), env);

        if (subst == null)
            return null;

        final LinkedHashMap<Term, Type> resultMembers = Maps.newLinkedHashMap();
        resultMembers.putAll(members);

        for (final Map.Entry<Term, Type> entry : map.getMembers().entrySet())
        {
            final Term key = entry.getKey();
            final Type mapMember = entry.getValue();
            final Type resultMember = resultMembers.get(key);

            if (resultMember != null)
            {
                final SubstMap memberSubst =
                    resultMember.subst(subst).unify(loc, mapMember.subst(subst), env);

                if (memberSubst == null)
                    return null;

                subst = subst.compose(loc, memberSubst);
            }
            else
            {
                resultMembers.put(key, mapMember);
            }
        }

        return Pair.create(new TypeMap(loc, keyType, resultMembers), subst);
    }

    // Type

    public Kind getKind()
    {
        return Kinds.STAR_MAP;
    }

    /**
     * TODO associate TypeAbs-specific matching with the TypeAbs itself
     */
    public SubstMap unify(final Loc loc, final Type other, final TypeEnv env)
    {
        if (other instanceof TypeVar)
            return SubstMap.bindVar(loc, (TypeVar)other, this, env);

        final Type otherEval = other.deref().eval();

        if (otherEval instanceof TypeMap)
        {
            final TypeMap otherMap = (TypeMap)otherEval;

            if (members.size() != otherMap.getMembers().size())
                return null;

            SubstMap subst = keyType.unify(loc, otherMap.getKeyType(), env);

            if (subst == null)
                return null;

            final Map<Term, Type> otherMembers = otherMap.getMembers();

            for (final Term key : members.keySet())
            {
                final Type member = members.get(key);
                final Type otherMember = otherMembers.get(key);

                final SubstMap memberSubst =
                    member.subst(subst).unify(loc, otherMember.subst(subst), env);

                if (memberSubst == null)
                    return null;

                subst = subst.compose(loc, memberSubst);
            }

            return subst;
        }
        else if (Types.isApp(otherEval) && otherEval.getKind() == Kinds.STAR_MAP)
        {
            // here we need to unify against type applications yielding type maps

            final TypeApp app = (TypeApp)otherEval;
            final Type base = app.getBase();

            if (base == Types.ASSOC)
            {
                final Type otherKey = Types.assocKey(app);
                final Type otherVals = Types.assocVals(app);

                // other type is assoc(key type, value type list)

                // [x, y, z] <=> K, [A, B, C] <=> V
                // -----------------------------------------------------
                // [x: A, y: B, z: C] <=> [K : V]

                final SubstMap keySubst = keyType.unify(loc, otherKey, env);

                if (keySubst == null)
                    return null;

                final TypeList memberList =
                    new TypeList(loc, new ArrayList<Type>(members.values()));

                final SubstMap valueSubst =
                    memberList.subst(keySubst).unify(loc, otherVals.subst(keySubst), env);

                if (valueSubst == null)
                    return null;

                return keySubst.compose(loc, valueSubst);
            }
        }

        return null;
    }

    public <T> T accept(final TypeVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    public boolean equiv(final Type other, final EquivState state)
    {
        final Type otherDeref = other.deref();

        if (otherDeref instanceof TypeMap)
        {
            final TypeMap otherMap = (TypeMap)otherDeref;

            if (!keyType.equiv(otherMap.getKeyType()))
                return false;

            final Map<Term, Type> otherMembers = otherMap.getMembers();

            for (final Term key : members.keySet())
                if (!members.get(key).equiv(otherMembers.get(key), state))
                    return false;

            return true;
        }

        return false;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final TypeMap typeMap = (TypeMap)obj;

        if (!keyType.equals(typeMap.keyType)) return false;
        if (!members.equals(typeMap.members)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = keyType.hashCode();
        result = 31 * result + members.hashCode();
        return result;
    }
}