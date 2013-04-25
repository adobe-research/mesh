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
 * f2i application reducer.
 *
 * @author Basil Hosmer
 */
public class F2IReducer implements ApplyReducer
{
    public final Term reduce(final Term arg)
    {
        if (arg instanceof DoubleLiteral)
            return new IntLiteral(arg.getLoc(),
                (int)Math.floor(((DoubleLiteral)arg).getValue()));

        return null;
    }
}
