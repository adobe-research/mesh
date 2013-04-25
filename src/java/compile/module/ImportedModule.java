
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

import compile.term.*;

import java.util.*;

/**
 * An imported module is a wrapper around a module which adds restrictions 
 * on the symbols that are visible in the default namespace.  It also 
 * contains some information to identify the style of import 
 * (implicit,manual import, etc.)
 *
 * @author Keith McGuigan
 */
public class ImportedModule
{
    private final Module module;
    private final boolean implicitImport;
    private final Set<String> exports;
    private boolean fullyExported;

    public ImportedModule(final Module wrapped, final boolean implicitImport)
    {
        this.module = wrapped;
        this.implicitImport = implicitImport;
        this.exports = new HashSet<String>();
        this.fullyExported = false;
    }

    public Module getModule() { return module; }
    public boolean isImplicitImport() { return implicitImport; }

    public void setFullyExported() { fullyExported = true; }

    public String getName()
    {
        return module.getName();
    }

    public ModuleDictionary getModuleDictionary() { return module.getModuleDictionary(); }

    /**
     * List of imported modules
     */
    public List<ImportedModule> getImportList()
    {
        return module.getImportList();
    }

    /**
     * map of imported modules by name
     */
    public Map<String, ImportedModule> getImportMap()
    {
        return module.getImportMap();
    }

    /**
     * note: breadth-first transitive search, most recent imports
     * searched first
     */
    public ImportedModule getImport(final String name)
    {
        return module.getImport(name);
    }

    /**
     * Note: importList order is reversed, so that the contents of later
     * importList occlude the contents of earlier ones.
     */
    public void addImport(final Module module, final boolean isImplicit)
    {
        module.getImportMap().put(module.getName(), 
            new ImportedModule(module, isImplicit));
    }

    /**
     * Add a symbol name which will be passed through to the underlying module.
     */
    public void addExport(final String sym)
    {
        exports.add(sym);
    }

    public ValueBinding getValueBinding(final String name)
    {
        if (fullyExported || implicitImport || exports.contains(name)) 
        {
            return module.getValueBinding(name);
        }
        return null;
    }

    public ValueBinding findValueBinding(final String name)
    {
        return getValueBinding(name);
    }

    public ValueBinding findValueBinding(final String qualifier, final String name)
    {
        if (implicitImport)
        {
            // Namespaces are only inherited from implicitly imported modules
            return module.findValueBinding(qualifier, name);
        }
        else
        {
            return null;
        }
    }

    public TypeDef findType(final String name)
    {
        if (fullyExported || implicitImport || exports.contains(name))
        {
            return module.findType(name);
        }
        return null;
    }

    public TypeDef findType(final String qualifier, final String name)
    {
        if (implicitImport)
        {
            // Namespaces are only inherited from implicitly imported modules
            return module.findType(qualifier, name);
        }
        return null;
    }

    public TypeDef getTypeDef(final String name)
    {
        return findType(name);
    }

    public Map<String,TypeDef> getTransitiveTypeDefs() 
    {
        final Map<String,TypeDef> types = new HashMap<String,TypeDef>();
        if (fullyExported || implicitImport)
        {
            types.putAll(module.getTransitiveTypeDefs());
        }
        else 
        {
            for (final String sym : exports)
            {
                final TypeDef td = module.getTypeDef(sym);
                if (td != null)
                    types.put(sym, td);
            }
        }
        return types;
    }

    public Map<String,LetBinding> getTransitiveLets() 
    {
        final Map<String,LetBinding> lets = new HashMap<String,LetBinding>();
        if (fullyExported || implicitImport)
        {
            lets.putAll(module.getTransitiveLets());
        }
        else 
        {
            for (final String sym : exports)
            {
                final LetBinding lb = (LetBinding)module.getValueBinding(sym);
                if (lb != null)
                    lets.put(sym, lb);
            }
        }
        return lets;
    }

    public Map<String,LetBinding> getTransitiveLets(final String namespace) 
    {
        final Map<String,LetBinding> lets = new HashMap<String,LetBinding>();
        if (implicitImport)
            lets.putAll(module.getTransitiveLets(namespace));
        return lets;
    }

    public Set<String> getTransitiveNamespaces()
    {
        final Set<String> ns = new HashSet<String>();
        if (implicitImport)
            ns.addAll(module.getTransitiveNamespaces());
        return ns;
    }
}
