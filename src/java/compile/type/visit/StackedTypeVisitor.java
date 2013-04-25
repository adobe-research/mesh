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

import compile.type.ScopeType;
import compile.type.Type;

import java.util.ArrayDeque;

/**
 * Type visitor that maintains a stack of enclosing type scopes.
 * NOTE: until higher-rank polymorphism is restored, there will be
 * at most one enclosing scope, and if present it will be the root
 * type. For now HRP is disabled--{@link TypeBindingCollector}
 * traps for inner scopes being pushed.
 *
 * @author Basil Hosmer
 */
public abstract class StackedTypeVisitor<T> extends TypeVisitorBase<T>
{
    protected final ArrayDeque<ScopeType> typeScopeStack;

    public StackedTypeVisitor()
    {
        this.typeScopeStack = new ArrayDeque<ScopeType>();
    }

    /**
     * Note
     */
    @Override
    protected T visitType(final Type type)
    {
        final boolean isEnclosing = type instanceof ScopeType &&
            (type.hasParams() || getTypeScopeStack().isEmpty());

        if (isEnclosing)
            pushTypeScope((ScopeType)type);

        final T result = super.visitType(type);

        if (isEnclosing)
            popTypeScope();

        return result;
    }

    protected ArrayDeque<ScopeType> getTypeScopeStack()
    {
        return typeScopeStack;
    }

    protected ScopeType getCurrentTypeScope()
    {
        return typeScopeStack.peek();
    }

    protected void pushTypeScope(final ScopeType scope)
    {
        typeScopeStack.push(scope);
    }

    protected void popTypeScope()
    {
        typeScopeStack.pop();
    }
}
