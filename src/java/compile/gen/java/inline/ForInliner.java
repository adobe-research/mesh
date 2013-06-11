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
import compile.term.*;
import runtime.intrinsic._count;
import runtime.intrinsic._index;
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;

import java.util.List;

/**
 * Try inlining calls to {@link runtime.intrinsic._for}.
 *
 * @author Basil Hosmer
 */
public class ForInliner implements Inliner
{
    public String tryInlining(
        final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        if (!stmtsOkay)
            return null;

        final List<Term> args = ((TupleTerm)apply.getArg()).getItems();

        final Term iterArg = args.get(0);
        final Term bodyArg = args.get(1);

        // To inline down to a for loop, we need to know a) that iterArg is
        // a list of contiguous integers, b) we need to be able to express
        // the list's initial and final values, and c) we need to know the
        // sign of their difference, to establish loop direction.
        // 1. index(list) is an int list from 0 to size(list), always ok
        // 2. count(n) is an int list from 0 to the absolute value of n, so
        // we know loop direction but need to inline the logic that ensures it.
        // Other possible candidates are range(i, n) and fromto(s, e), but
        // in these cases one or both arguments must be compile-time constant
        // to determine loop direction. May be worth doing them but at some
        // point but they're clearly less important than the ones we're doing now.

        final String startIndexExpr;
        final String endIndexExpr;

        final Term indexArg =
            InlinerUtils.derefToIntrinsicApply(iterArg, _index.INSTANCE, fmt);

        if (indexArg != null)
        {
            startIndexExpr = "0";
            endIndexExpr = fmt.formatTermAs(indexArg, ListValue.class) + ".size()";
        }
        else
        {
            final Term countArg =
                InlinerUtils.derefToIntrinsicApply(iterArg, _count.INSTANCE, fmt);

            if (countArg != null)
            {
                startIndexExpr = "0";
                if (countArg instanceof IntLiteral)
                {
                    endIndexExpr = "" + Math.abs(((IntLiteral)countArg).getValue());
                }
                else
                {
                    endIndexExpr = Math.class.getName() + ".abs(" +
                        fmt.formatTermAs(countArg, int.class) + ")";
                }
            }
            else
            {
                startIndexExpr = null;
                endIndexExpr = null;
            }
        }

        if (startIndexExpr == null)
        {
            final String indexes = fmt.formatTermAs(iterArg, ListValue.class);
            final String body = fmt.formatTermAs(bodyArg, Lambda.class);
            return "(" + indexes + ").run(" + body + ")";
        }

        final LambdaTerm bodyLambda = InlinerUtils.derefToLambda(bodyArg);

        if (bodyLambda == null)
        {
            final String body = fmt.formatTermAs(bodyArg, bodyArg.getType());

            return "{ " +
                "final int $n = " + endIndexExpr + "; " +
                "final " + Lambda.class.getName() + " $f = " + body + "; " +
                "for(int $i = " + startIndexExpr + "; $i < $n; $i++) " +
                "{ $f.apply(Integer.valueOf($i)); } }";
        }
        else
        {
            final String bodyParam = StatementFormatter.formatName(
                bodyLambda.getParams().keySet().iterator().next());

            final String body = InlinerUtils.formatBlockStmts(fmt, bodyArg, false);

            return "{ final int $n = " + endIndexExpr + "; " +
                "for(int $i = " + startIndexExpr + "; $i < $n; $i++) " +
                "{ final int " + bodyParam + " = $i; " + body + "; } }";
        }
    }
}
