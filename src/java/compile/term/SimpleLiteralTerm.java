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
package compile.term;

import compile.Loc;
import compile.type.Type;

/**
 * Common superclass for syntactic literal terms whose types
 * are invariant, e.g. {@link StringLiteral}, {@link IntLiteral}.
 *
 * @author Basil Hosmer
 */
public abstract class SimpleLiteralTerm extends AbstractTerm
{
    public SimpleLiteralTerm(final Loc loc)
    {
        super(loc);
    }

    // Term

    public final boolean isConstant()
    {
        return true;
    }

    // Typed

    public void setType(final Type type)
    {
        assert false : "ConstantTerm.setType()";
    }
}
