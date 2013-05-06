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

import compile.term.Term;

/**
 * Carrier for infix operator expression, plus dimensional info
 */
public class Verb
{
    public final int lefts;
    public final Object op;
    public final int rights;

    public Verb(final int lefts, final Object op, final int rights)
    {
        this.op = op;
        this.lefts = lefts;
        this.rights = rights;

        assert op instanceof Verb || op instanceof String || op instanceof Term;
    }

    public Verb(final Object op)
    {
        this(0, op, 0);
    }
}
