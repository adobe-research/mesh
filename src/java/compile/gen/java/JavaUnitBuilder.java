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

import compile.gen.Unit;
import compile.gen.UnitBuilder;
import compile.gen.UnitDictionary;
import compile.module.Module;

/**
 * Defines static method {@link #build}, which generates
 * compilation unit for a given module.
 *
 * @author Basil Hosmer
 */
public final class JavaUnitBuilder implements UnitBuilder.Impl
{
    private static Unit LastUnit = null;

    /**
     * For shell/debug convenience: provide access to the most recent
     * Unit built, whether or not the build was successful.
     */
    public Unit getLastBuildAttempt()
    {
        return LastUnit;
    }

    /**
     * Generate a {@link JavaUnit} for given Module.
     * We do this by first creating an empty Unit for the module, then generating
     * a ClassDef for the module class, accumulating ancillary ClassDefs etc. into
     * the Unit on demand. Finally we generate Java bytecode for the accumulated
     * definitions.
     */
    public JavaUnit build(final Module module, final UnitDictionary unitDictionary)
    {
        // create empty unit
        final JavaUnit unit = new JavaUnit(module);

        // need this for code generation - backptr to unit is for accumulating definitions
        final StatementFormatter statementFormatter =
            new StatementFormatter(unit, unitDictionary);

        // use standard rule for class name for classes we generate
        final String className = ModuleClassGenerator.qualifiedModuleClassName(module);

        // generate module instance classdef, accumulating ancillary definitions
        // into unit via termformatter
        final ClassDef moduleClassDef =
            ModuleClassGenerator.generate(module, className, statementFormatter);

        if (moduleClassDef == null)
            return null;

        unit.setModuleClassDef(moduleClassDef);

        // stash unit before attempting final codegen
        LastUnit = unit;

        // generate Java classes from unit classdefs, interfacedefs
        return new UnitClassGenerator(unit).generate() ? unit : null;
    }
}
