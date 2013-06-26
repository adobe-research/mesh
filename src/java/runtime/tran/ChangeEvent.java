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

/**
 * Change event, dispatched over the set of reactors attached
 * to a box at the time of a successful commit.
 *
 * @author Basil Hosmer
 */
final class ChangeEvent
{
    final PersistentMap reactors;
    final Object val;

    ChangeEvent(final PersistentMap reactors, final Object val)
    {
        this.reactors = reactors;
        this.val = val;
    }

    /**
     * Apply {@link #reactors} to {@link #val}
     */
    void fire()
    {
        for (final Object reactor : reactors.keySet())
        {
            ((Lambda)reactor).apply(val);
        }
    }
}
