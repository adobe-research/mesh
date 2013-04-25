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
 * i2f application reducer.
 *
 * @author Basil Hosmer
 */
public class I2FReducer implements ApplyReducer
{
    public final Term reduce(final Term arg)
    {
        if (arg instanceof IntLiteral)
            return new DoubleLiteral(arg.getLoc(), ((IntLiteral)arg).getValue());

        return null;
    }
}
