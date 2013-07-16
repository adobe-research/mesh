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

import com.google.common.collect.Lists;
import compile.Loc;
import compile.Pair;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Type list term.
 *
 * @author Basil Hosmer
 */
public final class TypeList extends NonScopeType
{
    private final List<Type> items;

    public TypeList(final Loc loc, final List<Type> items)
    {
        super(loc);
        this.items = items;
    }

    public List<Type> getItems()
    {
        return items;
    }

    /**
     * return substitution if we contain at least as many items as another
     * type list, and all overlapping items are unifiable with each other.
     * otherwise null
     */
    public SubstMap subsume(final Loc loc, final TypeList list, final TypeEnv env)
    {
        if (items.size() < list.getItems().size())
            return null;

        SubstMap subst = SubstMap.EMPTY;

        int i = 0;
        for (final Type listItem : list.getItems())
        {
            final Type item = items.get(i++);

            final SubstMap itemSubst =
                item.subst(subst).unify(loc, listItem.subst(subst), env);

            if (itemSubst == null)
                return null;

            subst = subst.compose(loc, itemSubst);
        }

        return subst;
    }

    /**
     * return pair of merged type list and substitution map, if
     * we can be merged successfully with another type list.,
     * otherwise null
     */
    public Pair<TypeList, SubstMap> merge(final TypeList list, final TypeEnv env)
    {
        // NOTE: these should always be enums over ground types
        SubstMap subst = SubstMap.EMPTY;

        final List<Type> resultItems = Lists.newArrayList(items);
        final int n = items.size();

        int i = 0;
        for (final Type listItem : list.getItems())
        {
            if (i < n)
            {
                final Type resultItem = resultItems.get(i++);

                final SubstMap memberSubst =
                    resultItem.subst(subst).unify(loc, listItem.subst(subst), env);

                if (memberSubst == null)
                    return null;

                subst = subst.compose(loc, memberSubst);
            }
            else
            {
                resultItems.add(listItem);
            }
        }

        return Pair.create(new TypeList(loc, resultItems), subst);
    }

    // Type

    /**
     * TODO This is here to support demos and is horribly inefficient
     * TODO and duplicative.
     * TODO Transitive eval/deref needs to be (a) implemented properly,
     * TODO (b) be merged with reducer and/or (c) implemented for all
     * TODO type terms. Type system needs a housecleaning in general.
     */
    public Type eval()
    {
        List<Type> newItems = null;

        final int size = items.size();
        for (int i = 0; i < size; i++)
        {
            final Type item = items.get(i);
            final Type itemEval = item.deref().eval();

            if (item != itemEval)
            {
                if (newItems == null)
                    newItems = new ArrayList<Type>(items);

                newItems.set(i, itemEval);
            }
        }

        return newItems == null ? this : new TypeList(loc, newItems);
    }

    public Kind getKind()
    {
        return Kinds.STAR_LIST;
    }

    /**
     * TODO associate TypeAbs-specific matching with the TypeAbs itself
     */
    public SubstMap unify(final Loc loc, final Type other, final TypeEnv env)
    {
        if (other instanceof TypeVar)
            return SubstMap.bindVar(loc, (TypeVar)other, this, env);

        final Type otherEval = other.deref().eval();

        if (otherEval instanceof TypeList)
        {
            // both sides are explicit type lists, unify item by item:

            // A <=> X, B <=> Y, C <=> Z
            // ------------------
            // [A,B,C] <=> [X,Y,Z]

            // NOTE: experimental [A, B, C, ...] <= [X, Y, Z]

            final TypeList otherList = (TypeList)otherEval;

            final List<Type> otherItems = otherList.getItems();

            return items.size() == otherItems.size() ?
                unifyRange(0, otherItems, env) :
                null;
        }
        else if (Types.isApp(otherEval) && otherEval.getKind() == Kinds.STAR_LIST)
        {
            // here we need to unify against type applications yielding type lists

            final TypeApp app = (TypeApp)otherEval;
            final Type base = app.getBase();

            if (base == Types.TMAP)
            {
                // other type is each expr

                // A <=> T(a), B <=> T(b), C <=> T(c), V <=> [a,b,c]
                // -------------------------------------------------
                // [A,B,C] <=> T @ V

                final Type tlist = Types.tmapList(app);
                final Type tcon = Types.tmapCons(app);

                final List<Type> argVars = new ArrayList<Type>();

                SubstMap subst = SubstMap.EMPTY;

                for (final Type item : items)
                {
                    final TypeVar argVar = env.freshVar(item.getLoc(), Kinds.STAR);
                    argVars.add(argVar);

                    final Type indiv = Types.app(tcon, argVar).deref().eval();

                    final SubstMap itemSubst =
                        item.subst(subst).unify(loc, indiv.subst(subst), env);

                    if (itemSubst == null)
                        return null;

                    subst = subst.compose(loc, itemSubst);
                }

                final TypeList memberArgList = new TypeList(loc, argVars);

                final SubstMap listSubst =
                    memberArgList.subst(subst).unify(loc, tlist.subst(subst), env);

                return subst.compose(loc, listSubst);
            }
        }

        return null;
    }

    /**
     * unify a range of our list against an entire other list.
     */
    private SubstMap unifyRange(final int start, final List<Type> list, final TypeEnv env)
    {
        if (start < 0)
            return null;

        final int listSize = list.size();

        if (start + listSize > items.size())
            return null;

        SubstMap subst = SubstMap.EMPTY;

        for (int i = 0; i < listSize; i++)
        {
            final Type item = items.get(start + i);
            final Type otherItem = list.get(i);

            final SubstMap itemSubst =
                item.subst(subst).unify(loc, otherItem.subst(subst), env);

            if (itemSubst == null)
                return null;

            subst = subst.compose(loc, itemSubst);
        }

        return subst;
    }

    public <T> T accept(final TypeVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    public boolean equiv(final Type other, final EquivState state)
    {
        final Type otherDeref = other.deref();

        if (otherDeref instanceof TypeList)
        {
            final TypeList otherList = (TypeList)otherDeref;

            for (int i = 0; i < items.size(); i++)
                if (!items.get(i).equiv(otherList.getItems().get(i), state))
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

        final TypeList typeList = (TypeList)obj;

        if (!items.equals(typeList.items)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return items.hashCode();
    }
}