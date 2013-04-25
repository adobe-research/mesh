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

import com.google.common.collect.Maps;
import compile.module.ModuleDictionary;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Dictionary of compiled {@link Unit}s by module name.
 *
 * @author Basil Hosmer
 */
public final class UnitDictionary
{
    public static final UnitDictionary EMPTY = new UnitDictionary();

    // instance

    private final LinkedHashMap<String, Unit> map = Maps.newLinkedHashMap();

    /**
     * Add unit to dictionary.
     */
    public void add(final Unit unit)
    {
        map.put(unit.getModule().getName(), unit);
    }

    /**
     * get Unit by name
     */
    public Unit getUnit(final String name)
    {
        return map.get(name);
    }

    /**
     * get all Units
     */
    public Collection<Unit> getUnits()
    {
        return map.values();
    }

    /**
     * Extract a module dictionary
     */
    public ModuleDictionary getModuleDictionary()
    {
        if (map.isEmpty())
        {
            return ModuleDictionary.EMPTY;
        }
        else
        {
            final ModuleDictionary moduleDictionary = new ModuleDictionary();

            for (final Unit unit : map.values())
                moduleDictionary.add(unit.getModule());

            return moduleDictionary;
        }
    }
}
