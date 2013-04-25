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

import compile.term.visit.TermVisitor;
import compile.type.Type;

/**
 * Term representing the phantom value (RHS) of a {@link ParamBinding}.
 * Exists to rationalize the treatment of binding RHSes.
 *
 * @author Basil Hosmer
 */
public final class ParamValue extends AbstractTerm
{
    private final ParamBinding param;

    public ParamValue(final ParamBinding param)
    {
        super(param.getLoc());
        this.param = param;
    }

    public ParamBinding getParam()
    {
        return param;
    }

    // Term

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Typed

    public Type getType()
    {
        return param.getType();
    }

    public void setType(final Type type)
    {
        assert false : "ParamValue.setType()";
    }
}
