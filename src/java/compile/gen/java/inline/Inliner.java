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
package compile.gen.java.inline;

import compile.gen.java.StatementFormatter;
import compile.term.ApplyTerm;

/**
 * Implementations define a {@link #tryInlining} method for particular
 * intrinsic functions.
 *
 * @author Basil Hosmer
 */
public interface Inliner
{
    /**
     * Attempt to generate inlined code for a call to an intrinsic
     * function, using the given formatter.
     *
     * The stmtsOkay flag indicates whether context in which the call occurs
     * is a freestanding statement or an expression.
     *
     * If successful, returns inlined code, otherwise null.
     */
    String tryInlining(ApplyTerm apply, StatementFormatter fmt, boolean stmtsOkay);
}
