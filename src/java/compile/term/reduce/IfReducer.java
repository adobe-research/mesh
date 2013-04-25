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
package compile.term.reduce;

import compile.term.*;

import java.util.List;

/**
 * And application reducer.
 *
 * @author Basil Hosmer
 */
public class IfReducer implements ApplyReducer
{
    public final Term reduce(final Term arg)
    {
        if (!(arg instanceof TupleTerm))
            return null;

        final List<Term> args = ((TupleTerm)arg).getItems();
        final Term cond = args.get(0);

        if (cond instanceof BoolLiteral)
        {
            if (((BoolLiteral)cond).getValue())
            {
                final Term left = args.get(1);

                if (left instanceof LambdaTerm)
                {
                    final LambdaTerm leftLam = (LambdaTerm)left;
                    if (leftLam.getNonResultStatements().isEmpty())
                        return leftLam.getResultStatement().getValue();
                }

                return new ApplyTerm(left.getLoc(), left, TupleTerm.UNIT);
            }
            else
            {
                final Term right = args.get(2);

                if (right instanceof LambdaTerm)
                {
                    final LambdaTerm rightLam = (LambdaTerm)right;
                    if (rightLam.getNonResultStatements().isEmpty())
                        return rightLam.getResultStatement().getValue();
                }

                return new ApplyTerm(right.getLoc(), right, TupleTerm.UNIT);
            }
        }

        return null;
    }
}
