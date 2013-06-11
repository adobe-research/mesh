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
package compile;

import com.google.common.collect.Lists;
import compile.analyze.ModuleAnalyzer;
import compile.gen.UnitBuilder;
import compile.gen.Unit;
import compile.gen.UnitDictionary;
import compile.module.Module;
import compile.parse.RatsScriptParser;
import compile.parse.RatsShellScriptParser;
import compile.term.ImportStatement;
import compile.term.Statement;

import java.io.Reader;
import java.util.*;

/**
 * Compiler top-level entry points.
 *
 * @author Basil Hosmer
 */
public class Compiler
{
    private static final ThreadLocal<Compiler> LOCAL = new ThreadLocal<Compiler>()
    {
        protected Compiler initialValue()
        {
            return new Compiler();
        }
    };

    private static Compiler getThreadLocal()
    {
        return LOCAL.get();
    }

    /**
     * Compile a unit from script input obtained via given reader.
     * Unit or null is returned. Also, if successful, unit is cached
     * in global unit dictionary held in {@link #unitDictionary}
     */
    public static Unit compileScript(
        final Loc loc, final Reader reader, final String name)
    {
        final List<Statement> stmts = RatsScriptParser.parseScript(reader, loc);
        if (stmts == null)
            return null;

        final Compiler compiler = getThreadLocal();

        final Module module = compiler.buildModule(loc, name, stmts);
        if (module == null)
            return null;

        final Unit unit = UnitBuilder.build(module, compiler.unitDictionary);
        if (unit == null)
            return null;

        compiler.unitDictionary.add(unit);
        return unit;
    }

    /**
     * Compile a unit from shell input. Shell input differs from standard
     * script code as described in {@link #analyzeShellInput}.
     * Unit or null is returned. Like units compiled from scripts, a
     * successfully compiled unit is cached in {@link #unitDictionary}
     */
    public static Unit compileShellInput(
        final Loc loc, final Reader reader, final String name,
        final List<ImportStatement> imports)
    {
        final Module module = analyzeShellInput(loc, reader, name, imports);
        if (module == null)
            return null;

        final Compiler compiler = getThreadLocal();

        final Unit unit = UnitBuilder.build(module, compiler.unitDictionary);
        if (unit == null)
            return null;

        // add unit to session-wide dictionary and return
        compiler.unitDictionary.add(unit);
        return unit;
    }

    /**
     * Build a module from shell input text. Shell input differs from
     * standard script input in two ways: a) unbound-value statements
     * are implicitly wrapped in a print() call. b) an extra list of
     * imports is prepended, supplying the shell input with definitions
     * introduced interactively. Built module or null is returned.
     */
    public static Module analyzeShellInput(
        final Loc loc, final Reader reader, final String name,
        final List<ImportStatement> imports)
    {
        final List<Statement> stmts = RatsShellScriptParser.parseScript(reader, loc);
        if (stmts == null)
            return null;

        return getThreadLocal().buildModule(loc, name, prependImports(imports, stmts));
    }

    /**
     *
     */
    private static List<Statement> prependImports(
        final List<ImportStatement> imports, final List<Statement> stmts)
    {
        final List<Statement> body = new ArrayList<Statement>(imports);
        body.addAll(stmts);
        return body;
    }

    /**
     *
     */
    public static UnitDictionary getUnitDictionary()
    {
        return getThreadLocal().unitDictionary;
    }

    /**
     *
     */
    public static void clearUnitDictionary()
    {
        getThreadLocal().unitDictionary.clear();
    }

    /**
     *
     */
    public static boolean writeUnits(final String path)
    {
        for (final Unit unit : getThreadLocal().unitDictionary.getUnits())
            if (!unit.write(path))
                return false;

        return true;
    }

    //
    // instance
    //

    /**
     * module-compilation state:
     * currently, imported modules are compiled eagerly, so our
     * compilation state includes a stack of modules-in-compilation.
     * TODO remove when we go to two-phase pipeline
     */
    private ArrayDeque<String> moduleStack;

    /**
     * We may or may not be compiling a module specified by an
     * implicit import, *or a module explicitly imported by such*.
     * When we are, implicit imports are themselves suppressed.
     */
    private boolean inImplicitImport;

    /**
     * Dictionary of compiled units.
     */
    private UnitDictionary unitDictionary;

    /**
     *
     */
    private Compiler()
    {

        this.moduleStack = new ArrayDeque<String>();
        this.inImplicitImport = false;
        this.unitDictionary = new UnitDictionary();
    }

    /**
     * Build an analyzed module from a name and statement list.
     * If successful, returned module is ready for code generation.
     * Otherwise, null is returned.
     * NOTE: currently, module is compiled in a single pass, with
     * imports causing a recursive call via {@link compile.analyze.ImportResolver}.
     * TODO move to a split pipeline to support import cycles.
     */
    private Module buildModule(final Loc loc,
        final String name, final List<Statement> stmts)
    {
        if (!moduleCircularityCheck(loc, name))
            return null;

        final boolean isImplicitImport = Config.isImplicitImport(name);
        final boolean isImplicitImportChild = inImplicitImport;
        final boolean isImplicitImportRoot = isImplicitImport && !isImplicitImportChild;

        if (isImplicitImportRoot)
            inImplicitImport = true;

        final List<Statement> body = inImplicitImport ? stmts :
            prependImports(Config.ImplicitImports, stmts);

        // create new module with passed loc, name, and stmts
        final Module module = new Module(loc, name, body);

        // analysis builds module internals, returns success
        moduleStack.push(name);
        final boolean analyzed = ModuleAnalyzer.analyze(module);
        moduleStack.pop();

        // clear implicit import flag if we're a root
        if (isImplicitImportRoot)
            inImplicitImport = false;

        return analyzed ? module : null;
    }

    /**
     * Check module name against session's in-module compilation stack.
     * Presence of name on the stack indicates a circular import, which
     * is currently not supported.
     * TODO won't need this when we go to split pipeline
     */
    private boolean moduleCircularityCheck(final Loc loc, final String name)
    {
        if (isInModule(name))
        {
            Session.error(loc,
                "cyclic module dependencies not yet supported: module = {0}, cycle =",
                name);

            for (final String importer : moduleStack)
                Session.error("\t{0}", importer);

            return false;
        }

        return true;
    }

    /**
     *
     */
    private boolean isInModule(final String name)
    {
        for (final String module : moduleStack)
            if (module.equals(name))
                return true;

        return false;
    }
}
