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

import compile.StringUtils;
import compile.term.LambdaTerm;
import compile.term.Term;

import java.util.*;

/**
 * {@link #dump} prints a summary of a {@link Unit compilation unit}.
 * $u in shell.
 *
 * @author Basil Hosmer
 */
public final class UnitDumper
{
    public static String dump(final Unit unit, final boolean debug)
    {
        return
            "*** Unit[" + unit.getModule().getName() + "] ***\n\n" +
            unit.getModule().dump() + "\n\n// moduleClassDef\n\n" +
            unit.getModuleClassDef().getSource(debug) +
            (unit.getLambdaClassDefs().isEmpty() ? "" : "\n\n// lambdaClassDefs\n\n") +
            StringUtils.join(dumpClassDefs(unit.getLambdaClassDefs(), debug), "\n\n");
    }

    private static Collection<String> dumpClassDefs(
        final Map<LambdaTerm, ClassDef> classDefs, final boolean debug)
    {
        final List<String> dumps = new ArrayList<String>();
        final TreeMap<String, LambdaTerm> namesToLambdas = new TreeMap<String, LambdaTerm>();

        for (final Map.Entry<LambdaTerm, ClassDef> entry : classDefs.entrySet())
            namesToLambdas.put(entry.getValue().getName(), entry.getKey());

        for (final Map.Entry<String, LambdaTerm> entry : namesToLambdas.entrySet())
        {
            final LambdaTerm term = entry.getValue();
            final String header = commentHeader(term);
            final String source = classDefs.get(entry.getValue()).getSource(debug);
            dumps.add(header + source);
        }

        return dumps;
    }

    /**
     * comment header for dumps
     */
    private static String commentHeader(final Term term)
    {
        return "// " + term.getLoc() + ": " + term.dump() + " : " +
            term.getType().dump() + "\n";
    }
}
