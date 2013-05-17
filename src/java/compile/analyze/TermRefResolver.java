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
import compile.parse.ApplyFlavor;
import compile.term.*;
import compile.term.visit.TermTransformerBase;

class TermRefResolver extends TermTransformerBase {

    private final RefResolver refResolver;
    private int noBindingErrorSupression;

    TermRefResolver(final RefResolver refResolver)
    {
        this.refResolver = refResolver;
    }

    public Term resolve(final Term term)
    {
        return visitTerm(term);
    }

    private boolean emitNoBindingError() { return noBindingErrorSupression == 0; }
    private void supressNoBindingError() { ++noBindingErrorSupression; }
    private void unsupressNoBindingError() { --noBindingErrorSupression; }

    private static ApplyTerm asApplyTerm(final Term t) 
    {
        return t instanceof ApplyTerm ? (ApplyTerm)t : null;
    }

    private static RefTerm asRefTerm(final Term t) 
    {
        return t instanceof RefTerm ? (RefTerm)t : null;
    }

    private static SymbolLiteral asSymbolLiteral(final Term t) 
    {
        return t instanceof SymbolLiteral ? (SymbolLiteral)t : null;
    }

    private static boolean mightBeQualifier(final ApplyTerm apply) 
    {
        final SymbolLiteral symbolArg = asSymbolLiteral(apply.getArg());
        final RefTerm refBase = asRefTerm(apply.getBase());
        final ApplyTerm applyBase = asApplyTerm(apply.getBase());
        
        return symbolArg != null && 
               apply.getFlav() == ApplyFlavor.StructAddr &&
               ((refBase != null) || (applyBase != null && mightBeQualifier(applyBase)));
    }

    @Override
    public Term visit(final ApplyTerm apply)
    {
        if (mightBeQualifier(apply)) 
        {
            // We have a potential qualified name here.  Namespace qualification looks 
            // suspiciously like struct access (i.e., an apply of a constant symbol) 
            // because... well because that's exactly what they are as far as the parser
            // is concerned.
            //
            // So, try first to resolve the base as normal.  If that fails, then replace
            // this entire term with a RefTerm with a namespace qualifier.

            if (Session.isDebug()) 
                Session.debug("Found potential qualified name: {0}", apply.dump());

            supressNoBindingError();
            final Term newTerm = super.visit(apply);
            unsupressNoBindingError();

            final ApplyTerm newApply = asApplyTerm(newTerm);

            if (newApply != null) 
            {
                final RefTerm ref = asRefTerm(newApply.getBase());
                if (ref != null && ref.getBinding() == null) 
                {
                    final SymbolLiteral sym = asSymbolLiteral(newApply.getArg());
                    // Base arg reference didn't work out.  Try a qualified reference instead,
                    // and replace this term with the new reference.
                    final RefTerm qualifiedRef = new RefTerm(apply.getLoc(), ref, sym.getValue());

                    if (Session.isDebug()) 
                        Session.debug("Replacing apply term: {0} with ref term: {1}",
                                newApply.dump(), qualifiedRef.dump());

                    return visit(qualifiedRef);
                }
            }

            return newTerm;
        }
        else
        {
            return super.visit(apply);
        }
    }

    /**
     * If reference is unresolved, resolve it, check for in-scope forward
     * refs, and track value dependency.
     * <p/>
     * Note: for now we prohibit forward var refs from inner lambda scopes
     * to outer lambda scopes. This is an issue, as it precludes mutually-
     * recursive local functions. Allowing them would require: a) omitting
     * the check in this function, b) generalizing {@link RefChecker} to
     * perform the same check as for forward refs from inner lambda scopes
     * to top-level bindings, and c) either reorder statements to preserve
     * the current invariant that closed-over variables all exist when a
     * closure is created, or modify the codegen strategy to no longer
     * rely on this being the case.
     */
    @Override
    public Term visit(final RefTerm ref)
    {
        final Scope currentScope = refResolver.getCurrentScope();

        final Loc loc = ref.getLoc();
        final String name = ref.getName();

        final ValueBinding binding;

        if (ref.getBinding() != null)
        {
            // some refs are already bound, e.g. inline param refs,
            // and refs from some decomposing assignments
            binding = ref.getBinding();
            final Scope bindingScope = binding.getScope();

            if (Session.isDebug())
                Session.debug(loc, "name ''{0}'' prebound to binding {1} from {2}",
                    ref.getName(), binding.dump(), binding.getLoc());

            if (bindingScope.isLambda())
            {
                // we need to make sure that each lambda between ourselves
                // and the target of our reference captures the reference.
                for (Scope captureScope = currentScope;
                    captureScope.isLambda() && captureScope != bindingScope;
                    captureScope = captureScope.getParentScope())
                {
                    if (Session.isDebug())
                        Session.debug(loc, "capturing binding {0} from scope {1} in lambda {2}",
                            binding.dump(), bindingScope.dump(), captureScope.dump());

                    ((LambdaTerm)captureScope).captureValueBinding(binding);
                }
            }
        }
        else
        {
            final Module module = refResolver.getModule();
            final ValueBinding vb = currentScope.findValueBinding(name);

            binding = (vb != null) ? vb : module.findValueBinding(name);

            // error cases:

            // no binding
            if (binding == null)
            {
                if (emitNoBindingError()) 
                   Session.error(loc, "no value binding found for name {0}", name);
                return super.visit(ref);
            }

            // self-reference
            if (binding == refResolver.getCurrentStatement())
            {
                Session.error(loc, "self-reference in definition of {0}", name);
                return super.visit(ref);
            }

            if (loc.isBefore(binding.getLoc()))
            {
                final Scope bindingScope = binding.getScope();

                // in-scope forward ref
                if (currentScope == bindingScope)
                {
                    Session.error(loc, "forward reference to variable {0} established at {1}",
                        binding.getName(), binding.getLoc());

                    return super.visit(ref);
                }

                // prohibit forward refs to outer lambda scopes.
                // see comment header
                if (bindingScope.isLambda() && !(binding instanceof ParamBinding))
                {
                    Session.error(loc,
                        "forward references to ambient local variables not yet supported: {0} at {1}",
                        binding.getName(), binding.getLoc());

                    return super.visit(ref);
                }
            }

            // ok

            if (Session.isDebug())
                Session.debug(loc, "resolving name ''{0}'' to binding {1} from {2}",
                    name, binding.dump(), binding.getLoc());

            ref.setBinding(binding);
        }

        if (!(binding instanceof ParamBinding))
            refResolver.registerDependency(binding);

        assert ref.getBinding() != null : "no binding";

        return super.visit(ref);
    }


    /**
     * Resolve declared type, then traverse body.
     */
    @Override
    public Term visit(final LambdaTerm lambda)
    {
        if (lambda.hasDeclaredType())
            refResolver.getTypeRefResolver().resolve(lambda.getDeclaredType());

        refResolver.visit(lambda);
        return super.visit(lambda);
    }

    @Override
    public Term visit(final CoerceTerm coerce)
    {
        refResolver.getTypeRefResolver().resolve(coerce.getType());

        return super.visit(coerce);
    }
}
