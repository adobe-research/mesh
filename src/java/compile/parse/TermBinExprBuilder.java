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
package compile.parse;

import compile.Loc;
import compile.term.ApplyTerm;
import compile.term.RefTerm;
import compile.term.Term;
import compile.term.TupleTerm;
import compile.Pair;

import java.util.List;

/**
 * Binary expr builder for value terms.
 *
 * @author Basil Hosmer
 */
public class TermBinExprBuilder extends BinExprBuilder<Term>
{
    private static TermBinExprBuilder INSTANCE = new TermBinExprBuilder();

    /**
     * static entry point
     */
    public static Term build(final Term head, final List<Pair<Object, Term>> tail)
    {
        return INSTANCE.buildBinExpr(head, tail);
    }

    @Override
    protected Term buildApply(final Object op, final Term lhs, final Term rhs)
    {
        final Loc loc = lhs.getLoc();

        final Term base = op instanceof Term ? (Term)op :
            new RefTerm(loc, getOpInfo(op).func);

        final Term arg = new TupleTerm(loc, lhs, rhs);

        return new ApplyTerm(loc, base, arg);
    }

    @Override
    protected BinopInfo getOpInfo(final Object op)
    {
        return op instanceof String ?
            Ops.BINOP_INFO.get(op) : Ops.DEFAULT_BINOP_INFO;
    }
}
