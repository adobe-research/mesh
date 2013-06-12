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
import runtime.rep.Tuple;
import runtime.tran.Box;
import runtime.tran.TransactionManager;

/**
 * Set a box's value. Wraps itself in a transaction if none is running.
 *
 * @author Basil Hosmer
 */
public final class _put extends IntrinsicLambda
{
    public static final _put INSTANCE = new _put(); 
    public static final String NAME = "put";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Box)args.get(0), args.get(1));
    }

    public static Tuple invoke(final Box box, final Object val)
    {
        return TransactionManager.put(box, val);
    }
}
