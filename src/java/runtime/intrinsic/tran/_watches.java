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

import runtime.intrinsic.IntrinsicLambda;
import runtime.rep.Lambda;
import runtime.rep.Tuple;
import runtime.tran.Boxes;
import runtime.tran.Watcher;

/**
 * Add a watcher to a tuple of boxes, return watcher.
 *
 * @author Keith McGuigan
 */
public final class _watches extends IntrinsicLambda
{
    public static final _watches INSTANCE = new _watches();
    public static final String NAME = "watches";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Tuple)args.get(0), (Lambda)args.get(1));
    }

    public static Lambda invoke(final Tuple boxes, final Lambda action)
    {
        final Watcher watcher = new Watcher(Boxes.from(boxes), action);
        watcher.start();
        return action;
    }
}
