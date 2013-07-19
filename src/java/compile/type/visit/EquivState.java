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
package compile.type.visit;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import compile.Session;
import compile.Pair;
import compile.type.ScopeType;
import compile.type.Type;
import compile.type.TypeParam;

import java.util.HashSet;

/**
 * Holds the in-progress state of a structural congruence check
 * of two type terms. Done in a hurry and seems to work, should
 * definitely be revisited to improve efficiency and verify correctness.
 * Better yet would be to find we don't need it after refactoring.
 *
 * @author Basil Hosmer
 */
public final class EquivState
{
    private final HashSet<Pair<Type, Type>> visited;
    private final BiMap<ScopeType, ScopeType> scopes;
    private final BiMap<TypeParam, TypeParam> params;

    public EquivState()
    {
        this.visited = new HashSet<Pair<Type, Type>>();
        this.scopes = HashBiMap.create();
        this.params = HashBiMap.create();
    }

    public EquivState(final ScopeType left, final ScopeType right)
    {
        this();
        matchScope(left, right);
    }
    
    /**
     * 
     */
    public boolean checkVisited(final Type left, final Type right)
    {
        final Pair<Type, Type> pair = Pair.create(left, right);
        if (visited.contains(pair))
        {
            return true;
        }
        else 
        {
            visited.add(pair);
            return false;
        }
    }

    /**
     *
     */
    public BiMap<TypeParam, TypeParam> getParams()
    {
        return params;
    }

    /**
     *
     */
    private boolean matchScope(final ScopeType left, final ScopeType right)
    {
        if (scopes.containsKey(left))
        {
            final ScopeType prevRight = scopes.get(left);
            if (!right.equals(prevRight))
            {
                if (Session.isDebug())
                    Session.debug(left.getLoc(),
                        "scope {0} already matched with scope {1}, can''t match {2}",
                        left.dump(), prevRight.dump(), right.dump());

                return false;
            }
            else
            {
                if (Session.isDebug())
                    Session.debug(left.getLoc(), "scope {0} matches scope {1}",
                        left.dump(), right.dump());

                return true;
            }
        }
        else 
        {
            final BiMap<ScopeType, ScopeType> inv = scopes.inverse();
            if (inv.containsKey(right))
            {
                final ScopeType prevLeft = inv.get(right);

                if (Session.isDebug())
                    Session.debug(right.getLoc(),
                        "scope {0} already matched with scope {1}, can''t match {2}",
                        right.dump(), prevLeft.dump(), left.dump());

                return false;
            }
            else
            {
                if (Session.isDebug())
                    Session.debug(left.getLoc(), "adding scope equiv {0} <=> {1}",
                        left.dump(), right.dump());

                scopes.put(left, right);

                return true;
            }
        }
    }

    /**
     *
     */
    public boolean matchParam(final TypeParam left, final TypeParam right)
    {
        if (left.getKind() != right.getKind())
        {
            Session.error(left.getLoc(), "param {0} kind differs from param {1} kind",
                left.dump(), right.dump());

            return false;
        }

        final ScopeType leftScope = left.getTypeScope();
        final ScopeType rightScope = right.getTypeScope();

        if (!matchScope(leftScope, rightScope))
        {
            Session.error(left.getLoc(),
                "param {0} scoped at {1} is not compatible with param {2} scoped at {3}",
                left.dump(), leftScope.getLoc(),
                right.dump(), rightScope.getLoc());

            return false;
        }

        if (params.containsKey(left))
        {
            final TypeParam prev = params.get(left);

            if (!right.equals(prev))
            {
                if (Session.isDebug())
                    Session.debug(left.getLoc(),
                        "param {0} already matched with param {1}, can''t match {2}",
                        left.dump(), prev.dump(), right.dump());

                return false;
            }

            return true;
        }
        else
        {
            final BiMap<TypeParam, TypeParam> inv = params.inverse();

            if (inv.containsKey(right))
            {
                final TypeParam prevLeft = inv.get(right);

                if (Session.isDebug())
                    Session.debug(right.getLoc(),
                        "param {0} already matched with param {1}, can''t match {2}",
                        right.dump(), prevLeft.dump(), left.dump());

                return false;
            }

            params.put(left, right);

            return true;
        }
    }
}
