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
import compile.module.Module;
import compile.module.Scope;
import compile.term.*;

/**
 * Reference resolver. {@link #resolve} method resolves reference terms,
 * checking for in-scope forward refs. Side effects include recording value
 * dependencies in scopes, and tracking variable capture in lambdas.
 * Also resolve types name refs in type terms, both in declared type annotations
 * (in lets and lambdas) and in type defs. Type name resolution is done here rather
 * than in a separate pass because value and type expressions are entangled.
 *
 * @author Basil Hosmer
 */
public final class RefResolver extends ModuleVisitor<Object>
{
    private final TypeRefResolver typeRefResolver;
    private final TermRefResolver termRefResolver;

    public RefResolver(final Module module)
    {
        super(module);
        this.typeRefResolver = new TypeRefResolver(this);
        this.termRefResolver = new TermRefResolver(this);
    }

    TypeRefResolver getTypeRefResolver() { return typeRefResolver; }
    TermRefResolver getTermRefResolver() { return termRefResolver; }

    /**
     *
     */
    public boolean resolve()
    {
        if (Session.isDebug())
            Session.debug(getModule().getLoc(), "Resolving names...");

        return process();
    }

    /**
     * resolve arbitrary term within the current state. Package local,
     * used by e,g, {@link TypeRefResolver}
     */
    Term resolve(final Term term)
    {
        return termRefResolver.resolve(term);
    }

    // BindingVisitor

    /**
     * Resolve declared type, then visit RHS.
     */
    @Override
    public Object visit(final LetBinding let)
    {
        if (let.hasDeclaredType())
            typeRefResolver.resolve(let.getDeclaredType());

        if (!let.isIntrinsic()) 
        {
            final Term t = resolve(let.getValue());
            let.setValue(t); // might have changed
        }
        return null;
    }

    /**
     * Resolve typedef RHS, then do post-resolution init
     */
    @Override
    public Object visit(final TypeDef typeDef)
    {
        typeRefResolver.resolve(typeDef.getValue());

        return super.visit(typeDef);
    }

    @Override 
    protected Object visitTerm(final Term term) 
    {
        return termRefResolver.resolve(term);
    }

    @Override
    protected void visitUnboundTerm(final UnboundTerm unboundTerm)
    {
        final Term t = resolve(unboundTerm.getValue());
        unboundTerm.setValue(t);
    }

    /**
     * Helper - register a dependency from the current statement to
     * the given binding. Note that the dependency winds up being added
     * <strong>in the scope of the target binding (i.e., the binding being
     * depended upon).</strong>.
     * <p/>
     * This means that e.g. dependencies from inner statements to outer bindings
     * will be expressed at the level of the outer scope. The source of the dependency
     * (i.e., the depending statement) will be an outer-scope statement that encloses
     * the actual source statement.
     */
    void registerDependency(final Binding binding)
    {
        for (final ScopeState scopeState : getStateStack())
        {
            final Scope scope = scopeState.getScope();

            if (scope == binding.getScope())
            {
                final Statement statement = scopeState.getStatement();

                scope.addDependency(statement, binding);

                return;
            }
        }

        // TODO binding is an import
        assert binding.getScope() != null : "missing scope for dependency target";
    }

}
