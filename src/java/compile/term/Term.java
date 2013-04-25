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

import compile.term.visit.TermVisitor;

/**
 * Common interface for all value terms.
 *
 * @author Basil Hosmer
 */
public interface Term extends Typed
{
    /**
     * visitor dispatch
     */
    <T> T accept(TermVisitor<T> visitor);

    /**
     * True if this term represents a compile-time constant value.
     * Two constant terms representing the same value must test equal
     * at compile time. Examples include both atomic literals like
     * {@link StringLiteral}, {@link IntLiteral}, and structures
     * like {@link TupleTerm} where all subterms are constant.
     */
    boolean isConstant();
}
