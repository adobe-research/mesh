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

import runtime.rep.map.PersistentMap;

import java.util.*;

/**
 * Change notification event, dispatched over the set of watchers attached
 * to a box at the time of a successful commit.
 *
 * @author Basil Hosmer
 */
final class ChangeEvent
{
    final Map<Box,Object> updates;

    ChangeEvent(final Box[] boxes, final Object[] values, final int count)
    {
        this.updates = new HashMap<Box,Object>();
        for (int i = 0; i < count; ++i)
            updates.put(boxes[i], values[i]);
    }

    /**
     * Activate each watcher of all the boxes
     */
    void fire()
    {
        // Because a watcher can span over multiple boxes, each of which may
        // have been updated in the same transaction, we first uniquify the
        // list of watchers
        final Set<Watcher> watchers = new HashSet<Watcher>();
        for (final Box b : updates.keySet())
        {
            final PersistentMap pm = b.getWatchers();
            if (pm != null)
                for (final Object obj : pm.keySet())
                    watchers.add((Watcher)obj);
        }

        for (final Watcher watcher : watchers)
            watcher.trigger(updates);
    }
}
