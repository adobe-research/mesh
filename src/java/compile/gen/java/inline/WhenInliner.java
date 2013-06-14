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
import compile.type.Types;

import java.util.List;

/**
 * Try inlining calls to {@link runtime.intrinsic._when}.
 *
 * @author Basil Hosmer
 */
public class WhenInliner implements Inliner
{
    public String tryInlining(
        final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        if (!stmtsOkay)
            return null;

        final List<Term> args = ((TupleTerm)apply.getArg()).getItems();
        final String c = fmt.formatTermAs(args.get(0), boolean.class);

        final String block = InlinerUtils.formatBlockStmts(fmt, args.get(1), true);
        if (block == null)
            return null;

        return "if (" + c + ") { " + block + "; }";
    }
}
