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
 * Port of
 * <pre>http://www.logarithmic.net/pfh-files/blog/01208083168/sort.py</pre>
 * Find the strongly-connected components in a graph using Tarjan's algorithm.
 * Graph is a map from children/dependency sources to collections of
 * parents/dependency targets.
 * NOTE: single-use, not threadsafe
 *
 * @author Basil Hosmer
 */
public final class SCCFinder<Child, Parent extends Child>
{
    // input
    private final Map<Child, ? extends Collection<Parent>> graph;

    // intermediates
    private final Stack<Child> stack;
    private final Map<Child, Integer> visited;

    // output
    private final List<List<Child>> components;

    /**
     * @param graph map from children to lists of parents
     */
    public SCCFinder(final Map<Child, ? extends Collection<Parent>> graph)
    {
        this.graph = graph;
        this.stack = new Stack<Child>();
        this.visited = new IdentityHashMap<Child, Integer>();
        this.components = new ArrayList<List<Child>>();
    }

    /**
     * Scan each child, accumulating strongly-connected components
     * in {@link #components} as a side effect. Then return components.
     */
    public List<List<Child>> run()
    {
        for (final Child child : graph.keySet())
            visit(child);

        return components;
    }

    private void visit(final Child child)
    {
        // only visit each child once.
        // if we've been visited, we're on the stack.
        if (visited.containsKey(child))
            return;

        // save current stack size and push us onto stack
        final int stackSize = stack.size();
        stack.push(child);

        // initially, visited just records our position
        // in the traversal order.
        final int index = visited.size();
        visited.put(child, index);

        // now, scan our parents.
        if (graph.get(child) == null)
            assert false;
        
        for (final Parent parent : graph.get(child))
        {
            // after this, parent's visited index will be the minimum index
            // of itself and any of its parents
            visit(parent);

            // in effect this makes sure that our visited index is
            // that of the first node encountered in a cycle.
            final int parentIndex = visited.get(parent);
            if (parentIndex < index)
                visited.put(child, parentIndex);
        }

        // if we are the first-visited node in a cycle, the rest of the
        // cycle is still on the stack. collect us into a new SCC, and
        // shift our visited indexes out of the way of subsequent
        // traversals.
        if (index == visited.get(child))
        {
            // collect us and everything above us on the stack into a new scc
            final List<Child> scc = new ArrayList<Child>(stack.subList(stackSize, stack.size()));

            // restore stack state
            while (stack.size() > stackSize)
                stack.pop();

            // add new scc to components
            components.add(new ArrayList<Child>(scc));

            // this ensures that all nodes whose SCCs have already been discovered
            // do not interfere with the level-finding logic above--i.e., if in the
            // future a parent's visited index is lower than a child's, it will always
            // imply a cycle, because any previously-discovered cycles the parent may
            // be participating in, with children visited prior to us, will have prompted
            // this index shift.
            for (final Child item : scc)
                visited.put(item, graph.size());
        }
    }
}