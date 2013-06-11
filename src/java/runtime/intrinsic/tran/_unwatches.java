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
package runtime.intrinsic.tran;

import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;
import runtime.tran.Box;

/**
 * Remove a multi-watcher function from a box.
 * Function equality is identity, so you need to pass
 * the watcher function itself.
 *
 * Note that the first argument, a tuple of boxes, does not need to match the
 * tuple that was originally passed to {@link _watches}.   If a subset is
 * passed, only the passed values will have the watch removed.  Any extraneous
 * boxes in the first argument are ignored.
 *
 * @author Keith McGuigan
 */
public final class _unwatches extends IntrinsicLambda
{
    public static final _unwatches INSTANCE = new _unwatches();
    public static final String NAME = "unwatches";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Tuple)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Tuple boxes, final Lambda watcher)
    {
        for (int i = 0; i < boxes.size(); ++i)
        {
            final Box box = (Box)boxes.get(i);
            box.acquireWriteLock();
            box.removeWatcher(watcher);
            box.releaseWriteLock();
        }
        return boxes;
    }
}
