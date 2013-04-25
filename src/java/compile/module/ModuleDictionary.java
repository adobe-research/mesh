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
package compile.module;

import com.google.common.collect.Maps;
import compile.module.intrinsic.BuiltinModule;

import java.util.HashMap;

/**
 * @author Basil Hosmer
 */
public final class ModuleDictionary
{
    public static final ModuleDictionary EMPTY = new ModuleDictionary();

    // instance

    private final HashMap<String, Module> map = Maps.newHashMap();

    /**
     * Note: preload with intrinsics module
     */
    public ModuleDictionary()
    {
        map.put(BuiltinModule.INSTANCE.getName(), BuiltinModule.INSTANCE);
    }

    /**
     * Add module to dictionary.
     *
     * @param module
     */
    public void add(final Module module)
    {
        map.put(module.getName(), module);
    }

    public Module get(final String name) 
    {
        return map.get(name);
    }
}
