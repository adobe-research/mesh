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
package compile.gen.java.inline;

import compile.gen.java.StatementFormatter;
import compile.term.ApplyTerm;
import compile.term.Term;
import compile.term.TupleTerm;
import compile.type.Type;
import runtime.intrinsic._lplus;
import runtime.intrinsic._mplus;
import runtime.rep.list.ListValue;
import runtime.rep.map.MapValue;

import java.util.List;

/**
 * Try inlining calls to {@link runtime.intrinsic.Plus}.
 *
 * @author Basil Hosmer
 */
public class PlusInliner implements Inliner
{
    public String tryInlining(
        final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        final List<Term> args = ((TupleTerm)apply.getArg()).getItems();

        final Term arg0 = args.get(0);
        final Term arg1 = args.get(1);

        final Type type = arg0.getType();
        final Class<?> c = fmt.mapType(type);

        final String l = fmt.formatTermAs(arg0, c);
        final String r = fmt.formatTermAs(arg1, c);

        final String expr;
        if (c == int.class || c == long.class ||
            c == float.class || c == double.class ||
            c == String.class)
        {
            expr = "((" + l + ") + (" + r + "))";
        }
        else if (c == ListValue.class)
        {
            expr = _lplus.class.getName() + ".invoke(" + l + ", " + r + ")";
        }
        else if (c == MapValue.class)
        {
            expr = _mplus.class.getName() + ".invoke(" + l + ", " + r + ")";
        }
        else
        {
            // bail
            return null;
        }

        if (stmtsOkay)
            return expr;

        return fmt.fixup(apply.getLoc(), expr, c);
    }
}
