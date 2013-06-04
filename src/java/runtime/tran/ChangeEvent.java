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
    final Object arg[];

    ChangeEvent(final PersistentMap watchers,
        final Object oldval, final Object newval)
    {
        assert watchers != null;
        assert !watchers.isEmpty();     // performance leak only

        this.watchers = watchers;
        this.arg = new Object[] { oldval, newval };
    }

    /**
     * Invoke {@link #watchers} on (oldval, newval)
     */
    void fire()
    {
        for (final Map.Entry<Object,Object> watcher : watchers.entrySet())
        {
            final Tuple tuple =
                Tuple.from(arg[0], arg[1], watcher.getValue());
            ((Lambda)watcher.getKey()).apply(tuple);
        }
    }
}
