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
import compile.term.visit.ConstantTermReducer;
import runtime.ConfigUtils;

import java.util.List;

/**
 * Simple in-module constant folding and propagation.
 * Logic here is just to traverse the module, real
 * work happens in {@link compile.term.visit.ConstantTermReducer}.
 *
 * @author Basil Hosmer
 */
public final class ConstantReducer extends ModuleVisitor<Object>
{
    /**
     * system property enables/disables CG inlining
     */
    private static final boolean ENABLED =
        ConfigUtils.parseBoolProp(ConstantReducer.class.getName() + ".ENABLED", true);

    //
    // instance
    //

    private final ConstantTermReducer reducer;

    public ConstantReducer(final Module module)
    {
        super(module);
        reducer = new ConstantTermReducer(this);
    }

    public boolean reduce()
    {
        if (!ENABLED)
            return true;

        if (Session.isDebug())
            Session.debug(getModule().getLoc(), "Constant fold/prop...");

        return process();
    }

    // ModuleVisitor

    @Override
    protected void processScope(final Scope scope)
    {
        for (final List<Statement> group : scope.getDependencyGroups())
            processGroup(group);
    }

    /**
     * Within each group, reduce lets and then unbound values.
     */
    private void processGroup(final List<Statement> group)
    {
        for (final Statement statement : group)
            if (statement.isBinding() && ((Binding)statement).isLet())
                processStatement(statement);

        for (final Statement statement : group)
            if (!statement.isBinding())
                processStatement(statement);
    }


    @Override
    protected void visitUnboundTerm(final UnboundTerm unboundTerm)
    {
        final Term value = unboundTerm.getValue();
        final Term newValue = reducer.reduce(value);

        if (newValue != value)
            unboundTerm.setValue(newValue);
    }


    // BindingVisitor

    /**
     * Add binding to current scope, after checking for redeclaration.
     */
    @Override
    public Object visit(final LetBinding let)
    {
        final Term value = let.getValue();
        final Term newValue = reducer.reduce(value);

        if (newValue != value)
            let.setValue(newValue);

        return null;
    }
}
