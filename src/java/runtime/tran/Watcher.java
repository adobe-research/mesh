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

import java.util.Map;

/**
 * Instances of this are attached to box(es) and react when the value has been
 * changed in a transaction.
 *
 * @author Keith McGuigan
 */
public class Watcher
{
    protected final Boxes boxes;
    protected final Lambda action;
    protected Object stash; // either a scalar value or a tuple

    public Watcher(final Boxes boxes, final Lambda action)
    {
        this.boxes = boxes;
        this.action = action;
        this.stash = null;
    }

    public synchronized void start()
    {
        boxes.acquireWriteLocks();

        stash = boxes.getValues();
        boxes.addWatcher(this);

        boxes.releaseWriteLocks();
    }

    public Lambda getAction()
    {
        return action;
    }

    /**
     * Apply the specified updates to the current value(s) and return a tuple
     * of ((oldvalue+), (newValue+)).  The existing stash is modified to
     * contain the new value(s).
     */
    public synchronized Tuple applyUpdates(final Map<Box,Object> updates)
    {
        final Tuple results = boxes.applyUpdates(stash, updates);
        this.stash = results.get(1);

        return results;
    }

    /**
     * This is called after a transaction commit if that transaction updated
     * any of the boxes that we're interested in.
     */
    public void trigger(final Map<Box,Object> updates)
    {
        final Tuple args = applyUpdates(updates);
        commitAction(args);
    }

    // What actually happens after a transaction which has changed values we've
    // registered interest in.  This is overridden by Waiter to implement
    // different behavior (notification rather than direct calling)
    protected void commitAction(final Tuple args)
    {
        action.apply(args);
    }
}
