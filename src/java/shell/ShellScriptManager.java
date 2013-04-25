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

import compile.Loc;
import compile.ScriptCompiler;
import compile.Session;
import compile.gen.java.*;
import compile.module.Module;
import compile.module.ModuleDictionary;
import compile.term.LambdaTerm;
import compile.term.Statement;
import compile.term.TypeDef;
import compile.term.ValueStatement;
import runtime.rep.ModuleRep;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Holds shell runtime context. Manages chained module import scheme which provides
 * shell's accumulating binding behavior.
 *
 * @author Basil Hosmer
 */
public final class ShellScriptManager
{
    private int seq;
    private UnitDictionary unitDictionary;
    private List<Unit> unitHistory;

    /**
     *
     */
    public ShellScriptManager()
    {
    }

    /**
     * Unit history - all units processed, in reverse order
     */
    public List<Unit> getUnitHistory()
    {
        return unitHistory;
    }

    /**
     * clear shell state
     * Note that we don't reset {@link #seq}, since even when we clear our state,
     * we leave old compiled module classes around that we can't step on.
     */
    public void clear(final List<String> scriptLoads)
    {
        if (unitDictionary != null)
        {
            // release script instance data - since module instances are singleton
            // static members of their representing classes, they won't be reclaimed
            // by simply detaching from units->modules
            for (final Unit unit : unitDictionary.getUnits())
                clearScriptUnit(unit);
        }

        ModuleClassGenerator.newEpoch();

        this.unitDictionary = new UnitDictionary();
        this.unitHistory = new ArrayList<Unit>();

        // also, we have carnal knowledge of Javassist internals
        JavassistHelper.resetClassPool();

        preloadShellScripts(scriptLoads);
    }

    /**
     *
     */
    private void preloadShellScripts(final List<String> scriptLoads)
    {
        if (scriptLoads.size() > 0)
        {
            final StringBuilder sb = new StringBuilder();
            for (final String script : scriptLoads)
            {
                sb.append("import * from ");
                sb.append(script);
                sb.append(";\n");

                if (Session.isDebug())
                    Session.debug(Loc.INTRINSIC, "Preloading " + script + "...");
            }

            runScript(Loc.INTRINSIC, new StringReader(sb.toString()), Session.isDebug(), false);
        }
    }

    /**
     * Retrieve a past unit.
     *
     * @param n number of steps into the past, 0 most recent
     * @return unit, or null if argument is out of range
     */
    public Unit getPastUnit(final int n)
    {
        return unitHistory.size() > n ? unitHistory.get(n) : null;
    }

    /**
     * Compile, save and run passed script text. See {@link #compileScriptUnit}, {@link #loadScriptUnit}.
     */
    public boolean runScript(final Loc loc, final Reader reader,
        final boolean debug, final boolean print)
    {
        final Unit unit = compileScriptUnit(loc, reader, debug, print);

        if (unit != null)
        {
            saveUnit(unit);

            if (Session.isDebug())
                Session.debug(loc, "Loading script...");

            return loadScriptUnit(unit, debug);
        }
        else
        {
            return false;
        }
    }

    /**
     * Compile a unit for passed script.
     * Script implicitly imports previous script, and so has access to all
     * values and types defined in any previous scripts built by this method.
     */
    private Unit compileScriptUnit(final Loc loc, final Reader reader,
        final boolean debug, final boolean print)
    {
        // disambiguate module name with counter
        final String moduleName = "Shell" + seq;
        final List<Module> implicitImports = getPastUnitImportList();

        // compile script module unit
        return ScriptCompiler.compileScript(
            loc, reader, moduleName, implicitImports, unitDictionary, debug, print);
    }

    /**
     * We only need to import the most recently created past unit,
     * since it will import its predecessor, and so on.
     */
    @SuppressWarnings("unchecked")
    private List<Module> getPastUnitImportList()
    {
        final Unit lastUnit = getPastUnit(0);
        return lastUnit != null ? Arrays.asList(lastUnit.getModule()) :
            Arrays.<Module>asList();
    }

    /**
     *
     */
    public boolean writeUnits(final String path)
    {
        for (final Unit unit : unitDictionary.getUnits())
        {
            if (!writeUnit(unit, path))
                return false;
        }

        return true;
    }

