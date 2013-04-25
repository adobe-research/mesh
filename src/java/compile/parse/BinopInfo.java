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

/**
 * Holds binary operator info: precedence, associativity and a function name.
 *
 * @author Basil Hosmer
 */
public class BinopInfo
{
    final int prec;
    final Assoc assoc;
    final String func;

    BinopInfo(final int prec, final Assoc assoc, final String func)
    {
        this.prec = prec;
        this.assoc = assoc;
        this.func = func;
    }
}
