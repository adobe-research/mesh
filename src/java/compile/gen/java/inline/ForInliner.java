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
import runtime.rep.lambda.Lambda;
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

        final Term indexArg = args.get(0);
        final Term countArg = InlinerUtils.derefToIntrinsicApply(indexArg, _count.INSTANCE, fmt);

        final Term bodyArg = args.get(1);

        if (countArg == null)
        {
            final String indexes = fmt.formatTermAs(indexArg, ListValue.class);
            final String body = fmt.formatTermAs(bodyArg, Lambda.class);
            return "(" + indexes + ").run(" + body + ")";
        }

        final String countExpr = fmt.formatTermAs(countArg, int.class);
        final LambdaTerm bodyLambda = InlinerUtils.derefToLambda(bodyArg);

        if (bodyLambda == null)
        {
            final String body = fmt.formatTermAs(bodyArg, bodyArg.getType());

            return "{ " +
                "final int $n = " + countExpr + "; " +
                "final " + Lambda.class.getName() + " $f = " + body + "; " +
                "for(int $i = 0; $i < $n; $i++) " +
                "{ $f.apply(Integer.valueOf($i)); } }";
        }
        else
        {
            final String bodyParam = StatementFormatter.formatName(
                bodyLambda.getParams().keySet().iterator().next());

            final String body = InlinerUtils.formatBlockStmts(fmt, bodyArg, false);

            return "{ final int $n = " + countExpr + "; " +
                "for(int $i = 0; $i < $n; $i++) " +
                "{ final int " + bodyParam + " = $i; " + body + "; } }";
        }
    }
}
