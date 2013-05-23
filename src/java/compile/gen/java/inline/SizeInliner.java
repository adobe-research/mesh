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
import compile.term.Term;
import compile.term.TupleTerm;
import runtime.intrinsic._zip;
import runtime.rep.list.ListValue;

import java.util.List;

/**
 * Inline calls to {@link runtime.intrinsic._size}.
 *
 * @author Basil Hosmer
 */
public class SizeInliner implements Inliner
{
    public String tryInlining(
        final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        final String listFmt = fmt.formatTermAs(apply.getArg(), ListValue.class);
        final String exprFmt = "(" + listFmt + ").size()";
        return fmt.fixup(apply.getLoc(), exprFmt, int.class);
    }
}
