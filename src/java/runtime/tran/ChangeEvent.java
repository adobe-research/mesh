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
package runtime.tran;

import runtime.rep.Tuple;
import runtime.rep.lambda.Lambda;
import runtime.rep.map.PersistentMap;

import java.util.Map;

/**
 * Change notification event, dispatched over the set of watchers attached
 * to a box at the time of a successful commit.
 *
 * @author Basil Hosmer
 */
final class ChangeEvent
{
    final PersistentMap watchers;
    final Tuple valpair;

    ChangeEvent(final PersistentMap watchers,
        final Object oldval, final Object newval)
    {
        assert watchers != null;
        assert !watchers.isEmpty();     // performance leak only

        this.watchers = watchers;
        this.valpair = Tuple.from(oldval, newval);
    }

    /**
     * Apply each watcher function in {@link #watchers} to either
     * (oldval, newval) or ((oldval, newval), cargo), depending on
     * whether the watcher is associated with a non-null caargo value.
     * NOTE: currently we take non-null cargo to mean that lambda is
     * of type ((V, V), C) -> X, rather than the standard (V, V) -> X.
     * Currently this capability is used only by MultiWaiter.
     * TODO consider surfacing this as part of the watcher API
     */
    void fire()
    {
        for (final Map.Entry<Object, Object> entry : watchers.entrySet())
        {
            final Lambda watcher = (Lambda)entry.getKey();
            final Object cargo = entry.getValue();

            watcher.apply(cargo != null ? Tuple.from(valpair, cargo) : valpair);
        }
    }
}
