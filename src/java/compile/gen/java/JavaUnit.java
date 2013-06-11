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

import compile.*;
import compile.gen.Unit;
import compile.gen.UnitDictionary;
import compile.module.Module;
import compile.module.Scope;
import compile.term.LambdaTerm;
import compile.term.Term;
import runtime.rep.Lambda;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Container for module and results of module compilation:
 * <ul>
 * <li/>source module
 * <li/>imported units
 * <li/>class def representing the module at runtime (variables and init code)
 * <li/>Map of class defs for lambda implementations
 * </ul>
 * <p/>
 * {@link JavaUnitBuilder#build} drives
 * the pipeline from {@link Module} to finished {@link JavaUnit}.
 *
 * @author Basil Hosmer
 */
public class JavaUnit implements Unit
{
    protected final Module module;
    protected final Map<LambdaTerm, ClassDef> lambdaClassDefs;
    protected final Map<LambdaTerm, String> newLambdaClassNames;
    protected ClassDef moduleClassDef;

    /**
     *
     */
    JavaUnit(final Module module)
    {
        this.module = module;
        this.lambdaClassDefs = new IdentityHashMap<LambdaTerm, ClassDef>();
        this.newLambdaClassNames = new IdentityHashMap<LambdaTerm, String>();
    }

    /**
     * Get unit's source module.
     */
    public Module getModule()
    {
        return module;
    }

    /**
     * Get collection of lambda implementation class defs.
     */
    Map<LambdaTerm, ClassDef> getLambdaClassDefs()
    {
        return lambdaClassDefs;
    }

    /**
     * Get module representation classdef
     */
    ClassDef getModuleClassDef()
    {
        return moduleClassDef;
    }

    /**
     * Set module representation classdef
     */
    void setModuleClassDef(final ClassDef moduleClassDef)
    {
        this.moduleClassDef = moduleClassDef;
    }

    /**
     * Get lambda class def for lambda term. May come from imported module.
     */
    ClassDef getLambdaClassDef(final LambdaTerm lambdaTerm, UnitDictionary unitDictionary)
    {
        final Module lambdaModule = lambdaTerm.getModule();

        // in-module lambda
        if (lambdaModule == module)
            return lambdaClassDefs.get(lambdaTerm);

        // imported module lambda
        final Unit lambdaUnit = unitDictionary.getUnit(lambdaModule);
        assert lambdaUnit != null;

        if (!(lambdaUnit instanceof JavaUnit))
        {
            Session.error(lambdaModule.getLoc(),
                "use of non-Java modules not supported: {0}", lambdaModule.getName());

            return null;
        }

        return ((JavaUnit)lambdaUnit).getLambdaClassDef(lambdaTerm, unitDictionary);
    }

    /**
     * Fetch or generate lambda class def. May come from imported module.
     */
    ClassDef ensureLambdaClassDef(final LambdaTerm lambdaTerm,
        final StatementFormatter statementFormatter)
    {
        ClassDef classDef = getLambdaClassDef(
            lambdaTerm, statementFormatter.getUnitDictionary());

        if (classDef == null)
        {
            // must be local if it hasn't been generated yet
            assert lambdaTerm.getModule() == module;

            // grow the map before generating - avoids collisions when we generate nested lambdas
            lambdaClassDefs.put(lambdaTerm, null);

            final String className = getNewLambdaClassName(lambdaTerm);

            classDef = LambdaClassGenerator.generate(lambdaTerm, className, statementFormatter);

            // swap null for real def
            lambdaClassDefs.put(lambdaTerm, classDef);
        }

        return classDef;
    }

    /**
     *
     */
    private String getNewLambdaClassName(final LambdaTerm lambdaTerm)
    {
        // we should only be doing this for local lambdas
        assert lambdaTerm.getModule() == module;

        String className = newLambdaClassNames.get(lambdaTerm);

        if (className == null)
        {
            String name;

            if (lambdaTerm.hasBindingName())
            {
                name = "_" + lambdaTerm.getBindingName();
            }
            else
            {
                name = Lambda.class.getSimpleName() + newLambdaClassNames.size();

                // Note: store synthetic name for use in child lambdas
                lambdaTerm.setBindingName(name);
            }

            Scope parentScope = lambdaTerm.getParentScope();
            while (parentScope instanceof LambdaTerm)
            {
                // Note: parent is guaranteed to have a binding name, see below
                name = "_" + ((LambdaTerm)parentScope).getBindingName() + "$" + name;
                parentScope = parentScope.getParentScope();
            }

            // goes in module package
            className = ModuleClassGenerator.qualifyModuleClassName(module, name);

            newLambdaClassNames.put(lambdaTerm, className);
        }

        return className;
    }

    /**
     *
     */
    String ensureLambdaClassName(
        final LambdaTerm lambdaTerm, final UnitDictionary unitDictionary)
    {
        final ClassDef classDef = getLambdaClassDef(lambdaTerm, unitDictionary);

        if (classDef != null)
        {
            return classDef.getName();
        }
        else if (lambdaTerm.getModule() == module)
        {
            return getNewLambdaClassName(lambdaTerm);
        }
        else
        {
            Session.error(lambdaTerm.getLoc(),
                "internal error: nonlocal lambda has no generated class available");

            return null;
        }
    }

    /**
     * Write a representation of the unit to the given file path
     */
    public boolean write(final String path)
    {
        try
        {
            final String srcpath = path + File.separatorChar + "src";
            final String binpath = path + File.separatorChar + "classes";

            if (Session.isDebug())
                Session.debug(module.getLoc(),
                    "writing module class source to directory \"{0}\"",
                    srcpath);

            writeClassDefSource(moduleClassDef, srcpath);

            if (Session.isDebug())
                Session.debug(module.getLoc(),
                    "writing module class binary to directory \"{0}\"",
                    binpath);

            moduleClassDef.getCtClass().writeFile(binpath);

            for (final Map.Entry<LambdaTerm, ClassDef> entry : lambdaClassDefs.entrySet())
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
    private static void writeClassDefSource(final ClassDef classDef, final String path)
        throws IOException
    {
        final String classname = classDef.getCtClass().getName();
        final String filename = path + File.separatorChar +
            classname.replace('.', File.separatorChar) + ".java";

        // make path
        final int pos = filename.lastIndexOf(File.separatorChar);
        if (pos > 0)
        {
            final String dir = filename.substring(0, pos);
            if (!dir.equals("."))
            {
                if (!new File(dir).mkdirs())
                {
                    Session.warn("failed to make path {0}", dir);
                    // TODO handle fail??
                }
            }
        }

        final FileOutputStream outputStream = new FileOutputStream(filename);
        try
        {
            outputStream.write(classDef.getSource().getBytes());
        }
        finally
        {
            outputStream.close();
        }
    }

    //
    // Dumpable
    //

    /**
     *
     */
    public String dump()
    {
        return
            "*** Unit[" + getModule().getName() + "] ***\n\n" +
                getModule().dump() + "\n\n// moduleClassDef\n\n" +
                getModuleClassDef().getSource() +
                (getLambdaClassDefs().isEmpty() ? "" : "\n\n// lambdaClassDefs\n\n") +
                StringUtils.join(dumpClassDefs(getLambdaClassDefs()), "\n\n");
    }

    /**
     *
     */
    private static Collection<String> dumpClassDefs(
        final Map<LambdaTerm, ClassDef> classDefs)
    {
        final List<String> dumps = new ArrayList<String>();
        final TreeMap<String, LambdaTerm> namesToLambdas = new TreeMap<String, LambdaTerm>();

        for (final Map.Entry<LambdaTerm, ClassDef> entry : classDefs.entrySet())
            namesToLambdas.put(entry.getValue().getName(), entry.getKey());

        for (final Map.Entry<String, LambdaTerm> entry : namesToLambdas.entrySet())
        {
            final LambdaTerm term = entry.getValue();
            final String header = commentHeader(term);
            final String source = classDefs.get(entry.getValue()).getSource();
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
