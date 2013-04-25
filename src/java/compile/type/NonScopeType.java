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

import compile.DumpUtils;
import compile.Loc;
import compile.Session;
import compile.type.visit.SubstMap;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Common base class for classes of type terms that cannot
 * be quantified.
 * TODO should go away after refactor
 *
 * @author Basil Hosmer
 */
public abstract class NonScopeType extends AbstractType
{
    private static final LinkedHashMap<String, TypeParam> EMPTY_PARAMS =
        new LinkedHashMap<String, TypeParam>();

    public NonScopeType(final Loc loc)
    {
        super(loc);
    }

    // Type

    public final void collectInlineParams()
    {
    }

    public boolean hasParams()
    {
        return false;
    }

    public final Map<String, TypeParam> getParams()
    {
        return EMPTY_PARAMS;
    }

    public final TypeParam getParam(final String name)
    {
        return null;
    }

    /**
     * Note that we allow this call but discard the params.
     * The immediate motivation is to allow the parser to
     * build parameterized non-scope types in the course
     * of a backtracking parse, but it leaves such types
     * broken on a successful parse, since the params
     * will have been forgotten. To fix this, we need
     * to eliminate the type-level distinction ({@link ScopeType}
     * vs {@link NonScopeType}), and bite the bullet of
     * either being to host params from {@link AbstractType},
     * or always hosting params in a wrapper. TODO
     */
    public final void addParam(final TypeParam param)
    {
        if (Session.isDebug())
            Session.debug(loc, "non-scope type {0} ignoring addParam({1})",
                dump(), param.dump());
    }

    /**
     * see comment for {@link #addParam}
     */
    public final void addParams(final Collection<TypeParam> params)
    {
        if (Session.isDebug())
            Session.debug(loc, "non-scope type {0} ignoring addParams({1})",
                dump(), DumpUtils.dumpList(params));
    }

    public final boolean getParamsCommitted()
    {
        return true;
    }

    public Type quantify(final SubstMap newParams, final SubstMap ambientParams)
    {
        return this;
    }
    
    public SubstMap buildParamMap(final Set<TypeVar> vars, final int nameGenOffset)
    {
        return SubstMap.EMPTY;
    }

    public final Type instance(final TypeEnv env, final boolean useParamNames)
    {
        return this;
    }
}
