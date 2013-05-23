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
    private final Map<String, Module> importMap;
    private final List<Import> imports;
    private WhiteList exports;
    private boolean exportLocalsOnly; // for "export ."

    private final ModuleDictionary moduleDictionary;

    public Module(final Loc loc, final String name, final List<Statement> body, 
        final ModuleDictionary dict)
    {
        super(loc, body);
        this.name = name;
        this.importMap = new LinkedHashMap<String, Module>();
        this.imports = new LinkedList<Import>();
        this.exports = WhiteList.open();
        this.exportLocalsOnly = false;
        this.moduleDictionary = dict;
    }

    public String getName()
    {
        return name;
    }

    public ModuleDictionary getModuleDictionary() { return moduleDictionary; }

    /**
     * map of imported modules by name
     */
    public Map<String, Module> getImportMap()
    {
        return importMap;
    }

    public List<Import> getImports()
    {
        return imports;
    }

    public void setExportLocalsOnly()
    {
        exportLocalsOnly = true;
    }

    public void setExports(final WhiteList wl)
    {
        exports = wl;
    }

    public boolean isExported(final String name)
    {
        if (exportLocalsOnly) 
            return getValueBinding(name) != null || getTypeDef(name) != null;
        else
            return exports.allows(name);
    }

    /**
     * List of imported modules
     */
    public List<Module> getImportList()
    {
        return new ArrayList<Module>(importMap.values());
    }

    /**
     * note: breadth-first transitive search
     */
    public Module getImportedModule(final String name)
    {
        final Module module = importMap.get(name);
        if (module != null)
            return module;

        for (final Module importedModule : importMap.values())
        {
            final Module sub = importedModule.getImportedModule(name);
            if (sub != null)
                return sub;
        }
        return null;
    }

    /**
     * Note: importList order is reversed, so that the contents of later
     * importList occlude the contents of earlier ones.
     */
    public void addImport(final Import imp)
    {
        importMap.put(imp.getModule().getName(), imp.getModule());
        imports.add(0, imp);
    }

    public Set<String> getNamespaces()
    {
        final Set<String> namespaces = new HashSet<String>();
        for (final Import imp : imports)
        {
            final String qualifier = imp.getQualifier();
            if (qualifier != null) 
                namespaces.add(qualifier);
        }
        return namespaces;
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
        return lets.get(name);
    }

    public Set<String> getLetNames() 
    {
        return lets.keySet();
    }

    public Set<String> getUnqualifiedNames() 
    {
        final Set<String> names = new HashSet<String>();
        names.addAll(getLetNames());
        for (final Import imp : getImports())
            if (imp.getQualifier() == null) 
                names.addAll(imp.getModule().getUnqualifiedNames());
        return names;
    }

    public HashMap<String, ParamBinding> getParams()
    {
        return EMPTY_PARAM_MAP;
    }

    public ValueBinding findValueBinding(final String qname)
    {
        return findValueBinding(qname, true);
    }

    public ValueBinding findValueBinding(
        final String qname, final boolean qualifiedOk)
    {
        final ValueBinding vb = getValueBinding(qname);

        if (vb != null) 
            return vb;

        // look for a binding with this name locally defined in
        // a module we import - most recent first
        for (final Import imp : imports) 
        {
            if (imp.getQualifier() == null || qualifiedOk)
            {
                final ValueBinding binding = imp.findValueBinding(qname);
                if (binding != null) 
                    return binding;
            }
        }
        return null;
    }

    public TypeDef findType(final String qname)
    {
        return findType(qname, true);
    }

    public TypeDef findType(final String qname, final boolean qualifiedOk)
    {
        final TypeDef td = getTypeDef(qname);
        
        if (td != null) 
            return td;

        for (final Import imp : imports) 
        {
            if (imp.getQualifier() == null || qualifiedOk)
            {
                final TypeDef type = imp.findTypeDef(qname);
                if (type != null) 
                    return type;
            }
        }
        return null;
    }

    public TypeDef getTypeDef(final String name)
    {
        return typeDefs.get(name);
    }

    public void addDependency(final Statement statement, final Binding binding)
    {
        // NOTE: we only track dependencies whose targets are in our immediate module,
        // NOT in imported modules. This forces all imports to have been typechecked
        // before us, which precludes cycles in imports under current pipeline.
        // TODO either 1) no cycles, 2) split pipeline + annotations in cycles,
        // 3) cross-module typechecking. (2) looks like the winner currently

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
