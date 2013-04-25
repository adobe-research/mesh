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

import compile.Pair;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Binary expression builder over parameterized operand type.
 * Subclasses supply desugaring and operator precedence info.
 * See {@link #buildBinExpr}.
 *
 * @author Basil Hosmer
 */
public abstract class BinExprBuilder<Operand>
{
    /**
     * Build binary expr term from <pre><code>(a, [(op1, b), (op2, c), ...])</code></pre>
     * based on precedence/associativity info from {@link #getOpInfo}.
     * Note: operators may be strings containing infix operator symbols,
     * or themselves be values of Operand type, the latter case representing
     * inline or already-desugared operator values.
     */
    public Operand buildBinExpr(final Operand head, final List<Pair<Object, Operand>> tail)
    {
        final ArrayDeque<Object> ops = new ArrayDeque<Object>();
        final ArrayDeque<Operand> vals = new ArrayDeque<Operand>();

        vals.push(head);

        final Pair<Object, Operand> first = tail.remove(0);
        ops.push(first.left);
        vals.push(first.right);

        while (!tail.isEmpty())
        {
            final int curprec;
            final Assoc curassoc;

            if (ops.isEmpty())
            {
                curprec = -1;
                curassoc = Assoc.Left;
            }
            else
            {
                final BinopInfo curinfo = getOpInfo(ops.peek());
                curprec = curinfo.prec;
                curassoc = curinfo.assoc;
            }

            final int nextprec = getOpInfo(tail.get(0).left).prec;

            if (curprec < nextprec || (curprec == nextprec && curassoc == Assoc.Right))
            {
                // shift
                final Pair<Object, Operand> next = tail.remove(0);
                ops.push(next.left);
                vals.push(next.right);
            }
            else
            {
                // reduce
                final Operand rhs = vals.pop();
                final Operand lhs = vals.pop();
                final Object op = ops.pop();
                vals.push(buildApply(op, lhs, rhs));
            }
        }

        Operand result = vals.pop();

        while (!ops.isEmpty())
        {
            final Object op = ops.pop();
            final Operand lhs = vals.pop();
            result = buildApply(op, lhs, result);
        }

        return result;
    }

    /**
     * helper--turn a binary expr into an application term in the
     * chosen domain. op can be a string containing a binary operator
     * symbol, or a full term to be used directly.
     */
    protected abstract Operand buildApply(final Object op, final Operand lhs, final Operand rhs);

    /**
     * helper--get opeator info struct for given argument.
     * op may be string or object. In the latter case, default
     * prec/assoc info should be returned
     */
    protected abstract BinopInfo getOpInfo(final Object op);
}
