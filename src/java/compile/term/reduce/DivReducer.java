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

import compile.Session;
import compile.term.IntLiteral;
import compile.term.Term;
import compile.term.TupleTerm;

import java.util.List;

/**
 * Int divide application reducer.
 *
 * @author Basil Hosmer
 */
public class DivReducer implements ApplyReducer
{
    public final Term reduce(final Term arg)
    {
        if (!(arg instanceof TupleTerm))
            return null;

        final List<Term> args = ((TupleTerm)arg).getItems();
        final Term num = args.get(0);
        final Term denom = args.get(1);

        if (denom instanceof IntLiteral)
        {
            final int d = ((IntLiteral)denom).getValue();

            if (d == 0)
            {
                Session.error(denom.getLoc(), "division by zero");
                return null;
            }
            else if (d == 1)
            {
                return num;
            }
            else if (num instanceof IntLiteral)
            {
                return new IntLiteral(num.getLoc(),
                    ((IntLiteral)num).getValue() / d);
            }
        }
        else if (num instanceof IntLiteral && ((IntLiteral)num).getValue() == 0)
        {
            return new IntLiteral(num.getLoc(), 0);
        }

        return null;
    }
}
