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
package compile;

import java.util.*;

/**
 * Topological sort
 *
 * @author Basil Hosmer
 */
public final class TopoSort
{
    /**
     * Begun from
     * <pre>http://www.logarithmic.net/pfh-files/blog/01208083168/sort.py</pre>
     * Given a dependency map, returns a list of sets, where items in list[n]
     * depend only on items from lists[<=n]. (Cycles create sets with more
     * than 1 member.)
     *
     * @param graph    dependency graph, as a map from children (dependency sources)
     *                 for collections of parents (dependency targets).
     * @param <Child>  dependency source type. May include supertypes that are not target types.
     *                 This permits use for e.g. dependencies from statements to bindings, where
     *                 all bindings are statements but not vice versa.
     *                 <p/>
     * @param <Parent> dependency target type
     * @return list of list of objects, where lists are in dependency order w.r.t. each other, and object
     *         in each list are interdependent.
     */
    public static <Child, Parent extends Child>
    List<List<Child>> componentSort(final Map<Child, ? extends Collection<Parent>> graph)
    {
        // get list of strongly-connected components
        final List<List<Child>> components = new SCCFinder<Child, Parent>(graph).run();

        // create node->component map
        final Map<Child, List<Child>> nodeComponents =
            new IdentityHashMap<Child, List<Child>>();

        for (final List<Child> component : components)
            for (final Child node : component)
                nodeComponents.put(node, component);

        // create dependency map among components
        final Map<List<Child>, List<List<Child>>> componentDeps =
            new IdentityHashMap<List<Child>, List<List<Child>>>();

        for (final List<Child> component : components)
            componentDeps.put(component, new ArrayList<List<Child>>());

        // for each original node->node dependency, add a corresponding
        // component->component dependency (if the two components are different)
        for (final Child node : graph.keySet())
        {
            final List<Child> nodeComponent = nodeComponents.get(node);

            for (final Child parent : graph.get(node))
            {
                final List<Child> parentComponent = nodeComponents.get(parent);

                if (nodeComponent != parentComponent)
                    componentDeps.get(nodeComponent).add(parentComponent);
            }
        }

        // sort component dependency map
        return sort(componentDeps);
    }

    /**
     * regular topo sort, no cycle handling (or checking)
     */
    public static <Child, Parent extends Child>
    List<Child> sort(final Map<Child, ? extends Collection<Parent>> graph)
    {
        final Map<Child, Integer> childCounts = countChildren(graph);

        final List<Child> childless = initChildless(graph, childCounts);

        // add each childless node to result list and decrement
        // child count of its parents, adding any newly childless
        // parents to childless list. repeat until done.
        //
        final List<Child> result = new ArrayList<Child>();
        while (!childless.isEmpty())
        {
            final Child child = childless.get(childless.size() - 1);
            childless.remove(childless.size() - 1);

            result.add(0, child);

            for (final Parent parent : graph.get(child))
            {
                final int newChildCount = childCounts.get(parent) - 1;

                childCounts.put(parent, newChildCount);

                if (newChildCount == 0)
                    childless.add(parent);
            }
        }

        return result;
    }

    /**
     * given a dependency graph, return a map of child counts for the children
     * (dependency sources)--i.e. a map recording the number of times each child
     * appears as a parent of other children.
     */
    private static <Child, Parent extends Child>
    Map<Child, Integer> countChildren(final Map<Child, ? extends Collection<Parent>> graph)
    {
        final Map<Child, Integer> counts = new IdentityHashMap<Child, Integer>();

        for (final Child node : graph.keySet())
            counts.put(node, 0);

        for (final Child node : graph.keySet())
            for (final Child parent : graph.get(node))
                counts.put(parent, counts.get(parent) + 1);

        return counts;
    }

    /**
     * for a given dependency graph and child counts, return a list of
     * the children (dependency sources) whose child count is zero.
     */
    private static <Child, Parent extends Child>
    List<Child> initChildless(final Map<Child, ? extends Collection<Parent>> graph,
                              final Map<Child, Integer> kidCounts)
    {
        final List<Child> childless = new ArrayList<Child>();

        for (final Child node : graph.keySet())
            if (kidCounts.get(node) == 0)
                childless.add(node);

        return childless;
    }
}