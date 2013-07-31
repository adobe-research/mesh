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
import compile.type.constraint.Constraint;
import compile.type.kind.Kind;

import java.util.Map;

/**
 * Type environment interface. Supplies fresh type vars, etc.
 *
 * @author Basil Hosmer
 */
public interface TypeEnv
{
    /**
     *
     */
    public void pushInstanceVars(final Map<TypeParam, TypeVar> map);

    /**
     *
     */
    public void popInstanceVars();

    /**
     * search scope stack for a var mapping a given param.
     * during unification, type expressions that refer to
     * out-of-scope type params must use the vars created
     * when the outer type is instantiated, rather than
     * creating new ones.
     */
    TypeVar findInstanceVar(final TypeParam param);

    /**
     * create new implicit type var, guaranteed unique within this type check.
     */
    TypeVar freshVar(final Loc loc);

    /**
     * create new implicit type var, guaranteed unique within this type check.
     * Kind is given, constraint is ANY
     */
    TypeVar freshVar(final Loc loc, final Kind kind);

    /**
     * create new implicit type var, guaranteed unique within this type check.
     * Kind and constraint are given
     */
    TypeVar freshVar(final Loc loc, final Kind kind, final Constraint constraint);

    /**
     * create new implicit type var, guaranteed unique within this type check.
     * particulars are taken from param (which is also stored) except for
     * constraint, which must be a prebuild instance of param constraint
     */
    TypeVar freshVar(final TypeParam param);

    /**
     * attempt to unify two types, maybe adding substitutions in the process
     */
    boolean unify(Loc loc, Type t1, Type t2);

    /**
     * cycle breaker during unifications
     */
    boolean checkVisited(final Type left, final Type right);
}
