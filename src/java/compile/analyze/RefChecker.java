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

import compile.Loc;
import compile.Session;
import compile.module.Module;
import compile.term.ApplyTerm;
import compile.term.LetBinding;
import compile.term.RefTerm;
import compile.term.ValueBinding;

import java.util.Map;

/**
 * Enforces the language rule that code within a lambda may make
 * forward references to bindings in the enclosing environment,
 * as long as all such bindings have been defined by the time the
 * lambda actually runs. The current algorithm is overly conservative
 * in two ways: a) lambdas passed as arguments are considered as running
 * at the call site, which may not be the case depending on what happens
 * within the called function. Much worse, b) forward references from
 * lambdas to bindings within *enclosing lambdas* (rather than at the
 * module level) are not currently checked here at all--instead they're
 * just prohibited by {@link RefResolver}. (a) is tolerable, but (b)
 * should be fixed, as it violates the language definition and e.g.
 * prevents mutually recursive local functions.
 *
 * @author Basil Hosmer
 */
public final class RefChecker extends ModuleVisitor<Object>
{
    private final Map<LetBinding, Loc> lastReached;

    private ApplyTerm curApply;

    public RefChecker(final Module module)
    {
        super(module);
        lastReached = new LastReached(getModule()).calc();
    }

    public boolean check()
    {
        if (Session.isDebug())
            Session.debug(getModule().getLoc(), "Checking refs...");

        return process();
    }

    // TermVisitor

    /**
     * NOTE: we check references to top-level lets only.
     *
     */
    @Override
    public Object visit(final RefTerm ref)
    {
        if (curApply == null)
            return null;

        final ValueBinding binding = ref.getBinding();

        if (!binding.isLet())
            return null;

        final LetBinding let = (LetBinding)binding;

        if (let.getScope() != getModule())
            return null;

        final Loc last = lastReached.get(let);

        assert last != null;

        if (Session.isDebug())
            Session.debug(curApply.getLoc(), "{0} reaches {1} at {2}, last = {3}",
                curApply.dump(), let.getName(), let.getLoc(), last);

        if (curApply.getLoc().isBefore(last))
        {
            Session.error(curApply.getLoc(),
                "uninitialized variable defined at {0} is reachable from function call at {1}",
                last, curApply.getLoc());
        }

        return null;
    }

    /**
     * trace all apply terms and subterms at the top level
     */
    @Override
    public Object visit(final ApplyTerm apply)
    {
        if (curApply != null || !getCurrentScope().isLambda())
        {
            final ApplyTerm save = curApply;

            curApply = apply;

            super.visit(apply);

            curApply = save;
        }

        return null;
    }
}