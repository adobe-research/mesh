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
import compile.module.Module;
import compile.term.LambdaTerm;
import runtime.rep.lambda.Lambda;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Container for module and results of module compilation:
 * <ul>
 * <li/>source module
 * <li/>imported units
 * <li/>class def representing the module at runtime (variables and init code)
 * <li/>Map of class defs for lambda implementations
 * </ul>
 * <p/>
 * {@link UnitBuilder#build} drives
 * the pipeline from {@link Module} to finished {@link Unit}.
 *
 * @author Basil Hosmer
 */
public class Unit
{
    protected final Module module;
    protected final UnitDictionary importedUnits;
    protected final boolean debug;
    protected final Map<LambdaTerm, ClassDef> lambdaClassDefs;
    protected final Map<LambdaTerm, String> newLambdaClassNames;
    protected ClassDef moduleClassDef;

    /**
     * @param module        source module
     * @param importedUnits units corresponding to imported modules
     * @param debug
     */
    public Unit(final Module module, final UnitDictionary importedUnits, final boolean debug)
    {
        this.module = module;
        this.importedUnits = importedUnits;
        this.debug = debug;
        this.lambdaClassDefs = new IdentityHashMap<LambdaTerm, ClassDef>();
        this.newLambdaClassNames = new IdentityHashMap<LambdaTerm, String>();
    }

    public Module getModule()
    {
        return module;
    }

    public Unit getImportedUnit(final String name)
    {
        Unit unit = importedUnits.getUnit(name);

        if (unit != null)
            return unit;

        for (final Unit importedUnit : importedUnits.getUnits())
        {
            unit = importedUnit.getImportedUnit(name);
            if (unit != null)
                return unit;
        }

        return null;
    }

    public UnitDictionary getUnitDictionary()
    {
        return importedUnits;
    }

    public boolean isDebug()
    {
        return debug;
    }

    /**
     * Get collection of lambda implementation class defs.
     */
    public Map<LambdaTerm, ClassDef> getLambdaClassDefs()
    {
        return lambdaClassDefs;
    }

    /**
     * Get module representation classdef
     */
    public ClassDef getModuleClassDef()
    {
        return moduleClassDef;
    }

    /**
     * Set module representation classdef
     */
    public void setModuleClassDef(final ClassDef moduleClassDef)
    {
        this.moduleClassDef = moduleClassDef;
    }

    /**
     * Get lambda class def for lambda term. May come from imported module.
     */
    private ClassDef getLambdaClassDef(final LambdaTerm lambdaTerm)
    {
        ClassDef classDef = lambdaClassDefs.get(lambdaTerm);

        if (classDef != null)
            return classDef;

        for (final Unit importedUnit : importedUnits.getUnits())
        {
            classDef = importedUnit.getLambdaClassDef(lambdaTerm);
            if (classDef != null)
                return classDef;
        }

        return null;
    }

    /**
     * Fetch or generate lambda class def. May come from imported module.
     */
    public ClassDef ensureLambdaClassDef(
        final LambdaTerm lambdaTerm, final StatementFormatter statementFormatter)
    {
        ClassDef classDef = getLambdaClassDef(lambdaTerm);

        if (classDef == null)
        {
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
        String className = newLambdaClassNames.get(lambdaTerm);

        if (className == null)
        {
            // goes in module package
            final String simpleClassName = Lambda.class.getSimpleName() + newLambdaClassNames.size();
            className = ModuleClassGenerator.qualifyModuleClassName(module, simpleClassName);
            newLambdaClassNames.put(lambdaTerm, className);
        }

        return className;
    }

    /**
     *
     */
    public String ensureLambdaClassName(final LambdaTerm lambdaTerm)
    {
        final ClassDef classDef = getLambdaClassDef(lambdaTerm);

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
}
