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
package compile.term;

import compile.Loc;
import compile.Session;
import compile.module.*;
import compile.term.visit.TermDumper;
import compile.term.visit.TermVisitor;
import compile.type.Type;
import compile.type.TypeParam;

import java.util.*;

/**
 * Note: we're both a Term and a Scope. Choosing here to extend
 * AbstractScope rather than AbstractTypedTerm for (relative)
 * cleanness of implementation.
 *
 * @author Basil Hosmer
 */
public final class LambdaTerm extends AbstractScope implements Term
{
    /**
     * Contains the calculated type of the term, set after type inference/checking
     */
    private final Signature signature;

    /**
     * Bindings we refer to that are defined in enclosing lambdas.
     */
    private final Map<String, ValueBinding> capturedLambdaBindings;

    /**
     * Bindings we refer to that are defined at the module level.
     */
    private final Map<String, ValueBinding> capturedModuleBindings;

    /**
     * Backpointer to parent scope (lambda or module).
     */
    private Scope parentScope;

    /**
     * Optional name to associate with this lambda, e.g. collected from
     * binding which defines it, if there is one
     */
    private String bindingName;

    /**
     * Lambda constructor.
     * Note that {@link #typeParamDecls} has explicitly declared type params;
     * inlines are added during resolution.
     */
    public LambdaTerm(
        final Loc loc,
        final List<TypeParam> typeParamDecls,
        final List<ParamBinding> params,
        final Type declaredResultType,
        final List<Statement> body)
    {
        super(loc, body);

        this.signature = new Signature(loc, typeParamDecls, params, declaredResultType);

        this.capturedLambdaBindings = new LinkedHashMap<String, ValueBinding>();
        this.capturedModuleBindings = new LinkedHashMap<String, ValueBinding>();

        this.parentScope = null;

        if (!params.isEmpty()) 
        {
            signature.commitParams(this);
        }

        validateBody();
    }

    /**
     * do basic syntactic check of body statements, ensure
     * that final (result) statement is an unbound term
     */
    private void validateBody()
    {
        if (body.size() == 0)
        {
            Session.error(loc, "lambda must contain at least a result expression");
        }
        else
        {
            // warn for unbound lambdas as non-results
            // TODO replace with late-stage check for pure, unbound non-results?
            for (int i = 0; i < body.size() - 1; i++)
            {
                final Statement s = body.get(i);
                if (!s.isBinding() && s instanceof UnboundTerm &&
                    ((UnboundTerm)s).getValue() instanceof LambdaTerm)
                {
                    Session.warn(s.getLoc(), "unbound lambda as statement");
                }
            }

            // make sure final statement is not a binding
            final Statement lastStatement = body.get(body.size() - 1);

            if (lastStatement.isBinding())
                Session.error(loc,
                    "final statement in lambda is not a result expression");
        }
    }

    /**
     *
     */
    public void addInlineParam(final ParamBinding param)
    {
        signature.addInlineParam(param);
    }

    /**
     * Ordered map of (value) param bindings.
     */
    public Map<String, ParamBinding> getParams()
    {
        return signature.getParams();
    }

    /**
     * Called once all params have been added.
     * 1. Lock param binding map.
     * 2. Fill gaps if inline params have been used.
     * 3. Build our declared type.
     * NOTE: declared type is built regardless of whether any annotations
     * were supplied, so {@link #hasDeclaredType()} is nontrivial.
     */
    public void commitParams()
    {
        signature.commitParams(this);
    }

    public boolean getParamsCommitted()
    {
        return signature.getParamsCommitted();
    }

    public Type getSignatureType()
    {
        return signature.getSignatureType();
    }

    /**
     * Return the given param type from our declared signature.
     */
    public Type getSignatureParamType(final int index)
    {
        return signature.getSignatureParamType(index);
    }

    /**
     * Return the type of param at given position, from our composite type.
     */
    public Type getParamType(final int index)
    {
        return signature.getParamType(index);
    }

    public Type getDeclaredResultType()
    {
        return signature.getDeclaredResultType();
    }

    public Map<String, TypeParam> getTypeParamDecls()
    {
        return signature.getTypeParamDecls();
    }

    public Map<String, ValueBinding> getCapturedLambdaBindings()
    {
        return capturedLambdaBindings;
    }

    public boolean hasCapturedLambdaBindings()
    {
        return !capturedLambdaBindings.isEmpty();
    }

    public List<Statement> getNonResultStatements()
    {
        return body.subList(0, body.size() - 1);
    }

