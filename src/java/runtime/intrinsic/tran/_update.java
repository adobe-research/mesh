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
import runtime.tran.Box;
import runtime.tran.TransactionManager;

/**
 * Update a box with the result of a function that takes box's
 * current value as input.
 * Wraps itself in a transaction if none is currently running.
 *
 * @author Basil Hosmer
 */
public final class _update extends IntrinsicLambda
{
    public static final _update INSTANCE = new _update(); 
    public static final String NAME = "update";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Box)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Box box, final Lambda f)
    {
        return TransactionManager.update(box, f);
    }
}
