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

import compile.analyze.ModuleAnalyzer;
import compile.gen.java.*;
import compile.module.*;
import compile.parse.RatsScriptParser;
import compile.parse.RatsShellScriptParser;
import compile.term.ImportStatement;
import compile.term.Statement;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Compiler top-level entry points.
 * TODO rationalize UnitDictionary/ModuleDictionary etc.
 *
 * @author Basil Hosmer
 */
public class ScriptCompiler
{
    public static final String INTRINSICS = "intrinsics";

    // Uses only Java backend and runtime for now
    private static final IntrinsicsResolver intrinsicsResolver =
        new JavaIntrinsicsResolver();

    private static final List<String> compilerImports = new ArrayList<String>();
    static {
        compilerImports.add("lang");
    }

    private static IntrinsicsResolver getIntrinsicsResolver()
    {
        return intrinsicsResolver;
    }

    /**
     * Build a {@link compile.gen.java.Unit} from a script
     * (a list of top-level statements) obtained from a reader,
     * and a set of implicitly imported modules.
     * <p/>
     * Final param determines whether last statement should
     * be printed if it's an unbound value, shell-style.
     * <p/>
     * Note that we import from a dictionary of fully compiled
     * {@link compile.gen.java.Unit}s, not simply {@link Module}s.
     *
     * @param loc        base loc for program text and module
     * @param reader          program text source
     * @param moduleName      module name
     * @param imports list of implicit imports.
     * @param dict            must contain units for any modules imported by program text,
     *                        as well as implicit imports
     * @param debug           debug mode for compiled code
     * @param print           if true, print value of final statement if unbound value
     * @return
     */
    public static Unit compileScript(final Loc loc,
                                     final Reader reader,
                                     final String moduleName,
                                     final List<ImportStatement> imports,
                                     final UnitDictionary dict,
                                     final boolean debug,
                                     final boolean print)
    {
        final List<Statement> statements = print ?
            RatsShellScriptParser.parseScript(reader, loc) :
            RatsScriptParser.parseScript(reader, loc);

        return statements == null ? null :
            compileModule(loc, moduleName, imports, statements, dict, debug);
    }

    /**
     * Like {@link #compileScript}, except that unit is not actually built.
     * We return the resulting {@link Module}.
     */
    public static Module analyzeScript(final Loc loc, final Reader reader,
                                       final String moduleName,
                                       final List<ImportStatement> imports,
                                       final ModuleDictionary dict)
    {
        final List<Statement> statements = RatsScriptParser.parseScript(reader, loc);

        return statements == null ? null :
            buildModule(loc, moduleName, imports, statements, dict,
                        getIntrinsicsResolver());
    }

    /**
     * Helper - compiles a unit from a list of passed statements,
     * a list of implicitly imported modules and a module name.
     * Returns the sucessfully built unit, or null.
     */
    private static Unit compileModule(final Loc loc,
                                      final String moduleName,
                                      final List<ImportStatement> imports,
                                      final List<Statement> statements,
                                      final UnitDictionary unitDictionary,
                                      final boolean debug)
    {
        final Module module = buildModule(
           loc, moduleName, imports, statements,
           unitDictionary.getModuleDictionary(),
           getIntrinsicsResolver());

        return module == null ? null :
            UnitBuilder.build(module, unitDictionary, debug);
    }

    /**
     * Builds a module from the context of compiling an importing module
     */
    public static Module compileModule(
        final Loc loc, final Reader reader, final String moduleName,
        final ModuleDictionary dict)
    {
        final List<ImportStatement> imports =
            Collections.<ImportStatement>emptyList();
        final List<Statement> statements =
            RatsScriptParser.parseScript(reader, loc);

        return statements == null ? null : buildModule(
            loc, moduleName, imports, statements, dict,
            getIntrinsicsResolver());
    }

    /**
     * Helper - builds and analyzes a module from a list of parsed statements,
     * a list of implicitly imported modules and a module name.
     * Adds the newly created module to the passed ModuleDictionary
     * Returns the successfully analyzed module, or null.
     *
     * TODO reconcile implicitImports param and module.addImport() (used originally
     *      to link to prior shell input) with addImplicitImports(), used to link to
     *      standard libs (from compiler defaults/config), and ImportStatements, used
     *      to process explicit imports from source. Once we have the right import/export
     *      directives, we should be able to use the same infrastructure for everything.
     */
    private static Module buildModule(final Loc loc,
                                      final String moduleName,
                                      final List<ImportStatement> imports,
                                      final List<Statement> statements,
                                      final ModuleDictionary dict,
                                      final IntrinsicsResolver intrinsicsResolver)
    {
        // TODO won't need this when we go to two-phase pipeline
        if (Session.inModule(moduleName))
        {
            Session.error(loc,
                "cyclic module dependencies not yet supported: {0}",
                moduleName);

            return null;
        }

        final boolean isImplicitImport = compilerImports.contains(moduleName);
        final boolean isImplicitImportChild = Session.isInImplicitImport();
        final boolean isImplicitImportRoot = isImplicitImport && !isImplicitImportChild;

        if (isImplicitImportRoot)
            Session.setInImplicitImport();

        // add compiler-implicit imports--see comment header
        final List<Statement> statementsWithPreloads = Session.isInImplicitImport() ?
            statements : addImplicitImports(imports, statements);

        // create new module with passed loc, name, and parsed term list; import intrinsics
        final Module module = new Module(loc, moduleName, statementsWithPreloads, dict,
                intrinsicsResolver);

        // add the newly created module to the passed dictionary
        dict.add(module);

        // analyze
        final boolean analyzed = ModuleAnalyzer.analyze(module);

        // clear implicit import flag if we're a root
        if (isImplicitImportRoot)
            Session.clearInImplicitImport();

        return analyzed ? module : null;
    }

    /**
     *
     */
    private static List<Statement> addImplicitImports(
        final List<ImportStatement> imports,
        final List<Statement> statements)
    {
        assert !Session.isInImplicitImport();

        final List<Statement> prepended = new ArrayList<Statement>();

        for (final String implicitImport : compilerImports)
            prepended.add(
                new ImportStatement(Loc.INTRINSIC, null, implicitImport, null));

        prepended.addAll(imports);
        prepended.addAll(statements);

        return prepended;
    }
}
