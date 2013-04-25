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
package compile.analyze;

import com.google.common.collect.Sets;
import compile.Loc;
import compile.Session;
import compile.module.Module;
import compile.module.Scope;
import compile.term.*;

import java.util.*;

/**
 * Calculates reachability data used by {@link RefChecker}.
 * For each let binding L in a module, the {@link #calc} method
 * picks the latest in-module let (i.e., the let whose definition
 * occurs last in the module's body) from the closure of lets reachable
 * from the RHS of L by reference (considering L as reachable in 0 steps).
 * Return a map from let bindings to these locations.
 *
 * @author Basil Hosmer
 */
public final class LastReached extends ModuleVisitor<Object>
{
    private Loc lastLoc;
    
    private Map<LetBinding, Loc> lastReached;

    private final Set<RefTerm> visitedRefs;

    public LastReached(final Module module)
    {
        super(module);
        visitedRefs = Sets.newIdentityHashSet();
    }

    /**
     *
     */
    public Map<LetBinding, Loc> calc()
    {
        if (Session.isDebug())
            Session.debug(getModule().getLoc(), "Calculating last reached...");

        visitedRefs.clear();

        lastReached = new IdentityHashMap<LetBinding, Loc>();

        process();

        return lastReached;
    }

    // BindingVisitor

    /**
     * if let binding is non-intrinsic and hasn't already been
     * visited, traverse RHS and add resulting last-reached 
     * location to {@link #lastReached}.
     */
    @Override
    public Object visit(final LetBinding let)
    {
        if (let.getLoc() == Loc.INTRINSIC)
            return null;

        if (lastReached.containsKey(let))
            return null;
        
        final Loc prevLoc = lastLoc;

        lastLoc = let.getLoc();
        
        // let is current statement. this traversal will
        // leave an earliest valid location in map
        super.visit(let);

        if (Session.isDebug())
            Session.debug(let.getLoc(), "{0} : {1}",
                let.getName(), lastLoc.toString());

        lastReached.put(let, lastLoc);
        
        lastLoc = prevLoc;
        
        return null;
    }

    // TermVisitor

    /**
     * if we're traversing the RHS of a let, update in-progress
     * last-reached with the last-reached of our referent, if
     * it's later than current loc.
     */
    @Override
    public Object visit(final RefTerm ref)
    {
        // not spidering a let, no need to compute bound
        if (lastLoc == null)
            return null;

        final ValueBinding binding = ref.getBinding();

        // ref is not to a let
        if (!binding.isLet())
            return null;

        final LetBinding let = (LetBinding)binding;

        final Scope scope = let.getScope();

        // let is nonlocal
        if (scope.getModule() != getModule())
            return null;

        // already visited this ref
        if (visitedRefs.contains(ref))
            return null;

        visitedRefs.add(ref);

        // traverse to find find last-reached loc for this let
        visit(let);
        
        final Loc loc = lastReached.get(let);

        // if our earliest valid bound is later than current earliest, update
        if (lastLoc.isBefore(loc))
            lastLoc = loc;

        return null;
    }
}