    public UnboundTerm getResultStatement()
    {
        return (UnboundTerm)body.get(body.size() - 1);
    }

    public String getBindingName()
    {
        return bindingName;
    }

    public void setBindingName(final String bindingName)
    {
        this.bindingName = bindingName;
    }

    public boolean hasBindingName()
    {
        return bindingName != null;
    }

    // Term

    public boolean isConstant()
    {
        return false;
    }

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Typed

    public Type getType()
    {
        return signature.getType();
    }

    public void setType(final Type type)
    {
        signature.setType(type);
    }

    /**
     * True if any part of our signature carries a declared type.
     */
    public boolean hasDeclaredType()
    {
        return signature.hasDeclaredType();
    }

    public Type getDeclaredType()
    {
        return signature.getDeclaredType();
    }

    public void setDeclaredType(final Type type)
    {
        signature.setDeclaredType(type);
    }

    // Dumpable

    public String dump()
    {
        return TermDumper.dump(this);
    }

    //
    // Scope
    //

    public Scope getParentScope()
    {
        return parentScope;
    }

    public void setParentScope(final Scope parentScope)
    {
        this.parentScope = parentScope;
    }

    public Module getModule()
    {
        return parentScope.getModule();
    }

    public boolean isLambda()
    {
        return true;
    }

    /**
     * Note that we track variable capture from calls
     * made to this method.
     */
    public ValueBinding findValueBinding(final String name)
    {
        final ValueBinding localValueBinding = getLocalValueBinding(name);
        if (localValueBinding != null)
            return localValueBinding;

        // check already-captured outer lambda bindings
        final ValueBinding capturedLambdaBinding = capturedLambdaBindings.get(name);
        if (capturedLambdaBinding != null)
            return capturedLambdaBinding;

        // check already-captured global bindings
        final ValueBinding capturedGlobalBinding = capturedModuleBindings.get(name);
        if (capturedGlobalBinding != null)
            return capturedGlobalBinding;

        // look up in enclosing scope
        final ValueBinding outerValueBinding = parentScope.findValueBinding(name);
        if (outerValueBinding != null)
        {
            captureValueBinding(outerValueBinding);
            return outerValueBinding;
        }

        return null;
    }

    /**
     * Lambda-specific: if given binding comes from a
     * foreign scope, add to one of our captured binding
     * maps.
     */
    public void captureValueBinding(final ValueBinding binding)
    {
        final Scope scope = binding.getScope();
        // assert isAncestorScope(scope);

        if (scope == this)
            return;

        final Map<String, ValueBinding> capturedBindingMap =
            scope.isLambda() ? capturedLambdaBindings :
                capturedModuleBindings;

        final ValueBinding prev = capturedBindingMap.get(binding.getName());

        if (prev == null)
        {
            if (binding.isLet())
            {
                // don't capture recursive refs to ourselves
                final LetBinding let = (LetBinding)binding;
                if (!let.isIntrinsic() && let.getValue() == this)
                    return;
            }

            capturedBindingMap.put(binding.getName(), binding);
        }
        else if (prev != binding)
        {
            Session.error(binding.getLoc(),
                "internal error: captured name \"{0}\" previously bound to value {1}, new binding is {2}",
                binding.getName(), prev.dump(), binding.dump());
        }
    }

    public ValueBinding getLocalValueBinding(final String name)
    {
        final ParamBinding param = signature.getParams().get(name);
        if (param != null)
            return param;

        final LetBinding let = lets.get(name);
        if (let != null)
            return let;

        return null;
    }

    public void addLet(final LetBinding let)
    {
        if (signature.getParams().containsKey(let.getName()))
            Session.error(let.getLoc(),
                "internal error: param \"{0}\" shadowed by new local binding {1}",
                let.getName(), let.dump());

        super.addLet(let);
    }

    public TypeBinding findTypeBinding(final String name)
    {
        final TypeBinding localType = getLocalTypeBinding(name);
        return localType != null ? localType : parentScope.findTypeBinding(name);
    }

    public TypeBinding getLocalTypeBinding(final String name)
    {
        // if signature has explicit (thus visible) type params,
        // they will have been committed.
        if (signature.getParamsCommitted())
        {
            final TypeParam param = signature.getSignatureType().getParam(name);
            if (param != null)
                return param;
        }

        final TypeDef localTypeDef = typeDefs.get(name);
        if (localTypeDef != null)
            return localTypeDef;

        return null;
    }
}