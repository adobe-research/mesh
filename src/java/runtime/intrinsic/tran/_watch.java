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
 * Add a watcher to a box, return watcher.
 *
 * @author Basil Hosmer
 */
public final class _watch extends IntrinsicLambda
{
    public static final _watch INSTANCE = new _watch(); 
    public static final String NAME = "watch";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Box)args.get(0), (Lambda)args.get(1));
    }

    public static Lambda invoke(final Box box, final Lambda watcher)
    {
        box.acquireWriteLock();
        box.addWatcher(watcher);
        box.releaseWriteLock();
        return watcher;
    }
}
