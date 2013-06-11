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
import compile.gen.Unit;
import compile.gen.UnitManager;
import runtime.rep.ModuleRep;

import java.lang.reflect.Field;

/**
 * Implementation of {@link UnitManager} for the Java backend.
 */
public class JavaUnitManager implements UnitManager
{
    /**
     * Load a unit's generated class, create its static INSTANCE, and
     * run INSTANCE's initialization (i.e., top-level script) code.
     */
    public boolean loadUnit(final Unit unit)
    {
        if (!(unit instanceof JavaUnit))
        {
            Session.error(unit.getModule().getLoc(), "internal error: non-Java unit {0}",
                unit.getModule().getName());

            return false;
        }

        final JavaUnit javaUnit = (JavaUnit)unit;

        final Class<?> moduleClass = javaUnit.getModuleClassDef().getCls();

        if (moduleClass != null)
        {
            try
            {
                // get INSTANCE
                final Field field = moduleClass.getField(Constants.INSTANCE);
                final ModuleRep moduleRep = (ModuleRep)field.get(null);

                // run top-level code
                moduleRep.run();
                return true;
            }
            catch (Exception e)
            {
                // TODO this prints raw Java stack. Need Mesh stack...
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
     * CAUTION: here we know a lot about the structure of a generated unit.
     */
    public boolean unloadUnit(final Unit unit)
    {
        if (!(unit instanceof JavaUnit))
        {
            Session.error(unit.getModule().getLoc(), "internal error: non-Java unit {0}",
                unit.getModule().getName());

            return false;
        }

        final JavaUnit javaUnit = (JavaUnit)unit;

        final Class<?> moduleClass = javaUnit.getModuleClassDef().getCls();

        if (moduleClass != null)
        {
            try
            {
                if (Session.isDebug())
                    Session.debug("Setting field ''{0}'' of ''{1}'' to null",
                        Constants.INSTANCE, moduleClass.getName());

                final Field field = moduleClass.getField(Constants.INSTANCE);

                // clear INSTANCE
                field.set(null, null);

                for (final ClassDef def : javaUnit.getLambdaClassDefs().values())
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
                            // if a lambda is a closure, it won't have an instance field
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
     * CAUTION: we have carnal knowledge of various codegen internals
     */
    public void resetInternals()
    {
        ModuleClassGenerator.newEpoch();
        JavassistHelper.resetClassPool();
    }
}
