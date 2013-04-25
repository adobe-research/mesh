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
import compile.Loc;
import compile.term.*;

import java.util.*;

/**
 * A module is a sequence of executable statements,
 * bindings and typedefs. Modules may import other modules.
 * Current compiler produces one module per source file.
 *
 * @author Basil Hosmer
 */
public class Module extends AbstractScope
{
    private static HashMap<String, ParamBinding>
        EMPTY_PARAM_MAP = Maps.newHashMap();

    private final String name;
    private final Map<String, ImportedModule> importMap;
    private final List<ImportedModule> importList;
    private final QualifiedSymbols qualifiedSymbols;

    private final ModuleDictionary moduleDictionary;

    public Module(final Loc loc, final String name, final List<Statement> body, 
        final ModuleDictionary dict)
    {
        super(loc, body);
        this.name = name;
        this.importMap = new LinkedHashMap<String, ImportedModule>();
        this.importList = new ArrayList<ImportedModule>();
        this.moduleDictionary = dict;
        this.qualifiedSymbols = new QualifiedSymbols();
    }

    public String getName()
    {
        return name;
    }

    public ModuleDictionary getModuleDictionary() { return moduleDictionary; }

    /**
     * map of imported modules by name
     */
    public Map<String, ImportedModule> getImportMap()
    {
        return importMap;
    }

    /**
     * List of imported modules
     */
    public List<ImportedModule> getImportList()
    {
        return importList;
    }

    /**
     * note: breadth-first transitive search, most recent imports
     * searched first
     */
    public ImportedModule getImport(final String name)
    {
        ImportedModule module = importMap.get(name);
        if (module != null)
            return module;

        for (final ImportedModule importedModule : importList)
        {
            module = importedModule.getImport(name);
            if (module != null)
                return module;
        }
        return null;
    }

    /**
     * Note: importList order is reversed, so that the contents of later
     * importList occlude the contents of earlier ones.
     */
    public void addImport(final ImportedModule module)
    {
        importMap.put(module.getName(), module);
        importList.add(0, module);
    }

    // Scope

    public Scope getParentScope()
    {
        return null;
    }

    public void setParentScope(final Scope parentScope)
    {
        assert false : "setParentScope on module";
    }

    public Module getModule()
    {
        return this;
    }

    public boolean isLambda()
    {
        return false;
    }

    public ValueBinding getValueBinding(final String name)
    {
        final LetBinding let = lets.get(name);
        if (let != null)
            return let;

        // look for a binding with this name locally defined in
        // a module we import - most recent first
        for (final ImportedModule importedModule : importList)
        {
            final ValueBinding binding = importedModule.getValueBinding(name);
            if (binding != null)
                return binding;
        }

        return null;
    }

    public HashMap<String, ParamBinding> getParams()
    {
        return EMPTY_PARAM_MAP;
    }

    public ValueBinding findValueBinding(final String name)
    {
        return getValueBinding(name);
    }

    public ValueBinding findValueBinding(final String qualifier, final String name)
    {
        ValueBinding vb = qualifiedSymbols.getValue(qualifier, name);
        if (vb != null)
            return vb;

        for (final ImportedModule importedModule : importList)
        {
            vb = importedModule.findValueBinding(qualifier, name);
            if (vb != null)
                return vb;
        }

        return null;
    }

    public TypeDef findType(final String name)
    {
        final TypeDef def = getTypeDef(name);
        if (def != null)
            return def;

        // search bindings in imported modules - most recent import first
        for (final ImportedModule importedModule : importList)
        {
            final TypeDef importedDef = importedModule.findType(name);
            if (importedDef != null)
                return importedDef;
        }

        return null;
    }

    public TypeDef findType(final String qualifier, final String name)
    {
        TypeDef td = qualifiedSymbols.getType(qualifier, name);
        if (td != null)
            return td;

        for (final ImportedModule importedModule : importList)
        {
            td = importedModule.findType(qualifier, name);
            if (td != null)
                return td;
        }

        return null;
    }

    public TypeDef getTypeDef(final String name)
    {
        return typeDefs.get(name);
    }

    public Map<String,TypeDef> getTransitiveTypeDefs() 
    {
        final Map<String,TypeDef> types = new HashMap<String,TypeDef>();
        types.putAll(getTypeDefs());

        for (final ImportedModule imported : importList)
            types.putAll(imported.getTransitiveTypeDefs());

        return types;
    }

    public void addQualifiedSymbols(final String namespace, final Module imported)
    {
        qualifiedSymbols.putAllTypes(namespace, imported);
        qualifiedSymbols.putAllValues(namespace, imported);
    }

    public Map<String,LetBinding> getTransitiveLets() 
    {
        final Map<String,LetBinding> lets = new HashMap<String,LetBinding>();
        lets.putAll(getLets());

        for (final ImportedModule imported : importList)
            lets.putAll(imported.getTransitiveLets());

        return lets;
    }

    public Map<String,LetBinding> getTransitiveLets(final String namespace)
    {
        final Map<String,LetBinding> lets = new HashMap<String,LetBinding>();
        lets.putAll(qualifiedSymbols.getValues(namespace));

        for (final ImportedModule imported : importList)
            lets.putAll(imported.getTransitiveLets(namespace));

        return lets;
    }

    public Set<String> getTransitiveNamespaces()
    {
        final Set<String> ns = new HashSet<String>();
        ns.addAll(qualifiedSymbols.getNamespaces());

        for (final ImportedModule imported : importList)
            ns.addAll(imported.getTransitiveNamespaces());

        return ns;
    }

    public void addDependency(final Statement statement, final Binding binding)
    {
        // NOTE: we only track dependencies whose targets are in our immediate module, NOT in
        // imported modules. This will break as soon as there are non-intrinsic imports.
        // TODO fix when we have real module imports

        if (binding.getScope() == this)
        {
            super.addDependency(statement, binding);
        }
    }

    // Dumpable

    public String dump()
    {
        return ModuleDumper.dump(this);
    }
}
