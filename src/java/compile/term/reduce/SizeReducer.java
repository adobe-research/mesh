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

import compile.term.IntLiteral;
import compile.term.ListTerm;
import compile.term.RefTerm;
import compile.term.Term;

/**
 * size application reducer.
 *
 * @author Basil Hosmer
 */
public class SizeReducer implements ApplyReducer
{
    public final Term reduce(final Term arg)
    {
        final Term deref = arg instanceof RefTerm ?
            ((RefTerm)arg).deref() : arg;

        if (deref instanceof ListTerm)
            return new IntLiteral(arg.getLoc(), ((ListTerm)deref).getItems().size());

        return null;
    }
}
