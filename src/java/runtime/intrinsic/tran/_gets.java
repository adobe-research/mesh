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
import runtime.tran.TransactionManager;

/**
 * Return a tuple of the values held in a tuple of boxes.
 * Atomic, so box reads all take place at the same moment
 * in program time. I.e., this function is equivalent to
 * performing individual gets on each box within a transaction
 * and returning a tuple of the results.
 *
 * @author Basil Hosmer
 */
public final class _gets extends IntrinsicLambda
{
    public static final _gets INSTANCE = new _gets(); 
    public static final String NAME = "gets";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Tuple)arg);
    }

    public static Tuple invoke(final Tuple boxes)
    {
        return TransactionManager.gets(boxes);
    }
}
