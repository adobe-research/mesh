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

import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;

import java.util.Set;

/**
 * Change notification event, dispatched over the set of watchers attached
 * to a box at the time of a successful commit.
 *
 * @author Basil Hosmer
 */
final class ChangeEvent
{
    final Set<Object> watchers;
    final Tuple arg;

    ChangeEvent(final Set<Object> watchers, final Object oldval, final Object newval)
    {
        assert watchers != null;
        assert !watchers.isEmpty();     // performance leak only

        this.watchers = watchers;
        this.arg = Tuple.from(new Object[]{oldval, newval});
    }

    /**
     * Invoke {@link #watchers} on (oldval, newval)
     */
    void fire()
    {
        for (final Object watcher : watchers)
            ((Lambda)watcher).apply(arg);
    }
}