    /**
     *
     */
    private boolean writeUnit(final Unit unit, final String path)
    {
        try
        {
            final String srcpath = path + File.separatorChar + "src";
            final String binpath = path + File.separatorChar + "classes";

            final ClassDef moduleClassDef = unit.getModuleClassDef();

            if (Session.isDebug())
                Session.debug(unit.getModule().getLoc(),
                    "writing module class source to directory \"{0}\"",
                    srcpath);

            writeClassDefSource(moduleClassDef, srcpath);

            if (Session.isDebug())
                Session.debug(unit.getModule().getLoc(),
                    "writing module class binary to directory \"{0}\"",
                    binpath);

            moduleClassDef.getCtClass().writeFile(binpath);

            for (final Map.Entry<LambdaTerm, ClassDef> entry : unit.getLambdaClassDefs()
                .entrySet())
            {
                final LambdaTerm lambdaTerm = entry.getKey();
                final ClassDef classDef = entry.getValue();

                if (Session.isDebug())
                    Session.debug(lambdaTerm.getLoc(),
                        "writing lambda class source {0} to directory \"{1}\"",
                        classDef.getName(), srcpath);

                writeClassDefSource(classDef, srcpath);

                if (Session.isDebug())
                    Session.debug(lambdaTerm.getLoc(),
                        "writing lambda class binary {0} to directory \"{1}\"",
                        classDef.getName(), binpath);

                classDef.getCtClass().writeFile(binpath);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeClassDefSource(final ClassDef classDef, final String path)
        throws IOException
    {
        final String classname = classDef.getCtClass().getName();
        final String filename =
            path + File.separatorChar + classname.replace('.', File.separatorChar) +
                ".java";

        // make path
        final int pos = filename.lastIndexOf(File.separatorChar);
        if (pos > 0)
        {
            final String dir = filename.substring(0, pos);
            if (!dir.equals("."))
                new File(dir).mkdirs();
        }

        final FileOutputStream outputStream = new FileOutputStream(filename);
        try
        {
            outputStream.write(classDef.getSource(false).getBytes());
        }
        finally
        {
            outputStream.close();
        }


    }

    /**
     * Save successfully built unit in {@link #unitHistory} and {@link #unitDictionary}.
     */
    private void saveUnit(final Unit unit)
    {
        unitDictionary.add(unit);
        unitHistory.add(0, unit);
        seq++;
    }

    /**
     * Load a unit's module's representing class, create its static INSTANCE, and
     * run INSTANCE's init() method (which contains top-level script code).
     * Debug flag controls whether runtime debug messages are enabled.
     */
    @SuppressWarnings({"UnusedParameters"})
    private boolean loadScriptUnit(final Unit unit, final boolean debug)
    {
        final Class<?> moduleClass = unit.getModuleClassDef().getCls();

        if (moduleClass != null)
        {
            try
            {
                // e.g. class Shell1 { public static final Shell1 INSTANCE = new Shell1(); }
                final Field field = moduleClass.getField(Constants.INSTANCE);

                // get INSTANCE
                final ModuleRep moduleRep = (ModuleRep)field.get(null);

                // run init()
                // TODO ensure that all imported modules have had their init() methods run.
                moduleRep.run();

                return true;
            }
            catch (Exception e)
            {
                // TODO print activation stack from DebugWatcher
                e.printStackTrace();
            }
            catch (Error e)
            {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Clear a unit's module's class's INSTANCE. This is done when clearing old scripts
     */
    private boolean clearScriptUnit(final Unit unit)
    {
        final Class<?> moduleClass = unit.getModuleClassDef().getCls();

        if (moduleClass != null)
        {
            try
            {
                if (Session.isDebug())
                    Session.debug("Setting field ''{0}'' of ''{1}'' to null",
                            Constants.INSTANCE, moduleClass.getName());
                // e.g. class Shell1 { public static final Shell1 INSTANCE = new Shell1(); }
                final Field field = moduleClass.getField(Constants.INSTANCE);

                // clear INSTANCE
                field.set(null, null);

                for (final ClassDef def : unit.getLambdaClassDefs().values())
                {
                    final Class<?> lambdaCls = def.getCls();
                    if (lambdaCls != null)
                    {
                        try 
                        {
                            final Field lfield = lambdaCls.getDeclaredField(Constants.INSTANCE);
                            if (Session.isDebug())
                                Session.debug("Setting field ''{0}'' of ''{1}'' to null",
                                        lfield, lambdaCls.getName());
                            lfield.set(null, null);
                        }
                        catch (NoSuchFieldException e)
                        {
                            /* some lambda don't have an instance field and that's ok. */
                        }
                    }
                }

                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            catch (Error e)
            {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Compile passed script text, and print the types of top-level type bindings and
     * then statements. (I.e., the two are separated, no longer interleaved in decl order.)
     * Return a map from statement dumps to type dumps.
     */
    public Map<String, String> printExprTypes(final Loc loc, final Reader reader)
    {
        final Map<String, String> dumps = new LinkedHashMap<String, String>();

        final ModuleDictionary dict = unitDictionary.getModuleDictionary();
        final Module module = ScriptCompiler
            .analyzeScript(loc, reader, "TypeCheck", getPastUnitImportList(), dict);

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
}
