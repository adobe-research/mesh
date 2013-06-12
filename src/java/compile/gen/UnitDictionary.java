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
package compile.gen;

import com.google.common.collect.Maps;
import compile.module.Module;

import java.util.*;

/**
 * Dictionary of compiled {@link compile.gen.java.JavaUnit}s, available by module name
 * and module identity.
 *
 * TODO will need to factor out backend-agnostic Unit/UnitDictionary interfaces
 *
 * @author Basil Hosmer
 */
public final class UnitDictionary
{
    // instance

    private final LinkedHashMap<String, Unit> byName = Maps.newLinkedHashMap();
    private final IdentityHashMap<Module, Unit> byModule = Maps.newIdentityHashMap();

    // cached map of unit modules by name
    private HashMap<String, Module> modules;

    /**
     * Add unit to dictionary.
     */
    public void add(final Unit unit)
    {
        // invalidate cache
        modules = null;

        final Module module = unit.getModule();

        byName.put(module.getName(), unit);
        byModule.put(module, unit);
    }

    /**
     * get Unit by name
     */
    public Unit getUnit(final String name)
    {
        return byName.get(name);
    }

    /**
     * get Unit by module
     */
    public Unit getUnit(final Module module)
    {
        return byModule.get(module);
    }

    /**
     * get module by name
     */
    public Module getModule(final String name)
    {
        return ensureModules().get(name);
    }

    /**
     * get all Units
     */
    public Collection<Unit> getUnits()
    {
        return byName.values();
    }

    /**
     * clear all units
     */
    public void clear()
    {
        modules = null;
        byName.clear();
        byModule.clear();
    }

    /**
     * Cache an extracted module map if none exists
     */
    private Map<String, Module> ensureModules()
    {
        if (modules == null)
        {
            // build and cache module dictionary
            modules = Maps.newHashMap();
            for (final Unit unit : byName.values())
            {
                final Module module = unit.getModule();
                modules.put(module.getName(), module);
            }
        }

        return modules;
    }
}
