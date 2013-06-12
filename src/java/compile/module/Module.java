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

import compile.Loc;
import compile.NameUtils;
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
    /**
     * module name
     */
    private final String name;

    /**
     * unqualified imports
     */
    private final List<Import> unqualifiedImports;

    /**
     * map of import specs by namespace.
     */
    private final Map<String, List<Import>> qualifiedImports;

    /**
     * whitelist of exported symbols
     */
    private WhiteList exports;

    /**
     * aggregate list of imported modules, derived on demand
     * from {@link #unqualifiedImports} and {@link #qualifiedImports}
     * and cached.
     */
    private List<Module> importedModules;

    /**
     *
     */
    public Module(final Loc loc, final String name, final List<Statement> body)
    {
        super(loc, body);
        this.name = name;
        this.unqualifiedImports = new ArrayList<Import>();
        this.qualifiedImports = new LinkedHashMap<String, List<Import>>();
        this.exports = WhiteList.open();

        this.importedModules = null;
    }

    /**
     * module name
     */
    public String getName()
    {
        return name;
    }

    /**
     * list of unqualified imports
     */
    public List<Import> getUnqualifiedImports()
    {
        return unqualifiedImports;
    }

    /**
     * return list of imports for a given namespace.
     */
    public List<Import> getNamespaceImports(final String namespace)
    {
        final List<Import> nsimps = qualifiedImports.get(namespace);
        return nsimps != null ? nsimps : Collections.<Import>emptyList();
    }

    /**
     * Return flat set of imported modules, derived from from our imports.
     * Note: cached.
     */
    public List<Module> getImportedModules()
    {
        // return cached list if available
        if (this.importedModules != null)
            return this.importedModules;

        // build list
        final List<Module> importedModules = new ArrayList<Module>();
        {
            // unqualified
            for (final Import imp : unqualifiedImports)
                importedModules.add(imp.getModule());

            // qualified by namespace
            for (final List<Import> imports : this.qualifiedImports.values())
                for (final Import imp : imports)
                    importedModules.add(imp.getModule());
        }

        // cache and return
        this.importedModules = importedModules;
        return importedModules;
    }

    /**
     * Note: import order is reversed within per-namespace lists,
     * so that the contents of later importList occlude the contents
     * of earlier ones.
     */
    public void addImport(final Import imp)
    {
        // invalidate cache
        importedModules = null;

        if (imp.isQualified())
        {
            // get or create import set for this namespace
            final List<Import> nsimps;
            {
                final String ns = imp.getNamespace();
                if (qualifiedImports.containsKey(ns))
                {
                    nsimps = qualifiedImports.get(ns);
                }
                else
                {
                    nsimps = new ArrayList<Import>();
                    qualifiedImports.put(ns, nsimps);
                }
            }

            // add this import -- duplicates should be errors before now
            assert !nsimps.contains(imp);
            nsimps.add(0, imp);
        }
        else
        {
            assert !unqualifiedImports.contains(imp);
            unqualifiedImports.add(imp);
        }
    }

    /**
     * return the set of inhabited namespaces in the module.
     */
    public Set<String> getNamespaces()
    {
        return qualifiedImports.keySet();
    }

    /**
     * set export whitelist. Note: caller's responsibility
     * to ensure whitelist validity w.r.t. our definitions.
     */
    public void setExports(final WhiteList exports)
    {
        assert exports.isValid(this);
        this.exports = exports;
    }

    /**
     * true iff the given name matches an exported definition
     */
    public boolean isExported(final String name)
    {
        return exports.allows(name);
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

    /**
     * resolve a value binding by (possibly) qualified name, either in
     * our local definitions or in our imports.
     */
    public ValueBinding findValueBinding(final String qname)
    {
        if (NameUtils.isQualified(qname))
        {
            // qualified name must be imported (currently): look in
            // modules imported into our namespace, if any
            for (final Import imp : getNamespaceImports(NameUtils.qualifier(qname)))
            {
                final ValueBinding binding = imp.findValueBinding(qname);
                if (binding != null)
                    return binding;
            }
        }
        else
        {
            // first check local defs, then unqualified imports
            final ValueBinding localDef = getLocalValueBinding(qname);
            if (localDef != null)
                return localDef;

            for (final Import imp : unqualifiedImports)
            {
                final ValueBinding binding = imp.findValueBinding(qname);
                if (binding != null)
                    return binding;
            }
        }

        return null;
    }

    /**
     * find a locally defined value binding by unqualified name.
     */
    public ValueBinding getLocalValueBinding(final String name)
    {
        return lets.get(name);
    }

    /**
     * resolve a type binding by (possibly) qualified name, either in
     * our local definitions or in our imports.
     */
    public TypeBinding findTypeBinding(final String qname)
    {
        if (NameUtils.isQualified(qname))
        {
            // qualified name must be imported (currently): look in
            // modules imported into our namespace, if any
            for (final Import imp : getNamespaceImports(NameUtils.qualifier(qname)))
            {
                final TypeBinding binding = imp.findTypeBinding(qname);
                if (binding != null)
                    return binding;
            }
        }
        else
        {
            // first check local defs, then unqualified imports
            final TypeBinding localDef = getLocalTypeBinding(qname);
            if (localDef != null)
                return localDef;

            for (final Import imp : unqualifiedImports)
            {
                final TypeBinding binding = imp.findTypeBinding(qname);
                if (binding != null)
                    return binding;
            }
        }

        return null;
    }

    /**
     * find a locally defined type by unqualified name
     */
    public TypeBinding getLocalTypeBinding(final String name)
    {
        return typeDefs.get(name);
    }

    /**
     * Add a dependency between a statement and a binding--i.e., record the fact
     * that the given statement depends on the given binding.
     * <p/>
     * Here we're simply filtering for local definitions before calling the
     * super method (common between modules and lambdas).
     * <p/>
     * NOTE: we only track dependencies whose targets are in our immediate module,
     * NOT in imported modules. This forces all imports to have been typechecked
     * before us, which precludes cycles in imports under current pipeline.
     * TODO either 1) no cycles, 2) split pipeline + annotations in cycles,
     * 3) cross-module typechecking. (2) looks like the winner currently
     */
    public void addDependency(final Statement statement, final Binding binding)
    {
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
