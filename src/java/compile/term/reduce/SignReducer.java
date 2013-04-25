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

/**
 * sign application reducer.
 *
 * @author Basil Hosmer
 */
public class SignReducer implements ApplyReducer
{
    public final Term reduce(final Term arg)
    {
        if (arg instanceof IntLiteral)
        {
            final int i = ((IntLiteral)arg).getValue();
            return new IntLiteral(arg.getLoc(), i > 0 ? 1 : i < 0 ? -1 : 0);
        }

        return null;
    }
}
