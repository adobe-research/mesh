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
import compile.type.*;
import compile.type.constraint.Constraint;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Create a new type by applying a TypeVar -> Type
 * substitution map to a given type.
 * <p/>
 * NOTE: types obey the lexical scoping of expressions, so
 * outer type parameters (e.g. on function signatures) may
 * be referred to by inner types. These parameter references
 * need to be preserved during quantification, which yields
 * new types.
 * <p/>
 * We currently do this very crudely, by moving, rather than
 * copying, type param objects that are propagated here.
 * This effectively destroys the original type, which is
 * acceptable in most cases but breaks in at least one situation,
 * when inline type abstractions are applied multiple times (e.g.
 * in a type-level each). For now, we have a flag that disables
 * the move-don't-copy behavior, which means that nested type
 * abstractions don't work. I.e., our way of maintaining
 * param refs is BROKEN. TODO FIX ASAP
 *
 * @author Basil Hosmer
 */
public final class TypeVarSubstitutor extends TypeTransformerBase
{
    /**
     * original type
     */
    private final Type type;

    /**
     * map of variable substitutions
     */
    private final SubstMap substMap;

    /**
     * true if we can destructively copy params from
     * original to result (see comment header)
     */
    private final boolean canCopyParams;

    /**
     * params we've encountered refs to during traversal,
     * that need to be transferred to result (see header comment).
     * Note: must be identity set.
     */
    private final Set<TypeParam> xferParams;

    /**
     * if we can copy params, we remember refs here.
     */
    private final IdentityHashMap<TypeParam, List<TypeRef>> paramRefs;

    /**
     * private entry point, used directly for quantification of
     * embedded type abstractions and type defs (the latter only
     * until {@link compile.analyze.TypeChecker} is made to handle
     * them properly)
     */
    public TypeVarSubstitutor(final Type type, final SubstMap substMap)
    {
        this(type, substMap, false);
    }

    /**
     *
     */
    public TypeVarSubstitutor(final Type type, final SubstMap substMap,
        final boolean canCopyParams)
    {
        this.type = type;
        this.substMap = substMap;
        this.canCopyParams = canCopyParams;
        this.xferParams = Sets.newIdentityHashSet();
        this.paramRefs = new IdentityHashMap<TypeParam, List<TypeRef>>();
    }

    /**
     * quantify and reduce
     */
    public final Type apply()
    {
        if (substMap.isEmpty())
            return type;

        final Type applied = transform(type);

        final Type reduced = TypeReducer.reduce(applied);

        // Session.info("TVS.apply() {0} => {1}", type.dump(), reduced.dump());

        return reduced;
    }

    // TypeTransformerBase

    /**
     * Here we transfer any params from the original that
     * are used by the transformed result.
     * {@link #xferParams} may contain nonlocal params, so
     * we need to intersect with the original's params when
     * transferring.
     * Note that this makes the original useless.
     * See header comment.
     */
    @Override
    protected void fixupParams(final Type original, final Type result)
    {
        for (final TypeParam param : original.getParams().values())
        {
            if (xferParams.contains(param))
            {
                if (canCopyParams)
                {
                    final TypeParam copy = new TypeParam(param);

                    result.addParam(copy);

                    if (paramRefs.containsKey(param))
                    {
                        for (final TypeRef paramRef : paramRefs.get(param))
                            paramRef.setBinding(copy);
                    }

                }
                else
                {
                    result.addParam(param);
                }
            }
        }
    }


    // TypeVisitor

    /**
     * At each occurence of a type variable, we return the corresponding
     * entry in our substitution map, if one exists.
     * When the substitution is a type parameter, we must also wrap the
     * param in a type ref.
     * Note that params from the substitution map are either out of scope
     * or freshly generated, so we don't record them for transfer from
     * original type to transformed result.
     */
    public Type visit(final TypeVar var)
    {
        // cases:
        // var stays var
        //      subst constraint
        // var becomees param
        //      set param constraint to subst constraint first time through
        // becomes ground type
        //      constraint must have been satisfied, can ignore

        final Type type = substMap.get(var);

        if (type == null)
        {
            final Constraint constraint = var.getConstraint();
            final Constraint substConstraint = constraint.subst(substMap);

            if (constraint != substConstraint)
                var.setConstraint(substConstraint);

            return var;
        }
        else if (type instanceof TypeParam)
        {
            final TypeParam substParam = (TypeParam)type;

            if (substParam.getConstraint() == null)
            {
                // install the quantified param constraint
                substParam.setConstraint(var.getConstraint().subst(substMap));
            }

            return new TypeRef(var.getLoc(), substParam);
        }
        else
        {
            return type;
        }
    }

    /**
     * Add any existing refs to in-scope params to
     * {@link #xferParams} for later fixup.
     */
    @Override
    public Type visit(final TypeRef ref)
    {
        if (ref.isParamRef())
        {
            final TypeParam param = (TypeParam)ref.getBinding();

            if (param.getTypeScope() == type)
            {
                xferParams.add(param);

                if (canCopyParams)
                {
                    if (!paramRefs.containsKey(param))
                        paramRefs.put(param, new ArrayList<TypeRef>());

                    final TypeRef newRef = new TypeRef(ref.getLoc(), ref.getName());

                    paramRefs.get(param).add(newRef);

                    return newRef;
                }
            }
        }

        return ref;
    }

    /**
     * Quantify anonymous type abstractions.
     */
    @Override
    public Type visit(final TypeCons cons)
    {
        if (!cons.isAbs())
            return cons;

        final Type body = cons.getBody();

        if (!(body instanceof ScopeType))
            return cons;

        final Type bodyQuant = new TypeVarSubstitutor(body, substMap, true).apply();

        return new TypeCons(cons.getLoc(), cons.getName(), cons.getKind(), bodyQuant);
    }
}