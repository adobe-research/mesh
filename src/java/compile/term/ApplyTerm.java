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
import compile.parse.ApplyFlavor;
import compile.term.visit.TermVisitor;

/**
 * Application term: argument applied to base
 *
 * @author Basil Hosmer
 */
public final class ApplyTerm extends AbstractTypedTerm
{
    private final Term base;
    private Term arg;
    private ApplyFlavor flav;

    public ApplyTerm(final Loc loc, final Term base,
        final Term arg, final ApplyFlavor flav)
    {
        super(loc);
        this.base = base;
        this.arg = arg;
        this.flav = flav;
    }

    public ApplyTerm(final Loc loc, final Term base, final Term arg)
    {
        this(loc, base, arg, ApplyFlavor.FuncApp);
    }

    public Term getBase()
    {
        return base;
    }

    public Term getArg()
    {
        return arg;
    }

    public ApplyFlavor getFlav()
    {
        return flav;
    }

    // Term

    public <T> T accept(final TermVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ApplyTerm applyTerm = (ApplyTerm)o;

        if (!arg.equals(applyTerm.arg)) return false;
        if (!base.equals(applyTerm.base)) return false;
        if (!flav.equals(applyTerm.flav)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = base.hashCode();
        result = 31 * result + arg.hashCode();
        result = 31 * result + flav.hashCode();
        return result;
    }
}
