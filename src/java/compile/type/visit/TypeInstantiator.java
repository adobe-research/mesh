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

import compile.Session;
import compile.type.*;
import compile.type.constraint.Constraint;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Produce a version of the given type with params
 * replaced by variables. Star types are completely
 * instantiated--params of the type itself are replaced
 * by fresh variables, while references to params from
 * enclosing scopes are replaced by their respective
 * previously-created variables. Type abstractions
 * have only nonlocal param refs replaced, creating
 * abstraction instances suitable for application
 * in the current inference context.
 * TODO revisit the above in context of HRP refactor
 * TODO instantiate type defs in {@link compile.analyze.TypeChecker#initTypeDef}.
 * <p/>
 * NOTE: here our parameterized originals must remain
 * usable, so we can't simply move retained params to
 * the transformed type (in the second case above),
 * as we do in cases where originals are disposable
 * (e.g. {@link TypeVarSubstitutor}). Here we copy instead,
 * but this breaks nested type abstractions.
 * TODO this is broken, fix param ref tracking.
 *
 * @author Basil Hosmer
 */
public final class TypeInstantiator extends TypeTransformerBase
{
    private final ScopeType type;

    private final boolean isAbs;

    private final TypeEnv env;

    private final Map<TypeParam, TypeVar> paramVars;

    private final Map<TypeParam, TypeParam> paramCopies;

    private final Type instance;

    /**
     * public entry point for non-abstraction types.
     * generates params -> var map immediately
     */
    public TypeInstantiator(final ScopeType type,
        final TypeEnv env,
        final boolean useParamNames)
    {
        this(type, env, useParamNames, false);
    }

    /**
     * private entry point for inline type abstractions
     */
    private TypeInstantiator(final ScopeType type, final TypeEnv env)
    {
        this(type, env, true, true);
    }

    /**
     * common private constructor: isAbs, instance caches given
     * explicitly.
     */
    private TypeInstantiator(final ScopeType type,
        final TypeEnv env,
        final boolean useParamNames,
        final boolean isAbs)
    {
        this.type = type;
        this.isAbs = isAbs;
        this.env = env;

        if (isAbs)
        {
            // we'll be replacing local params with copies

            paramVars = null;

            paramCopies = new IdentityHashMap<TypeParam, TypeParam>();

            for (final TypeParam param : type.getParams().values())
                paramCopies.put(param, new TypeParam(param));
        }
        else
        {
            // will be replacing local params with vars.
            // must first create typevar instance for each param,
            // then go back and instantiate constraints.

            paramCopies = null;

            paramVars = new IdentityHashMap<TypeParam, TypeVar>();

            for (final TypeParam param : type.getParams().values())
            {
                paramVars.put(param,
                    useParamNames ?
                        env.freshVar(param) :
                        env.freshVar(param.getLoc(), param.getKind(),
                            param.getConstraint()));
            }

            for (final TypeVar var : paramVars.values())
            {
                final Constraint constraint = var.getConstraint();
                final Constraint constraintInstance = constraint.instance(this);

                if (constraint != constraintInstance)
                    var.setConstraint(constraintInstance);
            }
        }

        instance = transform(type);
    }

    public Type getInstance()
    {
        return instance;
    }

    public ScopeType getType()
    {
        return type;
    }

    public Map<TypeParam, TypeVar> getParamVars()
    {
        return paramVars;
    }

    // TypeTransformerBase

    /**
     * for type abstractions, install param copies on result.
     * otherwise, we've replaced all in-scope params with vars,
     * no action necessary
     */
    @Override
    protected void fixupParams(final Type original, final Type result)
    {
        if (isAbs)
        {
            for (final TypeParam paramCopy : paramCopies.values())
                result.addParam(paramCopy);
        }
    }

    // TypeVisitor

    @Override
    public Type visit(final WildcardType wildcard)
    {
        return env.freshVar(wildcard.getLoc());
    }

    @Override
    public Type visit(final TypeRef ref)
    {
        if (ref.isParamRef())
        {
            final TypeParam param = (TypeParam)ref.getBinding();

            if (param.getTypeScope() == type)
            {
                // local (self-hosted) param

                if (isAbs)
                {
                    // for type abstractions we update local
                    // param refs to point to new param set

                    final TypeParam copy = paramCopies.get(param);
                    assert copy != null : "missing param copy";
                    return new TypeRef(ref.getLoc(), copy);
                }
                else
                {
                    // for ground types we replace local
                    // param refs with type vars

                    final TypeVar var = paramVars.get(param);
                    assert var != null : "missing type var";

                    return var;
                }
            }
            else
            {
                // always replace nonlocal params with vars

                final Type var = env.findInstanceVar(param);

                if (var == null)
                {
                    Session.error(ref.getLoc(),
                        "no var available for out of scope param {0} in type {1}",
                        param.dump(), type.dump());

                    return param;
                }

                return var;
            }
        }
        else
        {
            return ref;
        }
    }

    @Override
    public Type visit(final TypeParam param)
    {
        assert false;   // should never get here, see visit(TypeRef)
        return param;
    }

    /**
     * Instantiate anonymous type abstractions.
     */
    @Override
    public Type visit(final TypeCons cons)
    {
        if (!cons.isAbs())
            return cons;

        final Type body = cons.getBody();

        if (!(body instanceof ScopeType))
            return cons;

        env.pushInstanceVars(paramVars);

        final Type bodyInst = new TypeInstantiator((ScopeType)body, env).getInstance();

        env.popInstanceVars();

        return new TypeCons(cons.getLoc(), cons.getName(), cons.getKind(), bodyInst);
    }
}