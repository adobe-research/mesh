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
import compile.term.visit.TermDumper;
import compile.type.*;

/**
 * Common base implementation for all value terms.
 *
 * @author Basil Hosmer
 */
public abstract class AbstractTerm implements Term
{
    protected final Loc loc;

    protected AbstractTerm(final Loc loc)
    {
        this.loc = loc;
    }

    // Term

    /**
     * Overriden by subclasses, e.g. {@link IntLiteral#isConstant},
     * {@link PositionalTerm#isConstant}.
     */
    public boolean isConstant()
    {
        return false;
    }

    // Typed

    public boolean hasDeclaredType()
    {
        return false;
    }

    public Type getDeclaredType()
    {
        return null;
    }
    
    public void setDeclaredType(final Type type)
    {
        throw new UnsupportedOperationException("AbstractTerm.setDeclaredType()");
    }

    // Statement

    // Dumpable

    public final String dump()
    {
        return TermDumper.dump(this);
    }

    // Located

    public final Loc getLoc()
    {
        return loc;
    }

}
