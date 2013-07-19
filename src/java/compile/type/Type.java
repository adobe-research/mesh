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

import compile.Dumpable;
import compile.Loc;
import compile.Located;
import compile.Pair;
import compile.type.kind.Kind;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Type interface. Should be a little less sprawly after refactor.
 *
 * @author Basil Hosmer
 */
public interface Type extends Located, Dumpable
{
    /**
     * used (rarely) to patch loc when building types from user exprs
     */
    public void setLoc(Loc loc);

    /**
     *
     */
    void collectInlineParams();

    /**
     *
     */
    Type deref();

    /**
     * TODO merge with use of TypeReducer.reduce
     */
    Type eval();

    /**
     *
     */
    Kind getKind();

    /**
     *
     */
    boolean hasVars();

    /**
     * Return set of all type variables occuring in the term
     */
    Set<TypeVar> getVars();

    /**
     *
     */
    boolean hasParams();

    /**
     *
     */
    Map<String, TypeParam> getParams();

    /**
     *
     */
    TypeParam getParam(String name);

    /**
     *
     */
    void addParam(TypeParam param);

    /**
     *
     */
    void addParams(Collection<TypeParam> params);

    /**
     *
     */
    boolean getParamsCommitted();

    /**
     * Convert all params to fresh vars. Store type params names with type vars
     * for use in later quantification, if directed
     */
    Type instance(TypeEnv env, boolean useParamNames);

    /**
     *
     */
    Type quantify(SubstMap newParams, SubstMap ambientParams);

    /**
     *
     */
    SubstMap buildParamMap(Set<TypeVar> vars, int nameGenOffset, TypeEnv env);

    /**
     * Apply substitutions from map
     */
    Type subst(SubstMap substMap);

    /**
     * Returns a most general unifier with other type.
     * Experimental - base types generate mgu if otherType
     * is a subtype.
     */
    SubstMap unify(Loc loc, Type other, TypeEnv env);

    /**
     * TODO
     */
    //SubstMap subsume(Loc loc, Type type, TypeEnv env);

    /**
     * TODO
     */
    //Pair<? extends Type, SubstMap> merge(Type type, TypeEnv env);

    /**
     * Equivalent for purposes of checking agreement between
     * declared and inferred types: sees through refs/defs,
     * and matches wildcards to any *-kind type
     */
    boolean equiv(Type other);

    /**
     * used from top-level {@link #equiv} calls, carries
     * in-progress match state.
     */
    boolean equiv(Type other, EquivState state);

    /**
     * Type visitor dispatch.
     */
    <T> T accept(TypeVisitor<T> visitor);

    /**
     *
     */
    boolean hasWildcards();
}
