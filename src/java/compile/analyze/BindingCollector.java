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
import compile.module.Scope;
import compile.term.*;
import compile.type.Type;
import compile.type.TypeCons;
import compile.type.TypeParam;

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * {@link Module} analyzer. {@link #collect} method collects bindings from
 * module's statement list and adds them to its binding maps, along with
 * semantic checking, e.g. for shadowing.
 *
 * @author Basil Hosmer
 */
public final class BindingCollector extends ModuleVisitor<Object>
{
    private ArrayDeque<Statement> generated;
    
    public BindingCollector(final Module module)
    {
        super(module);
    }

    /**
     *
     */
    public boolean collect()
    {
        if (Session.isDebug())
            Session.debug(getModule().getLoc(), "Collecting bindings...");

        return process();
    }

    // ModuleVisitor

    /**
     * overriden to remove type bindings from statement list after scope traveral
     * has added them to type bindings map
     */
    @Override
    protected void processScope(final Scope scope)
    {
        final ArrayDeque<Statement> save = generated;
        generated = new ArrayDeque<Statement>();

        super.processScope(scope);

        // process generated statements and add to body
        while (!generated.isEmpty())
        {
            final Statement gen = generated.pop();
            
            if (Session.isDebug())
                Session.debug(gen.getLoc(), "processing generated statement {0}",
                    gen.dump());

            processStatement(gen);
            scope.getBody().add(gen);
        }

        generated = save;

        // remove typedefs from body list
        for (Iterator<Statement> iter = scope.getBody().iterator(); iter.hasNext(); )
            if (iter.next() instanceof TypeDef)
                iter.remove();
    }

    // BindingVisitor

    /**
     * Add binding to current scope, after checking for redeclaration.
     */
    @Override
    public Object visit(final LetBinding let)
    {
        Session.pushErrorCount();

        // check in current scope for previous binding under this name, then add
        final String name = let.getName();
        final Scope scope = getCurrentScope();

        final ValueBinding prev = scope.getValueBinding(name);
        if (prev == null)
        {
            // first in-scope appearance of this name
            if (Session.isDebug())
                Session.debug(let.getLoc(), "adding var binding {0}", let.dump());
        }
        else if (prev.getScope() != scope)
        {
            // shadowing out-of-scope bindings is fine.
            // Note: a ref to x followed by an binding of x within the same
            // scope will be flagged as an illegal forward, regardless of
            // whether an outer binding of x is visible.
            if (Session.isDebug())
                Session.debug(let.getLoc(),
                    "new let {0} shadows out-of-scope binding from {1}",
                    let.dump(), prev.getLoc());
        }
        else
        {
            // in-scope shadowing is not fine
            if (prev.isLet())
            {
                // let redefinition
                Session.error(let.getLoc(),
                    "new let {0} shadows previous definition at {1}",
                    let.dump(), prev.getLoc());
            }
            else
            {
                // a let shadowing a param is not fine
                Session.error(let.getLoc(),
                    "new let {0} conflicts with parameter declared at {1}",
                    let.dump(), prev.getLoc());
            }
        }

        if (Session.popErrorCount() == 0)
        {
            scope.addLet(let);

            // collect bindings in declared type
            if (let.hasDeclaredType())
                let.getDeclaredType().collectInlineParams();
        }

        // visit binding rhs
        final Object result = super.visit(let);

        // if our RHS is a lambda, we stash the binding name for use in CG
        // ...this is just for readability in our current primitive debug env
        final Term value = let.getValue();
        if (value instanceof LambdaTerm)
            ((LambdaTerm)value).setBindingName(name);

        return result;
    }

    /**
     * Add type def to current scope, check for type param shadowing.
     * If new type is nominal, save generated type constructor and
     * destructor to {@link #generated} for later addition to scope
     * {@link compile.module.Scope#getBody() body}.
     */
    @Override
    public Object visit(final TypeDef def)
    {
        if (def.isUnresolvedIntrinsic())
        {
            if (!def.resolveIntrinsic())
            {
                Session.error(def.getLoc(),
                        "unresolved intrinsic type {0}", def.getName());
                return null;
            }
        }

        final String name = def.getName();
        final Scope scope = getCurrentScope();

        final TypeBinding prev = scope.getTypeDef(name);

        if (prev != null)
        {
            if (prev instanceof TypeParam)
            {
                Session.error(def.getLoc(),
                    "type def {0} conflicts with type parameter declared at {1}",
                    def.getName(), prev.getLoc());
            }
            else
            {
                assert prev instanceof TypeDef;
                assert prev.getScope() == scope; // checks getType impl

                Session.error(def.getLoc(),
                    "redefinition of type {0}, previously defined at {1}",
                    def.getName(), prev.getLoc());
            }
        }
        else
        {
            final Type type = def.getValue();

            final Type paramHost =
                type instanceof TypeCons && ((TypeCons)type).getBody() != null ?
                    ((TypeCons)type).getBody() : type;

            final int nparams = paramHost.getParams().size();

            // this will raise error on explicitly-parameterized types
            paramHost.collectInlineParams();

            if (nparams < paramHost.getParams().size())
            {
                Session.error(def.getLoc(),
                    "type def cannot contain inline type params");

                return def;
            }

            if (Session.isDebug())
                Session.debug(def.getLoc(), "adding type def {0}", def.dump());

            scope.addTypeDef(def);

            if (def.isNominal())
            {
                final LetBinding ctorLet = def.getCtorLet();
                final LetBinding dtorLet = def.getDtorLet();

                if (Session.isDebug())
                    Session.debug(def.getLoc(), "adding type ctor let {0}, dtor let {1}",
                        ctorLet.dump(), dtorLet.dump());

                generated.add(ctorLet);
                generated.add(dtorLet);
            }
        }

        return super.visit(def);
    }

    // TermVisitor

    /**
     * Inline param refs come from the parser pre-resolved to param bindings.
     * These need to be collected, merged and attached to their host lambdas.
     * Note that the host lambda may not be the innermost lambda.
     */
    @Override
    public Object visit(final RefTerm ref)
    {
        final ValueBinding binding = ref.getBinding();

        // ordinary case: ref terms not yet bound
        if (binding == null)
            return super.visit(ref);

        assert binding instanceof ParamBinding : "corrupt inline param ref";

        final ParamBinding paramBinding = (ParamBinding)binding;

        // now, find the host lambda by counting dollar signs

        Scope scope = getCurrentScope();
        String name = paramBinding.getName();

        if (!name.startsWith("$"))
        {
            Session.error(ref.getLoc(),
                "internal error: malformed name in inline param ref: {0}",
                paramBinding.getName());

            return super.visit(ref);
        }

        // up one lambda for each '$'
        while (name.substring(1).startsWith("$") && scope.isLambda())
        {
            scope = scope.getParentScope();
            name = name.substring(1);
        }

        // too far?
        if (!scope.isLambda())
        {
            Session.error(ref.getLoc(),
                "inline param ref {0} does not resolve to lambda", ref.getName());

            return super.visit(ref);
        }

        // found the right lambda, is it eligible?
        final LambdaTerm lambdaTerm = (LambdaTerm)scope;

        if (lambdaTerm.getParamsCommitted())
        {
            // if lambda has explicit param list, inline params cannot be used
            Session.error(ref.getLoc(),
                "inline param ref {0} resolves to lambda with declared parameters",
                ref.getName());

            return super.visit(ref);
        }

        // process legit inline param ref.
        // use lambda's location to ensure name uniqueness

        final Loc loc = lambdaTerm.getLoc();
        name = name + "_" + loc.getLine() + "_" + loc.getColumn();

        final ValueBinding existingBinding = lambdaTerm.getValueBinding(name);

        if (existingBinding == null)
        {
            // first time we've seen this one. fix up name and install on host
            final ParamBinding fixed =
                new ParamBinding(paramBinding.getLoc(), name, null, true);

            if (Session.isDebug())
                Session.debug(paramBinding.getLoc(), "adding implicit param binding {0}",
                    fixed.dump());

            lambdaTerm.addInlineParam(fixed);

            // repoint ref term
            ref.setName(fixed.getName());
            ref.setBinding(fixed);
        }
        else
        {
            // inline param names should be inexpressible, in which case it's
            // impossible to name and refer to a let-bound variable by this name
            assert existingBinding instanceof ParamBinding;

            // we've seen it before - repoint ref to existing binding
            // note the rename
            ref.setName(existingBinding.getName());
            ref.setBinding(existingBinding);
        }

        return super.visit(ref);
    }

    /**
     * 1. set lambda parent scope.
     * 2. traverse lambda body.
     * 3. commit params, if not yet committed (if lambda contained an explicit
     * param list, they were committed at parse time, otherwise not)
     */
    @Override
    public Object visit(final LambdaTerm lambda)
    {
        final Scope lambdaParentScope = lambda.getParentScope();

        if (lambdaParentScope == null)
            lambda.setParentScope(getCurrentScope());
        else
            assert lambdaParentScope == getCurrentScope() : "invalid lambda parent scope";

        final Object result = super.visit(lambda);

        if (!lambda.getParamsCommitted())
        {
            if (Session.isDebug())
                Session.debug(lambda.getLoc(), "committing param bindings...");

            lambda.commitParams();
        }

        return result;
    }
}
