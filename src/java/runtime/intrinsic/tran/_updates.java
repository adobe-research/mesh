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
import runtime.tran.TransactionManager;

/**
 * Tuplized version of update.
 * Update a tuple of boxes with the result of a function
 * that takes tuple of values as input, returns new tuple
 * as output.
 *
 * @author Basil Hosmer
 */
public final class _updates extends IntrinsicLambda
{
    public static final _updates INSTANCE = new _updates(); 
    public static final String NAME = "updates";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Tuple)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Tuple boxes, final Lambda f)
    {
        TransactionManager.updates(boxes, f);
        return Tuple.UNIT;
    }
}
