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

import com.google.common.collect.Sets;
import compile.Loc;
import compile.Session;
import compile.type.visit.*;

import java.util.*;

/**
 * Common base implementation for classes that represent type terms
 * that can be quantified.
 * TODO refactor quantified types pronto
 *
 * @author Basil Hosmer
 */
public abstract class ScopeType extends AbstractType
{
    private final LinkedHashMap<String, TypeParam> params;
    private boolean paramsCommitted;

    public ScopeType(final Loc loc)
    {
        super(loc);
        this.params = new LinkedHashMap<String, TypeParam>();
        this.paramsCommitted = false;
    }

    // Type

    public final void collectInlineParams()
    {
        new TypeBindingCollector(this).collect();
        paramsCommitted = true;
    }

    public boolean hasParams()
    {
        return !params.isEmpty();
    }

    public Map<String, TypeParam> getParams()
    {
        return params;
    }

    public TypeParam getParam(final String name)
    {
        return params.get(name);
    }

    public void addParam(final TypeParam param)
    {
        if (paramsCommitted)
        {
            Session.error(loc,
                "internal error in type {0}: addParam with params committed", dump());
        }
        else if (params.containsKey(param.getName()))
        {
            Session.error(loc,
                "internal error in type {0}: addParam {1}, already defined", dump(),
                param.getName());
        }
        else
        {
            param.setTypeScope(this);
            params.put(param.getName(), param);
        }
    }
    
    public void addParams(final Collection<TypeParam> params)
    {
        for (final TypeParam param : params)
            addParam(param);

        paramsCommitted = true;
    }

    public final boolean getParamsCommitted()
    {
        return paramsCommitted;
    }

    /**
     *
     */
    public final Type instance(final TypeEnv env, final boolean useParamNames)
    {
        return new TypeInstantiator(this, env, useParamNames).getInstance();
    }

    /**
     *
     */
    public final Type quantify(final SubstMap newParams, final SubstMap ambientParams)
    {
        // Session.info("ST.quantify() will add {0}", DumpUtils.dumpList(newParams.values()));

        final Type applied = subst(ambientParams.compose(loc, newParams));

        // add new params to our result type
        for (final Type type : newParams.values())
            applied.addParam((TypeParam)type);

        return applied;
    }

    /**
     * Note: nameGenOffset is part of our scheme for avoiding (the appearance of)
     * name capture in dumps and error messages. We quantify inner-to-outer,
     * so the type checker passes in the number of ambient vars as a starting
     * offset to the name generator. That way outermost names are A, B, C, ...
     * Spacing is not perfect, there are gaps. It would give a prettier result to
     * do a consolidation pass at the end, but the important thing for round-
     * tripping (not that we do it currently) is that there is no overlap.
     *
     * NOTE: the relationship between a var's constraint and that of its
     * corresponding param is tricky--see comment in code body, and also
     * {@link TypeVarSubstitutor#visit(TypeVar)}.
     */
    public SubstMap buildParamMap(final Set<TypeVar> vars, final int nameGenOffset,
        final TypeEnv env)
    {
        final Set<TypeVar> qvars = Sets.intersection(vars, getVars());

        if (qvars.isEmpty())
            return SubstMap.EMPTY;

        // when generating param names, avoid names of used params.
        final Set<String> usedNames = new HashSet<String>();

        for (final TypeParam param : TypeParamCollector.collect(this))
            usedNames.add(param.getName());

        // begin generating names from this offset into the generated name space.
        int genIndex = nameGenOffset;

        final SubstMap substMap = new SubstMap();

        for (final TypeVar v : qvars)
        {
            final String name;

            final TypeParam param = v.getBackingParam(usedNames, this);
            if (param != null)
            {
                name = param.getName();
            }
            else
            {
                String tmp;
                do
                {
                    tmp = nameGen(genIndex++);
                }
                while (usedNames.contains(tmp));

                name = tmp;
            }

            usedNames.add(name);

            // NOTE: v's constraint is ignored here, because it needs to be
            // not just transferred to the corresponding param, but also run
            // through this same substitution map at quantification time.
            // Users of this map are responsible for doing this.
            //
            substMap.put(v, new TypeParam(v.getLoc(), name, v.getKind(), null));
        }

        // Session.info("ST.BPM(v) {0} => {1}", DumpUtils.dumpList(vars), substMap.dump());

        return substMap;
    }

    /**
     *
     */
    private static String nameGen(int i)
    {
        String name = "";
        while (i >= 0)
        {
            final int ci = i % 26;
            i = i / 26 - 1;
            name = (char)(((int)'A') + ci) + name;
        }
        return name;
    }

    /**
     * The override here enforces the rule that two scopes
     * can only be equivalent if they match each other.
     */
    public boolean equiv(final Type other)
    {
        if (other instanceof ScopeType)
            return equiv(other, new EquivState(this, (ScopeType)other));

        return equiv(other.deref(), new EquivState());
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ScopeType that = (ScopeType)o;

        if (!params.equals(that.params)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return params.hashCode();
    }
}
