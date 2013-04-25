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
import compile.type.ScopeType;
import compile.type.Type;
import compile.type.TypeParam;
import compile.type.TypeRef;

/**
 * Creates a type application term from a parameterized base
 * and a TypeParam -> Type substitution map.
 *
 * @author Basil Hosmer
 */
public final class TypeApplier extends TypeTransformerBase
{
    private final Type base;
    private final SubstMap args;

    public TypeApplier(final Type base, final SubstMap args)
    {
        this.base = base;
        this.args = args;
    }

    public Type apply()
    {
        return transform(base);
    }

    // TypeTransformerBase

    /**
     * If args map is exhaustive, no params need to be transferred.
     * TODO args map shouldn't have to be exhaustive.
     */
    @Override
    protected void fixupParams(final Type original, final Type result)
    {
    }

    // TypeVisitor

    /**
     * Replace references to params with corresponding arguments.
     * Note that we may encounter refs to params from enclosing
     * type terms. Our arg map should never contain entries for
     * these.
     */
    @Override
    public Type visit(final TypeRef ref)
    {
        if (ref.isParamRef())
        {
            final TypeParam param = (TypeParam)ref.getBinding();

            final Type arg = args.get(param);

            final ScopeType paramTypeScope = param.getTypeScope();

            if (arg != null)
            {
                if (paramTypeScope != base)
                    Session.error(param.getLoc(),
                        "internal error: arg {0} specified for nonlocal param {1} in apply",
                        arg.dump(), param.dump());

                return arg;
            }
            else
            {
                if (paramTypeScope == base)
                    Session.error(param.getLoc(),
                        "internal error: no arg specified for param {0} in apply",
                        param.dump());

                return ref;
            }
        }
        else
        {
            return ref;
        }
    }
}