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
import compile.type.Type;

import java.util.List;

/**
 * Try inlining calls to {@link runtime.intrinsic.If}.
 *
 * @author Basil Hosmer
 */
public class IfInliner implements Inliner
{
    public String tryInlining(final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        final List<Term> args = ((TupleTerm)apply.getArg()).getItems();
        final String c = fmt.formatTermAs(args.get(0), boolean.class);

        final String inlined;
        if (stmtsOkay)
        {
            final String t = InlinerUtils.formatBlockStmts(fmt, args.get(1));
            final String f = InlinerUtils.formatBlockStmts(fmt, args.get(2));

            inlined = "if (" + c + ") { " + t + "; } else { " + f + "; }";
        }
        else
        {
            final String t = InlinerUtils.formatBlockExpr(fmt, args.get(1));
            final String f = InlinerUtils.formatBlockExpr(fmt, args.get(2));

            final String expr = "((" + c + ") ? (" + t + ") : (" + f + "))";

            // note: here we can use the calculated result type,
            // since ?: is polymorphic in the generated code
            final Type applyType = apply.getType();

            inlined = fmt.fixup(apply.getLoc(), expr, applyType);
        }

        return inlined;
    }
}
