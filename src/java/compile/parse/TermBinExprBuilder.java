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
import compile.Session;
import compile.term.*;
import compile.Pair;
import runtime.intrinsic._eachleft;
import runtime.intrinsic._eachright;

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
        final Term base = buildOpTerm(loc, op);

        // TODO must be a better way
        if (base instanceof RefTerm)
        {
            final String name = ((RefTerm)base).getName();
            if (name != null)
            {
                if (name.equals(Ops.VAR_SYM))
                    return new VariantTerm(loc, lhs, rhs);

                if (name.equals(Ops.COND_SYM))
                    return new CondTerm(loc, lhs, rhs);
            }
        }

        final Term arg = new TupleTerm(loc, lhs, rhs);
        return new ApplyTerm(loc, base, arg);
    }

    /**
     *
     */
    private Term buildOpTerm(final Loc loc, final Object op)
    {
        if (op instanceof Term)
            return (Term)op;

        if (op instanceof String)
        {
            BinopInfo info = getOpInfo(op);
            if (info == null)
            {
                Session.error(loc, "operator ''{0}'' not yet supported", op);
                info = Ops.DEFAULT_BINOP_INFO;
            }

            return new RefTerm(loc, info.func);
        }

        assert op instanceof Verb;
        final Verb v = (Verb)op;

        final Term base = buildOpTerm(loc, v.op);
        return buildRightedOp(loc, v.rights, buildLeftedOp(loc, v.lefts, base));
    }

    /**
     *
     */
    private Term buildLeftedOp(final Loc loc, final int n, final Term base)
    {
        if (n == 0)
            return base;

        return buildLeftedOp(loc, n - 1,
            new ApplyTerm(loc, new RefTerm(loc, _eachleft.NAME), base));
    }

    /**
     *
     */
    private Term buildRightedOp(final Loc loc, final int n, final Term base)
    {
        if (n == 0)
            return base;

        return buildRightedOp(loc, n - 1,
            new ApplyTerm(loc, new RefTerm(loc, _eachright.NAME), base));
    }

    /**
     * Note that dimensional modifiers do not affect precedence or associativity.
     */
    @Override
    protected BinopInfo getOpInfo(final Object op)
    {
        return
            op instanceof Verb ? getOpInfo(((Verb)op).op) :
            op instanceof String ? Ops.BINOP_INFO.get(op) :
            Ops.DEFAULT_BINOP_INFO;
    }
}
