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
import compile.type.kind.Kinds;

/**
 * A type expression may carry inline type params, analogous to inline value params.
 * Here we collect and add them to the enclosing type scope.
 *
 * @author Basil Hosmer
 */
public final class TypeBindingCollector extends StackedTypeVisitor<Object>
{
    private final Type type;

    public TypeBindingCollector(final Type type)
    {
        this.type = type;
    }

    public void collect()
    {
        visitType(type);
    }

    /**
     * NOTE: here is where we block higher-rank polymorphism in
     * declared types (the syntax allows it).
     */
    @Override
    protected void pushTypeScope(final ScopeType scope)
    {
        if (!getTypeScopeStack().isEmpty())
        {
            Session.error(scope.getLoc(),
                "nested parameterized types are not supported: {0}",
                scope.dump());
        }
        else if (scope != type)
        {
            Session.error(scope.getLoc(),
                "internal error: outermost type scope is not root: {0}",
                scope.dump());
        }

        super.pushTypeScope(scope);
    }

    /**
     * Inline params show up as pre-resolved references
     * to params whose type scopes are unset. The first
     * instance of a given param name gets added to the
     * current type scope, and subsequent instances are
     * repointed.
     */
    @Override
    public Object visit(final TypeRef ref)
    {
        // pre-resolved param refs only
        if (!ref.isParamRef())
            return null;

        final TypeParam param = (TypeParam)ref.getBinding();

        // if param already has type scope set, no action necessary
        if (param.hasTypeScope())
            return null;

        final ScopeType enclosingType = getCurrentTypeScope();

        if (enclosingType.getParamsCommitted())
        {
            // if true, declared params have already been added and committed
            Session.error(ref.getLoc(),
                "inline and declared type params cannot be mixed");

            return null;
        }

        final TypeParam prev = enclosingType.getParam(ref.getName());

        if (prev == null)
        {
            // first encounter with this param name
            if (Session.isDebug())
                Session.debug(ref.getLoc(), "collecting inline type param {0}",
                    param.dump());

            enclosingType.addParam(param);
        }
        else if (prev != param)
        {
            // we've already collected another param instance by this name,
            // so we need to repoint this ref to that instance
            if (param.getKind() != Kinds.STAR)
            {
                // only the first instance of an inline param may have an
                // explicit kind declaration, which we kind of enforce here
                Session.error(ref.getLoc(),
                    "explicit kinds allowed only on first occurence of inline type param {0}",
                    param.getName());
            }

            // satisfy check in patch method
            param.setTypeScope(enclosingType);

            ref.patchInlineParam(prev);
        }
        else
        {
            // should never happen--previously encountered param has no type scope
            assert false : "failed to set type scope on param {0}" + prev.dump();
        }

        return null;
    }

    /**
     * Collect param bindings in anonymous type abstractions.
     * Note that we're avoiding making use of the type scope
     * stack here, for the bad reason of fear of disruption.
     * TODO once there's time, rationalize type scopes,
     * type abstractions, and HRP
     */
    @Override
    public Type visit(final TypeCons cons)
    {
        if (!cons.isAbs())
            return cons;

        final Type body = cons.getBody();

        if (body instanceof ScopeType)
        {
            new TypeBindingCollector(body).collect();
        }

        return null;
    }
}