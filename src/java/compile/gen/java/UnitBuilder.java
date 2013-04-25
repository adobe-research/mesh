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

import compile.Session;
import compile.module.ImportedModule;
import compile.module.Module;

/**
 * Defines static method {@link #build}, which generates compilation unit for a given
 * module.
 *
 * @author Basil Hosmer
 */
public final class UnitBuilder
{
    /**
     * Development debug only: provide access to generated source on failed compile
     */
    public static Unit LastUnit = null;

    /**
     * Generate a {@link Unit} for given Module.
     * <p/>
     * We do this by first creating an empty ModuleUnit for the module, then generating
     * a ClassDef for the module class, accumulating ancillary ClassDefs etc. into the
     * ModuleUnit on demand.
     * <p/>
     * Note that we do not store the new unit into the unitDictionary. This is typically
     * done by the caller, but might not be desired in some cases.
     *
     * @param module         source module
     * @param unitDictionary must contain module units for all of source module's imports
     * @param debug          debug flag for generated code
     * @return
     */
    public static Unit build(final Module module, final UnitDictionary unitDictionary,
        final boolean debug)
    {
        // create empty unit
        final UnitDictionary extracted = extractImportedUnits(module, unitDictionary, debug);
        final Unit unit = new Unit(module, extracted, debug); 

        // need this for code generation - backptr to unit is for accumulating definitions
        final StatementFormatter statementFormatter = new StatementFormatter(unit);

        // use standard rule for class name for classes we generate
        final String className = ModuleClassGenerator.qualifiedModuleClassName(module);

        // generate module instance classdef, accumulating ancillary definitions
        // into unit via termformatter
        final ClassDef moduleClassDef =
            ModuleClassGenerator.generate(module, className, statementFormatter);

        if (moduleClassDef != null)
        {
            unit.setModuleClassDef(moduleClassDef);

            // stash unit
            LastUnit = unit;

            // generate Java classes from unit classdefs, interfacedefs
            if (new UnitClassGenerator(unit).generate(debug))
            {
                unitDictionary.add(unit);
                return unit;
            }
        }

        return null;
    }

    /**
     * Given a unit dictionary and a module, return the subdictionary consisting of
     * units imported by the passed module.
     *
     * @param module
     * @param unitDictionary
     * @return
     */
    private static UnitDictionary extractImportedUnits(final Module module,
        final UnitDictionary unitDictionary, final boolean debug)
    {
        final UnitDictionary importedUnits = new UnitDictionary();

        for (final ImportedModule importedModule : module.getImportList())
        {
            Unit importedUnit = unitDictionary.getUnit(importedModule.getName());
            if (importedUnit == null)
            {
                importedUnit = build(importedModule.getModule(), unitDictionary, debug);
            }

            if (importedUnit != null)
            {
                importedUnits.add(importedUnit);
            }
            else
            {
                Session.error(
                    "internal error: module \"{0}\" imported by module \"{1}\" not found in dictionary passed to unit constructor",
                    importedModule.getName(), module.getName());
            }
        }

        return importedUnits;
    }
}
