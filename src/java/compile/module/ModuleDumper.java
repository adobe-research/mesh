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
package compile.module;

import compile.StringUtils;
import compile.term.LetBinding;
import compile.term.Statement;
import compile.term.TypeDef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Basil Hosmer
 */
public final class ModuleDumper
{
    /**
     * dump module contents.
     */
    public static String dump(final Module module)
    {
        return "Module{name=\"" + module.getName() +
            "\",\n\tvalueBindings=[\n\t\t" +
            StringUtils.join(dumpVarBindings(module.getLets().values()), ",\n\t\t") +
            "\n\t],\n\ttypeBindings=[\n\t\t" +
            StringUtils.join(dumpTypeBindings(module.getTypeDefs().values()), "\n\t\t") +
            "\n\t],\n\tbody=[\n\t\t" +
            StringUtils.join(dumpStatements(module.getBody()), "\n\t\t") +
            "\n\t]\n}";
    }

    /**
     * dump value bindings
     */
    public static List<String> dumpVarBindings(final Collection<LetBinding> lets)
    {
        final List<String> dumps = new ArrayList<String>();
        for (final LetBinding valueBinding : lets)
            dumps.add(valueBinding.dump());
        return dumps;
    }

    /**
     * dump type bindings
     */
    public static List<String> dumpTypeBindings(final Collection<TypeDef> typeDefs)
    {
        final List<String> dumps = new ArrayList<String>();
        for (final TypeDef typeDef : typeDefs)
            dumps.add(typeDef.dump());
        return dumps;
    }

    /**
     * dump statement list
     */
    public static List<String> dumpStatements(final Collection<Statement> statements)
    {
        final List<String> dumps = new ArrayList<String>();
        for (final Statement statement : statements)
        {
            final StringBuilder buf = new StringBuilder();
            buf.append(statement.getLoc()).append("\t").append(statement.dump());
            dumps.add(buf.toString());
        }
        return dumps;
    }
}