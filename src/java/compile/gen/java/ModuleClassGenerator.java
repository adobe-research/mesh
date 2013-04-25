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

import compile.analyze.SymbolConstantCollector;
import compile.module.ImportedModule;
import compile.module.Module;
import compile.module.intrinsic.BuiltinModule;
import compile.term.*;
import compile.type.Types;
import runtime.rep.ModuleRep;

import java.util.*;

/**
 * {@link #generate} produces a classdef for a module.
 *
 * @author Basil Hosmer
 */
public final class ModuleClassGenerator extends ClassGenerator
{
    private static int epochCounter = 0;

    // Used after a $clear to get all new class names
    public static void newEpoch() { ++epochCounter; }

    /**
     * Create a {@link ClassDef} to represent this module's
     * definitions and state at runtime. Necessary subdefinitions
     * are created on demand and stored to the {@link Unit} associated
     * with the passed {@link StatementFormatter}.
     */
    public static ClassDef generate(final Module module,
        final String className, final StatementFormatter fmt)
    {
        final ClassDef moduleClassDef = new ClassDef(className);

        // module rep class implements {@link ModuleRep}
        moduleClassDef.addInterfaceName(ModuleRep.class.getName());

        // add static INSTANCE field
        moduleClassDef.addStaticFieldDef(buildInstanceFieldDef(className));

        // add private RAN field 
        addRunLatchField(moduleClassDef);

        // add fields for constants
        addConstantFields(module, moduleClassDef, fmt);

        // add fields for value bindings
        addValueBindingFields(module, moduleClassDef, fmt);

        // add run() method
        moduleClassDef.addMethodDef(buildRunMethodDef(module, fmt));

        // add main() method
        moduleClassDef.addMethodDef(buildMainMethodDef(module));

        return moduleClassDef;
    }

    /**
     *
     */
    private static FieldDef buildInstanceFieldDef(final String className)
    {
        // NOTE: non-final so shell can null it out on $clear
        final String decl = "public static " + className + " " + Constants.INSTANCE;
        final String init = "new " + className + "()";
        return new FieldDef(decl, init);
    }

    /**
     * Add module's field definitions to passed {@link ClassDef}.
     * Note: lets are not initialized inline or in a constructor,
     * due to Javassist issues around init/cinit ordering. Instead
     * they're initialized in the generated run() method.
     *
     * But note that we're declaring them final anyway, since at
     * runtime they are in fact written exactly once. Javassist
     * doesn't appear to do any static flow check that prevents
     * this.
     */
    private static void addValueBindingFields(final Module module,
        final ClassDef classDef, final StatementFormatter fmt)
    {
        for (final LetBinding let : module.getLets().values())
            classDef.addFieldDef(new FieldDef(
                formatFieldDecl(let, true, true, fmt), ""));
    }

    /**
     * add fields for constant initializers
     */
    private static void addConstantFields(final Module module, final ClassDef classDef,
        final StatementFormatter fmt)
    {
        final SymbolConstantCollector collector = new SymbolConstantCollector(module);
        collector.collect();

        // add symbol constants

        final String symbolTypeFmt = fmt.formatType(Types.SYMBOL);

        for (final SymbolLiteral symbol : collector.getSymbolConstants())
        {
            final String fieldName =
                StatementFormatter.formatName("$symc_" + symbol.getValue());

            classDef.addFieldDef(
                new FieldDef("public static final " + symbolTypeFmt + " " + fieldName,
                    fmt.formatTermAs(symbol, Types.SYMBOL)));

            fmt.addSymbolConstant(symbol, classDef.getName() + "." + fieldName);
        }

        // symbol list constants

        // different record types may generate the same keyset,
        // keep track to avoid dupes
        final HashSet<String> keysetFields = new HashSet<String>();

        for (final List<SimpleLiteralTerm> keyList : collector.getKeysetConstants())
        {
            final StringBuilder nameBuilder = new StringBuilder();
            final List<String> exprs = new ArrayList<String>();

            for (final SimpleLiteralTerm key : keyList)
            {
                if (key instanceof SymbolLiteral)
                    nameBuilder.append("$").append(((SymbolLiteral)key).getValue());

                exprs.add(fmt.formatTermAs(key, Object.class));
            }

            final String name = nameBuilder.toString();

            final String fieldName = StatementFormatter.formatName(
                name.isEmpty() ? "$keyset" + keysetFields.size() : name);

            // symbol keysets are 1-1 with their names, so multiple types may reuse
            if (!keysetFields.contains(fieldName))
            {
                keysetFields.add(fieldName);

                final String value = fmt.formatObjectArrayLiteral(exprs);

                classDef.addFieldDef(
                    new FieldDef("public static final Object[] " + fieldName, value));
            }

            fmt.addKeyListConstant(keyList, classDef.getName() + "." + fieldName);
        }
    }

    /**
     * add latch field to prevent run() from running multiple times.
     */
    private static void addRunLatchField(final ClassDef classDef)
    {
        classDef.addFieldDef(new FieldDef("private boolean RAN", "false"));
    }

    private static void addRunLatch(final MethodDef methodDef)
    {
        methodDef.addStatement(new JavaStatement("if (RAN == true) return"));
        methodDef.addStatement(new JavaStatement("RAN = true"));
    }

    /**
     * run() method executes top-level statements, including non-constant
     * initializations.
     * Note: supporting classdefs for lambdas, etc. are generated on-demand
     * as we traverse the top-level statement list.
     */
    private static MethodDef buildRunMethodDef(final Module module,
        final StatementFormatter fmt)
    {
        final String sig = "public final void run()";
        final MethodDef methodDef = new MethodDef(sig);

        addRunLatch(methodDef);

        for (final Statement stmt : module.getBody())
        {
            final String expr = fmt.formatTopLevelStatement(stmt);
            methodDef.addStatement(new JavaStatement(stmt, expr));
        }

        return methodDef;
    }

    /**
     * main() method just calls INSTANCE.run(), throwing away args.
     */
    private static MethodDef buildMainMethodDef(final Module module)
    {
        final String sig = "public static void main(String[] args)";
        final MethodDef methodDef = new MethodDef(sig);

        final Set<Module> runModules = new HashSet<Module>();

        for (final ImportedModule importedModule : module.getImportMap().values())
            addModuleRunStatement(methodDef, importedModule.getModule(), runModules);

        methodDef.addStatement(new JavaStatement("INSTANCE.run()"));

        return methodDef;
    }

    /**
     *
     */
    private static void addModuleRunStatement(final MethodDef methodDef,
        final Module module, final Set<Module> runModules)
    {
        for (final ImportedModule importedModule : module.getImportMap().values())
        {
            addModuleRunStatement(methodDef, importedModule.getModule(), runModules);
        }

        if (!(module instanceof BuiltinModule))
            if (!runModules.contains(module))
            {
                methodDef.addStatement(
                        new JavaStatement(qualifiedModuleClassName(module) + ".INSTANCE.run()"));
                runModules.add(module);
            }
    }

    /**
     * qualified module class name
     */
    public static String qualifiedModuleClassName(final Module module)
    {
        return qualifyModuleClassName(module, "Module");
    }

    /**
     * Qualify class name with module.
     */
    public static String qualifyModuleClassName(final Module module,
        final String className)
    {
        return module.getName().toLowerCase() + "_" + epochCounter + "." + className;
    }
}
