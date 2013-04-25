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
import compile.type.kind.Kind;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Type tuple term.
 *
 * @author Basil Hosmer
 */
public final class TypeTuple extends NonScopeType
{
    private final List<Type> members;
    private Kind kind;

    public TypeTuple(final Loc loc, final List<Type> members, final Kind kind)
    {
        super(loc);
        this.members = members;
        this.kind = kind;
    }

    public TypeTuple(final Loc loc, final List<Type> members)
    {
        this(loc, members, null);
    }

    public TypeTuple(final Loc loc, final Type... memberTypes)
    {
        this(loc, Arrays.asList(memberTypes), null);
    }

    public List<Type> getMembers()
    {
        return members;
    }

    /**
     * Note: may be set to {@link compile.type.kind.Kinds#STAR}
     * in error situations, so no checking for consistency here.
     */
    public void setKind(final Kind kind)
    {
        this.kind = kind;
    }

    // Type

    /**
     * TODO This is here to support demos and is horribly inefficient.
     * TODO Transitive eval/deref needs to be (a) implemented properly,
     * TODO (b) be merged with reducer and/or (c) implemented for all
     * TODO type terms. Type system needs a housecleaning in general.
     */
    public Type eval()
    {
        List<Type> newMembers = null;

        final int size = members.size();
        for (int i = 0; i < size; i++)
        {
            final Type member = members.get(i);
            final Type memberDeref = member.deref().eval();

            if (member != memberDeref)
            {
                if (newMembers == null)
                    newMembers = new ArrayList<Type>(members);

                newMembers.set(i, memberDeref);
            }
        }

        return newMembers == null ? this :
            new TypeTuple(loc, newMembers, kind);
    }

    public Kind getKind()
    {
        return kind;
    }

    public SubstMap unify(final Loc loc, final Type other, final TypeEnv env)
    {
        if (other instanceof TypeVar)
            return SubstMap.bindVar(loc, (TypeVar)other, this);

        final Type otherDeref = other.deref().eval();

        if (otherDeref instanceof TypeTuple)
        {
            final TypeTuple otherTuple = (TypeTuple)otherDeref;

            if (members.size() != otherTuple.getMembers().size())
                return null;

            final List<Type> otherMembers = otherTuple.getMembers();

            SubstMap subst = SubstMap.EMPTY;

            for (int i = 0; i < members.size(); i++)
            {
                final Type member = members.get(i);
                final Type otherMember = otherMembers.get(i);

                final SubstMap memberSubst =
                    member.subst(subst).unify(loc, otherMember.subst(subst), env);

                if (memberSubst == null)
                    return null;

                subst = subst.compose(loc, memberSubst);
            }

            return subst;
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

        if (otherDeref instanceof TypeTuple)
        {
            final TypeTuple otherTuple = (TypeTuple)otherDeref;

            for (int i = 0; i < members.size(); i++)
                if (!members.get(i).equiv(otherTuple.getMembers().get(i), state))
                    return false;

            return true;
        }

        return false;
    }
}