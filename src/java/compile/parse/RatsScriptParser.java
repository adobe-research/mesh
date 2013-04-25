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
import compile.Session;
import compile.term.Statement;
import rats.MeshParser;
import xtc.parser.ParseError;
import xtc.parser.Result;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Service wrapper for Rats-generated parser.
 *
 * @author Basil Hosmer
 */
public class RatsScriptParser
{
    private static ThreadLocal<RatsScriptParser> LOCAL =
        new ThreadLocal<RatsScriptParser>()
        {
            @Override
            protected RatsScriptParser initialValue()
            {
                return new RatsScriptParser();
            }
        };

    public static List<Statement> parseScript(final Reader reader, final Loc loc)
    {
        return LOCAL.get().parse(reader, loc);
    }

    //
    // instance
    //

    /**
     * Parse text from Reader into a list of Statements.
     */
    public List<Statement> parse(final Reader reader, final Loc loc)
    {
        final MeshParser parser = new MeshParser(reader, loc.getPath());

        try
        {
            Session.pushErrorCount();

            final Result r = parser.pScript(0);

            if (!r.hasValue())
            {
                final ParseError err = r.parseError();
                Session.error(err.index < 0 ? loc : parser.loc(err.index), err.msg);
            }

            if (Session.popErrorCount() == 0)
                return r.semanticValue();
            else
                return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
