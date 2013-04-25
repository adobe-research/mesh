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

import compile.Loc;
import compile.term.*;
import runtime.intrinsic._print;

import java.io.Reader;
import java.util.List;

/**
 * Parser for shell input. Extends {@link RatsScriptParser} to wrap
 * final top-level statement, if an unbound value, in a call to print().
 *
 * @author Basil Hosmer
 */
public final class RatsShellScriptParser extends RatsScriptParser
{
    private static ThreadLocal<RatsShellScriptParser> LOCAL = new ThreadLocal<RatsShellScriptParser>()
    {
        @Override
        protected RatsShellScriptParser initialValue()
        {
            return new RatsShellScriptParser();
        }
    };

    public static List<Statement> parseScript(final Reader reader, final Loc loc)
    {
        return LOCAL.get().parse(reader, loc);
    }

    /**
     * Wrap top-level non-assignment statements in calls to print() before
     * returning parsed statement list.
     */
    @Override
    public List<Statement> parse(final Reader reader, final Loc loc)
    {
        final List<Statement> script = super.parse(reader, loc);

        if (script != null && !script.isEmpty())
        {
            final int lastPos = script.size() - 1;
            final Statement last = script.get(lastPos);

            if (last instanceof UnboundTerm)
            {
                final Term term = ((UnboundTerm)last).getValue();
                final Loc termLoc = term.getLoc();
                final RefTerm print = new RefTerm(termLoc, _print.INSTANCE.getName());
                script.set(lastPos, new UnboundTerm(new ApplyTerm(termLoc, print, term)));
            }
        }

        return script;
    }
}
