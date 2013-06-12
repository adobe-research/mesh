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

import runtime.rep.Lambda;
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
    final Box[] boxes;
    final Object[] values;

    ChangeEvent(final Box[] boxes, final Object[] values, final int count)
    {
        // Unfortunately we do need to copy these here since we've been
        // passed references to the transaction's internal arrays and it
        // will reuse them.
        this.boxes = new Box[count];
        this.values = new Object[count];
        System.arraycopy(boxes, 0, this.boxes, 0, count);
        System.arraycopy(values, 0, this.values, 0, count);
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
        final Set<Lambda> calls = new HashSet<Lambda>();
        for (final Box b : boxes)
        {
            final PersistentMap pm = b.getWatchers();
            if (pm != null)
                for (final Map.Entry<Object,Object> obj : pm.entrySet())
                {
                    if (!calls.contains(obj.getKey()))
                    {
                        calls.add((Lambda)obj.getKey());
                        watchers.add((Watcher)obj.getValue());
                    }
                }
        }

        for (final Watcher watcher : watchers)
            watcher.trigger(boxes, values);
    }
}
