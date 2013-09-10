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
import runtime.intrinsic._map;
import runtime.intrinsic._mapred;
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;

import java.util.List;

/**
 * Try inlining calls to {@link runtime.intrinsic._reduce}.
 *
 * In particular, we want to transform
 *
 *      reduce(r, i, vs | f)
 *
 * into
 *
 *      mapred(r, i, vs, f)
 *
 * TODO: not a late-stage inline. Move to *Reducer stage
 *
 * @author Basil Hosmer
 */
public class ReduceInliner implements Inliner
{
    public String tryInlining(
        final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        final List<Term> args = ((TupleTerm)apply.getArg()).getItems();

        final Term valsArg = args.get(2);
        final Term mapArgs =
            InlinerUtils.derefToIntrinsicApply(valsArg, _map.INSTANCE, fmt);

        if (mapArgs != null && mapArgs instanceof TupleTerm)
        {
            final TupleTerm mapArgsTuple = (TupleTerm)mapArgs;

            final List<Term> items = mapArgsTuple.getItems();
            assert items.size() == 2;

            final Term mapList = items.get(0);
            final Term mapFunc = items.get(1);

            final String exprFmt = _mapred.class.getName() + ".invoke(" +
                fmt.formatTermAs(args.get(0), Lambda.class) + ", " +
                fmt.formatTermAs(args.get(1), Object.class) + ", " +
                fmt.formatTermAs(mapList, ListValue.class) + ", " +
                fmt.formatTermAs(mapFunc, Lambda.class) + ")";

            return fmt.fixup(apply.getLoc(), exprFmt, Object.class);
        }

        return null;
    }
}
