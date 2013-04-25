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
import compile.term.RefTerm;
import compile.term.Term;
import compile.term.TupleTerm;
import compile.type.Type;
import runtime.rep.Symbol;

import java.util.List;

/**
 * Try inlining calls to {@link runtime.intrinsic.NE}.
 *
 * @author Basil Hosmer
 */
public class NEInliner implements Inliner
{
    public String tryInlining(
        final ApplyTerm apply, final StatementFormatter fmt,
        final boolean stmtsOkay)
    {
        final List<Term> args = ((TupleTerm)apply.getArg()).getItems();

        final Term arg0 = args.get(0);
        final Term arg1 = args.get(1);

        final Type type = arg0.getType();
        final Class<?> argClass = fmt.mapType(type);

        final String l = fmt.formatTermAs(arg0, argClass);
        final String r = fmt.formatTermAs(arg1, argClass);

        final String expr;
        if (argClass.isPrimitive() || argClass == Symbol.class)
        {
            expr = "((" + l + ") != (" + r + "))";
        }
        else if (arg0 instanceof RefTerm && arg1 instanceof RefTerm)
        {
            // arg0.hashCode() != arg1.hashCode() || !arg0.equals(arg1);
            final String hashCheck = "(" + l + ").hashCode() != (" + r + ").hashCode()";
            expr = "((" + hashCheck + ") || !(" + l + ").equals(" + r + "))";
        }
        else
        {
            // avoid potential redundant computation
            return null;
        }

        if (stmtsOkay)
            return expr;

        return fmt.fixup(apply.getLoc(), expr, boolean.class);
    }
}
