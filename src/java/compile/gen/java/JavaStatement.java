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
package compile.gen.java;

import compile.Loc;
import compile.term.Statement;
import compile.term.ValueStatement;

/**
 * Java source code statement, associated with a loc or term
 *
 * @author Basil Hosmer
 */
public final class JavaStatement
{
    protected final Loc loc;
    protected final Statement statement;
    protected final String text;

    public JavaStatement(final Statement statement, final String text)
    {
        this.loc = statement.getLoc();
        this.statement = statement;
        this.text = text;
    }

    public JavaStatement(final String text)
    {
        this.loc = null;
        this.statement = null;
        this.text = text;
    }

    public Loc getLoc()
    {
        return loc;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Statement getStatement()
    {
        return statement;
    }

    public String getText()
    {
        return text;
    }

    public String getSource(final boolean debug, final boolean comments)
    {
        final StringBuilder buf = new StringBuilder();

        if (loc != null)
        {
            if (comments)
            {
                buf.append("\t\t// ").
                    append(loc).
                    append(statement != null ? ": " + (statement.dump() + typeInfo(statement)) : "").
                    append("\n");
            }

            if (debug)
            {
                //buf.append("\t\t").
                //    append(DebugWatcher.class.getName()).append(".setLocation(\"").
                //    append(loc).
                //    append("\");\n");
            }
        }

        buf.append("\t\t").
            append(text).
            append(";");

        return buf.toString();
    }

    private static String typeInfo(final Statement statement)
    {
        return statement instanceof ValueStatement ? " : " + ((ValueStatement)statement).getType().dump() : "";
    }
}
