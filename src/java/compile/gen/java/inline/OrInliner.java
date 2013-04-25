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

import java.util.List;

/**
 * Try inlining calls to {@link runtime.intrinsic.Or}.
 *
 * @author Basil Hosmer
 */
public class OrInliner implements Inliner
{
    public String tryInlining(
        final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        final List<Term> args = ((TupleTerm)apply.getArg()).getItems();
        final String l = fmt.formatTermAs(args.get(0), boolean.class);

        final String inlined;
        if (stmtsOkay)
        {
            final String r = InlinerUtils.formatBlockStmts(fmt, args.get(1));

            inlined = "if (!(" + l + ")) { " + r + "; }";
        }
        else
        {
            final String r = InlinerUtils.formatBlockExpr(fmt, args.get(1));
            final String expr = "((" + l + ") || (" + r + "))";

            inlined = fmt.fixup(apply.getLoc(), expr, boolean.class);
        }

        return inlined;
    }
}
