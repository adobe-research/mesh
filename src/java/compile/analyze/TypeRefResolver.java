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
package compile.analyze;

import compile.Session;
import compile.term.TypeBinding;
import compile.term.Term;
import compile.type.*;
import compile.type.visit.StackedTypeVisitor;

/**
 * {@link #resolve} method resolves any type refs within the
 * hosted type term, as well as any value expressions.
 *
 * @author Basil Hosmer
 */
public final class TypeRefResolver extends StackedTypeVisitor<Object>
{
    private final RefResolver refResolver;

    public TypeRefResolver(final RefResolver refResolver)
    {
        this.refResolver = refResolver;
    }

    /**
     *
     */
    public void resolve(final Type type)
    {
        visitType(type);
    }

    /**
     * resolve value references. note that enum base type is
     * never stated explicitly, so no resolution is needed.
     */
    @Override
    public Object visit(final EnumType enumType)
    {
        if (enumType.isExplicit())
            for (final Term value : enumType.getValues())
                refResolver.resolve(value);

        return enumType;
    }

    /**
     * Resolve a type reference.
     * <p/>
     * Note: for now we're making no restrictions on in-scope
     * forward refs to typedefs, but this might be a usability
     * issue.
     * <p/>
     * Note: Inline type params show up here as pre-resolved refs
     * to inline type params. {@link compile.type.visit.TypeBindingCollector} has done
     * the housekeeping to move them to the enclosing type's param
     * map.
     */
    @Override
    public Object visit(final TypeRef ref)
    {
        if (ref.isResolved())
        {
            if (ref.isParamRef())
            {
                // reality check pre-resolved inline param ref
                final ScopeType currentScope = getCurrentTypeScope();

                // must have a scope
                assert currentScope != null;

                // inline param refs induce declared params at the level
                // of the current scope. confirm that that's where this param is.
                assert currentScope.getParam(ref.getName()) == ref.getBinding();
            }
        }
        else
        {
            // resolve

            final TypeBinding binding = findNamedType(ref.getName());

            if (binding != null)
            {
                ref.setBinding(binding);

                if (Session.isDebug())
                    Session.debug(ref.getLoc(), "resolved type ref {0} to type {1} from {2}",
                        ref.dump(), binding.dump(), binding.getLoc());
            }
            else
            {
                Session.error(ref.getLoc(),
                    "no definition for type {0} is available here", ref.getName());
            }
        }

        // dependency tracking
        final TypeBinding binding = ref.getBinding();
        if (binding != null)
        {
            // register dependency from the current statement
            // in nameResolver to this TypeBinding
            if (!(binding instanceof TypeParam))
                refResolver.registerDependency(binding);
        }

        return super.visit(ref);
    }

    /**
     * helper--look for a named type, first among the params of types
     * we are nested within, then among the params and type defs of
     * scopes we are nested within.
     */
    private TypeBinding findNamedType(final String name)
    {
        for (final Type type : getTypeScopeStack())
        {
            final TypeParam param = type.getParam(name);
            if (param != null)
                return param;
        }

        return refResolver.getCurrentScope().findTypeBinding(name);
    }
}
