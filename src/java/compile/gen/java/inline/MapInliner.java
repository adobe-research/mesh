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
import runtime.intrinsic._mapz;
import runtime.intrinsic._zip;
import runtime.rep.Tuple;
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;

import java.util.List;

/**
 * Try inlining calls to {@link runtime.intrinsic._map}.
 * In particular, translate map(zip(...), f) into mapz((...), f).
 *
 * TODO: not a late-stage inline. Move to *Reducer stage
 *
 * @author Basil Hosmer
 */
public class MapInliner implements Inliner
{
    public String tryInlining(
        final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        final List<Term> args = ((TupleTerm)apply.getArg()).getItems();

        final Term listArg = args.get(0);
        final Term funcArg = args.get(1);

        final Term zipLists =
            InlinerUtils.derefToIntrinsicApply(listArg, _zip.INSTANCE, fmt);

        final String exprFmt;
        if (zipLists != null && zipLists instanceof TupleTerm)
        {
            final String funcFmt = fmt.formatTermAs(funcArg, Lambda.class);

            final TupleTerm zipListsTuple = (TupleTerm)zipLists;
            final List<Term> items = zipListsTuple.getItems();
            if (items.size() == 2)
            {
                // map(zip(x, y), f) => _mapz.invoke2(x, y, f)
                final String listxFmt = fmt.formatTermAs(items.get(0), ListValue.class);
                final String listyFmt = fmt.formatTermAs(items.get(1), ListValue.class);
                exprFmt = _mapz.class.getName() + ".invoke2(" +
                    listxFmt + ", " + listyFmt + ", " + funcFmt + ")";
            }
            else
            {
                // transform map(zip(x, y, ..), f) into mapz((x, y, ..), f)
                final String listsFmt = fmt.formatTermAs(zipLists, Tuple.class);
                exprFmt = _mapz.class.getName() + ".invoke(" +
                    listsFmt + ", " + funcFmt + ")";
            }
        }
        else
        {
            // list arg is not a call to zip(), just inline _map.invoke()
            final String argsFmt = fmt.formatTermAs(listArg, ListValue.class);
            final String funcFmt = fmt.formatTermAs(funcArg, Lambda.class);
            exprFmt = "(" + argsFmt + ").apply(" + funcFmt + ")";
        }

        return fmt.fixup(apply.getLoc(), exprFmt, ListValue.class);
    }
}
