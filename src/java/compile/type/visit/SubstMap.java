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

import com.google.common.collect.Sets;
import compile.Loc;
import compile.Pair;
import compile.Session;
import compile.StringUtils;
import compile.type.Type;
import compile.type.TypeEnv;
import compile.type.TypeVar;
import compile.type.constraint.Constraint;

import java.util.*;

/**
 * Type substitution map.
 *
 * @author Basil Hosmer
 */
@SuppressWarnings("serial")
public final class SubstMap extends LinkedHashMap<Type, Type>
{
    /**
     * empty substitution map, must not be modified
     */
    public static SubstMap EMPTY = new SubstMap();

    /**
     * return a substitution map binding a var to a type.
     */
    public static SubstMap bindVar(final Loc loc, final TypeVar var,
        final Type type, final TypeEnv env)
    {
        if (var.equals(type))
            return EMPTY;

        if (!var.getKind().equals(type.getKind()))
        {
            Session.error(loc,
                "attempting to bind type variable {0} with kind {1} to type {2} with kind {3}",
                var.dump(), var.getKind().dump(),
                type.dump(), type.getKind().dump());

            return null;
        }

        if (type.getVars().contains(var))
        {
            Session.error(loc,
                "type variable {0} occurs in type {1} - recursive types are not supported",
                var.dump(), type.dump());

            return null;
        }

        if (type instanceof TypeVar)
        {
            // for var -> var, we need to check and possibly manipulate constraints
            final TypeVar rhsVar = (TypeVar)type;

            final Pair<? extends Constraint, SubstMap> merged =
                var.getConstraint().merge(rhsVar.getConstraint(), env);
            final Constraint mergedConstraint = merged.left;
            final SubstMap mergeSubst = merged.right;

            if (merged == null)
            {
                Session.error(loc,
                    "type constraints {0} and {1} are incompatible",
                    var.getConstraint().dump(), rhsVar.getConstraint().dump());

                return null;
            }

            if (mergedConstraint != rhsVar.getConstraint())
                rhsVar.setConstraint(mergedConstraint.subst(mergeSubst));

            // if lhs var has param info, transfer it.
            // this doesn't affect inference, but helps preserves declared param names.
            if (var.hasSourceParam())
                rhsVar.addUnifiedParam(var.getSourceParam());

            rhsVar.addUnifiedParams(var.getUnifiedParams());

            return mergeSubst.compose(loc, new SubstMap(var, rhsVar));
        }
        else
        {
            final SubstMap sat = var.getConstraint().satisfy(loc, type, env);

            if (sat == null)
            {
                // TODO need to get this back out to TypeChecker to error-format types
                Session.error(loc,
                    "type {0} does not satisfy constraint {1}",
                    type.dump(), var.getConstraint().dump());

                return null;
            }

            return sat.compose(loc, new SubstMap(var, type));
        }
    }

    /**
     * Check for agreement with another map. If maps m1 and m2 agree, there is no
     * LHS v for which m1.contains(v) && m2.contains(v) && m1.get(v) != m2.get(v).
     * An error is raised for each LHS on which maps disagree.
     */
    public static boolean checkAgreement(final Loc loc,
        final SubstMap left, final SubstMap right)
    {
        boolean agrees = true;

        for (final Type type : Sets.intersection(left.keySet(), right.keySet()))
        {
            final Type leftType = left.get(type);
            final Type rightType = right.get(type);

            if (!leftType.equals(rightType))
            {
                // TODO always internal? if not, change message
                Session.error(loc,
                    "internal error: substitution maps disagree: {0} vs. {1}",
                    leftType.dump(), rightType.dump());
                agrees = false;
            }
        }

        return agrees;
    }

    //
    // instance
    //

    /**
     * Empty map
     */
    public SubstMap()
    {
    }

    /**
     * map with a single binding
     */
    public SubstMap(final TypeVar lhs, final Type rhs)
    {
        put(lhs, rhs);
    }

    /**
     * Compose our substitutions with "newer" substitutions from another map.
     * <p/>
     * Note: "newer" means that RHSs of new substs cannot mention vars that are LHSs
     * of old substs. If they do, the resulting map may have cycles, and a call to
     * type.apply(substMap) isn't guaranteed to return a complete substitution.
     * In this case one or more errors are raised.
     */
    public SubstMap compose(final Loc loc, final SubstMap newSubs)
    {
        if (isEmpty())
            return newSubs;

        if (newSubs.isEmpty())
            return this;

        final SubstMap result = new SubstMap();

        for (final Map.Entry<Type, Type> entry : entrySet())
        {
            final Type var = entry.getKey();
            final Type type = entry.getValue();
            result.put(var, type.subst(newSubs));
        }

        result.putAll(newSubs);
        result.checkClosure(loc, Sets.newHashSet(newSubs.values()));

        return result;
    }

    /**
     * Check map for closure: no types in check set can mention any LHS vars.
     *
     */
    public boolean checkClosure(final Loc loc, final Set<Type> newRhses)
    {
        final Set<Type> lhsVars = keySet();

        final Set<Type> cycles = Sets.newLinkedHashSet();

        for (final Type rhs : newRhses)
        {
            for (final TypeVar rhsVar : rhs.getVars())
            {
                if (lhsVars.contains(rhsVar))
                {
                    cycles.add(rhs);

                    Session.error(loc,
                        "internal error: substitution map is not closed. RHS type {0} mentions LHS var {1}, map = {2}",
                        rhs.dump(), rhsVar.dump(), dump());
                }
            }
        }

        if (!cycles.isEmpty())
        {
            return false;
        }

        return true;
    }

    /**
     * Dump substitutions to string
     */
    public String dump()
    {
        final Map<Type, List<Type>> rmap = new LinkedHashMap<Type, List<Type>>();

        for (final Map.Entry<Type, Type> entry : entrySet())
        {
            final Type key = entry.getKey();
            final Type value = entry.getValue();

            if (!rmap.containsKey(value))
                rmap.put(value, new ArrayList<Type>());

            rmap.get(value).add(key);
        }

        final List<String> dumps = new ArrayList<String>();

        for (final Map.Entry<Type, List<Type>> entry : rmap.entrySet())
            dumps.add(TypeDumper.dumpList(entry.getValue(), " ") + " => " +
                entry.getKey().dump());

        return "[" + StringUtils.join(dumps, ", ") + "]";
    }
}