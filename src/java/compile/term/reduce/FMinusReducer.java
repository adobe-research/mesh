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

import compile.term.DoubleLiteral;
import compile.term.IntLiteral;
import compile.term.Term;
import compile.term.TupleTerm;

import java.util.List;

/**
 * Double minus application reducer.
 *
 * @author Basil Hosmer
 */
public class FMinusReducer implements ApplyReducer
{
    public final Term reduce(final Term arg)
    {
        if (!(arg instanceof TupleTerm))
            return null;

        final List<Term> args = ((TupleTerm)arg).getItems();
        final Term l = args.get(0);
        final Term r = args.get(1);

        if (l instanceof DoubleLiteral)
        {
            if (r instanceof DoubleLiteral)
                return new DoubleLiteral(l.getLoc(),
                    ((DoubleLiteral)l).getValue() - ((DoubleLiteral)r).getValue());
        }
        else if (r instanceof DoubleLiteral && ((DoubleLiteral)r).getValue() == 0)
        {
            return l;
        }

        return null;
    }
}
