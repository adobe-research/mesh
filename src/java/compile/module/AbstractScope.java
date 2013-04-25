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

import com.google.common.collect.Sets;
import compile.Loc;
import compile.TopoSort;
import compile.term.*;

import java.util.*;

/**
 * Parts of {@link Scope} common to both
 * {@link compile.module.Module} and {@link LambdaTerm}.
 *
 * @author Basil Hosmer
 */
public abstract class AbstractScope implements Scope
{
    protected final Loc loc;

    protected final List<Statement> body;

    protected final LinkedHashMap<String, LetBinding> lets;

    protected final LinkedHashMap<String, TypeDef> typeDefs;

    protected final Map<Statement, Set<Binding>> dependencies;
    protected List<List<Statement>> groups;

    public AbstractScope(final Loc loc, final List<Statement> body)
    {
        this.loc = loc;
        this.body = body != null ? body : Collections.<Statement>emptyList();
        this.lets = new LinkedHashMap<String, LetBinding>();
        this.typeDefs = new LinkedHashMap<String, TypeDef>();
        this.dependencies = new IdentityHashMap<Statement, Set<Binding>>();
    }

    // Scope

    public final List<Statement> getBody()
    {
        return body;
    }

    public final LinkedHashMap<String, LetBinding> getLets()
    {
        return lets;
    }

    public void addLet(final LetBinding let)
    {
        let.setScope(this);
        lets.put(let.getName(), let);
    }

    public final Map<String, TypeDef> getTypeDefs()
    {
        return typeDefs;
    }

    public final void addTypeDef(final TypeDef typeDef)
    {
        typeDef.setScope(this);
        typeDefs.put(typeDef.getName(), typeDef);
    }

    public final List<List<Statement>> getDependencyGroups()
    {
        // calculate groups on first request
        if (groups == null)
            commitDependencies();

        return groups;
    }

    private void commitDependencies()
    {
        // make sure map contains all body statements as entries
        ensureDependencies(body);
        ensureDependencies(typeDefs.values());

        // topo sort statements into connected components
        groups = TopoSort.componentSort(dependencies);
    }

    public void addDependency(final Statement statement, final Binding binding)
    {
        // invalidate previously calculated dependency groups
        groups = null;

        assert body.contains(statement) ||
            (statement instanceof TypeDef && typeDefs.containsValue(statement)) :
            "source statement not from this scope";

        assert binding.getScope() == this :
            "target binding not from this scope";

        Set<Binding> targets = dependencies.get(statement);

        if (targets == null)
        {
            targets = Sets.newIdentityHashSet();
            dependencies.put(statement, targets);
        }

        targets.add(binding);
    }

    /**
     * helper--make sure passed statements are in domain of
     * dependency map
     */
    private void ensureDependencies(final Collection<? extends Statement> statements)
    {
        for (final Statement statement : statements)
            if (!dependencies.containsKey(statement))
                dependencies.put(statement, Collections.<Binding>emptySet());
    }

    // Located

    public final Loc getLoc()
    {
        return loc;
    }
}
