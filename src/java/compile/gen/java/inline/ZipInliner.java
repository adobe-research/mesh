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
 * Try inlining calls to {@link runtime.intrinsic._zip}.
 * In particular, call private entry points for fixed sizes.
 *
 * @author Basil Hosmer
 */
public class ZipInliner implements Inliner
{
    public String tryInlining(
        final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        final List<Term> args = ((TupleTerm)apply.getArg()).getItems();

        if (args.size() != 2)
            return null;

        // zip.invoke(tuple(x, y)) => zip.invoke2(x, y)
        final String listxFmt = fmt.formatTermAs(args.get(0), ListValue.class);
        final String listyFmt = fmt.formatTermAs(args.get(1), ListValue.class);
        final String exprFmt = _zip.class.getName() +
            ".invoke2(" + listxFmt + ", " + listyFmt + ")";

        return fmt.fixup(apply.getLoc(), exprFmt, ListValue.class);
    }
}
