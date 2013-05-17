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
import compile.term.visit.BindingVisitorBase;

import java.util.ArrayDeque;

/**
 * Module traversal base implementation.
 *
 * @author Basil Hosmer
 */
public abstract class ModuleVisitor<T> extends BindingVisitorBase<T>
{
    private final Module module;
    private final ArrayDeque<ScopeState> stateStack;

    protected ModuleVisitor(final Module module)
    {
        this.module = module;
        this.stateStack = new ArrayDeque<ScopeState>();
    }

    protected final Module getModule()
    {
        return module;
    }

    protected boolean process()
    {
        Session.pushErrorCount();
        pushScope(module);
        processScope(module);
        popScope();
        return Session.popErrorCount() == 0;
    }

    private void pushScope(final Scope scope)
    {
        stateStack.push(new ScopeState(scope, null));
    }

    private void popScope()
    {
        stateStack.pop();
    }

    protected void processScope(final Scope scope)
    {
        processTypeDefs(scope);
        processParamBindings(scope);
        processStatements(scope);
    }

    protected void processTypeDefs(final Scope scope)
    {
        for (final TypeDef typeDef : scope.getTypeDefs().values())
        {
            setCurrentStatement(typeDef);
            visitBinding(typeDef);
        }
    }

    protected void processParamBindings(final Scope scope)
    {
        for (final ParamBinding param : scope.getParams().values())
        {
            setCurrentStatement(param);
            visitBinding(param);
        }
    }

    protected void processStatements(final Scope scope)
    {
        for (final Statement statement : scope.getBody())
            processStatement(statement);
    }

    protected void processStatement(final Statement statement)
    {
        setCurrentStatement(statement);

        if (statement.isBinding())
        {
            visitBinding((Binding)statement);
        }
        else if (statement instanceof ImportStatement) 
        {
            visitImportStatement((ImportStatement)statement);
        }
        else if (statement instanceof ExportStatement) 
        {
            visitExportStatement((ExportStatement)statement);
        }
        else
        {
            assert statement instanceof UnboundTerm;
            visitUnboundTerm((UnboundTerm)statement);
        }
    }

    protected void visitImportStatement(final ImportStatement stmt)
    {
    }

    protected void visitExportStatement(final ExportStatement stmt)
    {
    }

    protected void visitUnboundTerm(final UnboundTerm unboundTerm)
    {
        visitTerm(unboundTerm.getValue());
    }

    protected final Statement getCurrentStatement()
    {
        assert !stateStack.isEmpty();
        return stateStack.peek().getStatement();
    }

    protected final void setCurrentStatement(final Statement statement)
    {
        assert !stateStack.isEmpty();
        stateStack.peek().setStatement(statement);
    }

    protected final Scope getCurrentScope()
    {
        assert !stateStack.isEmpty();
        return stateStack.peek().getScope();
    }

    protected final ArrayDeque<ScopeState> getStateStack()
    {
        return stateStack;
    }

    // TermVisitor

    @Override
    public T visit(final LambdaTerm lambda)
    {
        pushScope(lambda);
        processScope(lambda);
        popScope();
        return null;
    }

    // local classes

    protected static class ScopeState
    {
        private final Scope scope;
        private Statement statement;

        ScopeState(final Scope scope, final Statement statement)
        {
            this.scope = scope;
            this.statement = statement;
        }

        public Scope getScope()
        {
            return scope;
        }

        public Statement getStatement()
        {
            return statement;
        }

        public void setStatement(final Statement statement)
        {
            this.statement = statement;
        }
    }
}
