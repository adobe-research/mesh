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
import javassist.CtField;

/**
 * Same as Statement, except getSource() emits declaration instead of statement
 *
 * @author Basil Hosmer
 */
public final class FieldDef
{
    private final Loc loc;
    private final String declaration;
    private final String initializer;

    private CtField ctField;

    public FieldDef(final Loc loc, final String declaration, final String initializer)
    {
        this.loc = loc;
        this.declaration = declaration;
        this.initializer = initializer;
    }

    public FieldDef(final String declaration, final String initializer)
    {
        this(null, declaration, initializer);
    }

    public String getDeclaration()
    {
        return declaration;
    }

    public String getInitializer()
    {
        return initializer;
    }

    public CtField getCtField()
    {
        return ctField;
    }

    public void setCtField(final CtField ctField)
    {
        this.ctField = ctField;
    }

    public boolean isInitialized()
    {
        return initializer != null && initializer.length() > 0;
    }

    public String getSource(final boolean comments)
    {
        return buildSource(true, comments);
    }

    public String getUninitializedSource()
    {
        return buildSource(false, false);
    }

    private String buildSource(final boolean init, final boolean comments)
    {
        final StringBuilder buf = new StringBuilder();

        if (loc != null)
        {
            if (comments)
            {
                buf.append("\t// ").
                    append(loc).
                    append("\n");
            }
        }

        buf.append("\t").
            append(getDeclaration());

        if (init && isInitialized())
        {
            buf.append(" = ").
                append(getInitializer());
        }

        buf.append(";");

        return buf.toString();
    }
}