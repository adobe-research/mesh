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

import compile.module.Scope;
import compile.term.visit.BindingVisitor;

/**
 * Binds a name within a scope.
 *
 * @author Basil Hosmer
 */
public interface Binding extends Statement
{
    /**
     *
     */
    String getName();

    /**
     * True if we're an instance of {@link LetBinding}
     */
    boolean isLet();

    /**
     *
     */
    Scope getScope();

    /**
     *
     */
    void setScope(Scope scope);

    /**
     * visitor dispatching
     */
    <T> T accept(BindingVisitor<T> visitor);
}
