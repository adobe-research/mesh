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
 * Common impl for tersm whose type is calculated and stored with the term,
 * rather than being an invariant of the term itself as in simple literals.
 *
 * @author Basil Hosmer
 */
public abstract class AbstractTypedTerm extends AbstractTerm
{
    protected Type type;

    protected AbstractTypedTerm(final Loc loc)
    {
        super(loc);
    }

    protected AbstractTypedTerm(final Loc loc, final Type type)
    {
        super(loc);
        this.type = type;
    }

    // Typed

    public void setType(final Type type)
    {
        this.type = type;
    }

    public Type getType()
    {
        return type;
    }
}
