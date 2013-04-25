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

import compile.term.LetBinding;
import compile.term.ParamBinding;
import compile.term.TypeDef;

/**
 * Visits bindings and the terms within them (hence extends TermVisitor).
 *
 * @author Basil Hosmer
 */
public interface BindingVisitor<T> extends TermVisitor<T>
{
    /**
     *
     */
    T visit(final LetBinding let);

    /**
     *
     */
    T visit(final ParamBinding param);

    /**
     *
     */
    T visit(final TypeDef typeDef);
}