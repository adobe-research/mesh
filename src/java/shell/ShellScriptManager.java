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
package shell;

import com.google.common.collect.Lists;
import compile.*;
import compile.Compiler;
import compile.gen.Unit;
import compile.gen.UnitManager;
import compile.module.Module;
import compile.parse.RatsScriptParser;
import compile.term.ImportStatement;
import compile.term.Statement;
import compile.term.TypeDef;
import compile.term.ValueStatement;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * Holds shell runtime context. Manages chained module import scheme which provides
 * shell's accumulating binding behavior.
 *
 * @author Basil Hosmer
 */
public final class ShellScriptManager
{
    private final UnitManager unitManager;
    private final Map<String, Integer> loadCounts;
    private final List<Unit> unitHistory;
    private final List<ImportStatement> historyImports;

    /**
     *
     */
    public ShellScriptManager()
    {
        this.unitManager = Config.newUnitManager();
        this.loadCounts = new HashMap<String, Integer>();
        this.unitHistory = new ArrayList<Unit>();
        this.historyImports = new ArrayList<ImportStatement>();
    }

    /**
     * Unit history - all units processed, in reverse order
     */
    public List<Unit> getUnitHistory()
    {
        return unitHistory;
    }

    /**
     * Retrieve a past unit.
     *
     * @param n number of steps into the past, 0 most recent
     * @return unit, or null if argument is out of range
     */
    public Unit getHistoryUnit(final int n)
    {
        return unitHistory.size() > n ? unitHistory.get(n) : null;
    }

    /**
     * Compile, save and run passed script text.
     */
    public boolean loadScript(final Loc loc, final Reader reader)
    {
        // derive module name from passed location, presumably a file path
        final String name = NameUtils.getFilenameStemFromPath(loc.getPath());

        if (!NameUtils.isValidName(name))
        {
            Session.error(loc,
                "script path {0} does not yield a valid module name ({1})",
                loc.getPath(), name);

            return false;
        }

        final String loadName = getLoadName(name);

        // compile script unit from input
        final Unit unit = Compiler.compileScript(loc, reader, loadName);
        if (unit == null)
            return false;

        // add unit to history
        addHistoryUnit(unit);

        if (Session.isDebug())
            Session.debug(loc, "Loading script {0}...", loadName);

        return unitManager.loadUnit(unit);
    }

    /**
     * attach disambiguating suffix to given name
     */
    private String getLoadName(final String name)
    {
        return name + "_" + postIncLoadCount(name);
    }

    /**
     * retrieve the load count for a given name, increment it,
     * then return the original.
     */
    private int postIncLoadCount(final String name)
    {
        if (!loadCounts.containsKey(name))
        {
            loadCounts.put(name, 1);
            return 0;
        }
        else
        {
            final int count = loadCounts.get(name);
            loadCounts.put(name, count + 1);
            return count;
        }
    }

    /**
     * Save successfully built unit in {@link #unitHistory}
     */
    private void addHistoryUnit(final Unit unit)
    {
        // add unit to history (most recent first)
        unitHistory.add(0, unit);

        // this becomes an import for subsequent shell input
        final ImportStatement historyImport =
            ImportStatement.openUnqualified(Loc.INTRINSIC, unit.getModule().getName());

        // add module to import list (most recent last)
        historyImports.add(historyImport);
    }

    /**
     * Evaluate shell input
     */
    public boolean evalShellInput(
        final Loc loc, final Reader reader, final List<ImportStatement> shellImports)
    {
        // import shell imports, followed by history
        final List<ImportStatement> imports = Lists.newArrayList(shellImports);
        imports.addAll(historyImports);

        // disambiguate module name with counter
        final String loadName = getLoadName("shell");

        // compile script unit from input
        final Unit unit = Compiler.compileShellInput(loc, reader, loadName, imports);

        if (unit == null)
            return false;

        addHistoryUnit(unit);

        if (Session.isDebug())
            Session.debug(loc, "Loading script...");

        return unitManager.loadUnit(unit);
    }

    /**
     * Compile passed script text, and print the types of top-level type bindings and
     * then statements. (I.e., the two are separated, no longer interleaved in decl order.)
     * Return a map from statement dumps to type dumps.
     */
    public Map<String, String> dumpTypes(
        final Loc loc, final Reader reader, final List<ImportStatement> shellImports)
    {
        final Map<String, String> dumps = new LinkedHashMap<String, String>();

        // imports list is shell history, followed $import list
        final List<ImportStatement> imports = Lists.newArrayList(historyImports);
        imports.addAll(shellImports);

        final Module module = compile.Compiler.analyzeShellInput(
            loc, reader, "typecheck", imports);

        if (module != null)
        {
            for (final TypeDef typeDef : module.getTypeDefs().values())
            {
                dumps.put(typeDef.getName(), typeDef.getValue().dump());
            }

            for (final Statement statement : module.getBody())
            {
                if (statement instanceof ValueStatement)
                {
                    dumps.put(statement.dump(),
                        ((ValueStatement)statement).getType().dump());
                }
            }
        }

        return dumps;
    }

    /**
     * clear shell state
     * Note that we don't reset {@link #loadCounts}, since even when we
     * clear our state, we leave old compiled module classes around that
     * we can't step on.
     */
    public void clearShellState()
    {
        // release script instance data - since module instances are singleton
        // static members of their representing classes, they won't be reclaimed
        // by simply detaching from units->modules
        for (final Unit unit : Compiler.getUnitDictionary().getUnits())
            unitManager.unloadUnit(unit);

        Compiler.clearUnitDictionary();

        this.unitHistory.clear();
        this.historyImports.clear();

        unitManager.resetInternals();
    }

    /**
     * Attempt to build an import statement from passed spec
     */
    public static ImportStatement parseImportSpec(
        final Loc loc, final String spec)
    {
        final StringReader reader = new StringReader("import " + spec);

        final List<Statement> statements =
            RatsScriptParser.parseScript(reader, loc);

        if (statements != null && statements.size() == 1 &&
            statements.get(0) instanceof ImportStatement)
        {
            return (ImportStatement)statements.get(0);
        }

        return null;
    }
}
