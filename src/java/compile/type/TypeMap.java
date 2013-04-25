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
import compile.term.Term;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

import java.util.ArrayList;
import java.util.Map;

/**
 * Type map term.
 *
 * @author Basil Hosmer
 */
public final class TypeMap extends NonScopeType
{
    private Type keyType;
    private final Map<Term, Type> members;

    public TypeMap(final Loc loc, final Type keyType, final Map<Term, Type> members)
    {
        super(loc);
        this.keyType = keyType;
        this.members = members;

        // verifyKeyAgreement();
    }

    public TypeMap(final Type keyType, final Map<Term, Type> members)
    {
        this(Loc.INTRINSIC, keyType, members);
    }

    public TypeMap(final Loc loc, final Map<Term, Type> members)
    {
        this(loc, new ChoiceType(loc, members.keySet()), members);
    }

    public Map<Term, Type> getMembers()
    {
        return members;
    }

    public Type getKeyType()
    {
        assert keyType != null;
        return keyType;
    }

    public void setKeyType(final Type keyType)
    {
        assert keyType == null;
        this.keyType = keyType;
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
            return SubstMap.bindVar(loc, (TypeVar)other, this);

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