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
package compile.term.visit;

import compile.term.Binding;
import compile.term.LetBinding;
import compile.term.ParamBinding;
import compile.term.TypeDef;

/**
 * Binding visitor base implementation.
 *
 * @author Basil Hosmer
 */
public class BindingVisitorBase<T> extends TermVisitorBase<T> implements BindingVisitor<T>
{
    /**
     * helper - process a binding. Subclasses can override to do pre/post processing
     */
    protected T visitBinding(final Binding binding)
    {
        return binding.accept(this);
    }

    public T visit(final LetBinding let)
    {
        return let.isIntrinsic() ? null : visitTerm(let.getValue());
    }

    public T visit(final ParamBinding param)
    {
        return null;
    }

    public T visit(final TypeDef typeDef)
    {
        return null;
    }
}
