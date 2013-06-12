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
import runtime.tran.Box;
import runtime.tran.TransactionManager;

/**
 * Own a box. Valid only in a transaction. returns box value.
 *
 * @author Basil Hosmer
 */
public final class _own extends IntrinsicLambda
{
    public static final _own INSTANCE = new _own(); 
    public static final String NAME = "own";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Box)arg);
    }

    public static Object invoke(final Box box)
    {
        return TransactionManager.own(box);
    }
}
