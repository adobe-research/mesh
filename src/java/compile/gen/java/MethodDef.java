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

import javassist.CtBehavior;
import javassist.CtMethod;
import compile.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Parts for a Java method.
 *
 * @author Basil Hosmer
 */
public class MethodDef
{
    private final String sig;
    private final List<JavaStatement> statements;

    // storage for associated Javassist artifact. CtBehavior is super of CtMethod and CtConstructor.
    protected CtBehavior ctBehavior;

    public MethodDef(final String sig)
    {
        this.sig = sig;
        this.statements = new ArrayList<JavaStatement>();
    }

    public String getSig()
    {
        return sig;
    }

    public List<JavaStatement> getStatements()
    {
        return statements;
    }

    public void addStatement(final JavaStatement statement)
    {
        statements.add(statement);
    }

    public boolean isEmpty()
    {
        return statements.size() == 0;
    }

    public CtMethod getCtMethod()
    {
        return (CtMethod)ctBehavior;
    }

    public void setCtMethod(final CtMethod ctMethod)
    {
        this.ctBehavior = ctMethod;
    }

    public String getSource(final boolean comments)
    {
        return buildSource(true, comments);
    }

    public String getEmptySource()
    {
        return buildSource(false, false);
    }

    private String buildSource(final boolean body, final boolean comments)
    {
        final StringBuilder buf = new StringBuilder().
            append("\t").
            append(getSig());

        if (body)
        {
            buf.append("\n\t{");

            final List<String> statements = new ArrayList<String>();
            for (final JavaStatement stmt : getStatements())
                statements.add(stmt.getSource(comments));

            final boolean doublesp = !statements.isEmpty() &&
                statements.get(0).contains("\n");

            buf.append(StringUtils.joinAndDelimit(statements,
                doublesp ? "\n\n" : "\n", "\n", "\n\t"));
            buf.append("}");
        }
        else
        {
            buf.append(";");
        }

        return buf.toString();
    }
}
