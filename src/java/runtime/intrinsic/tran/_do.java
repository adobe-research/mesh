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
import runtime.tran.TransactionManager;

/**
 * Run a block in a transaction.
 *
 * @author Basil Hosmer
 */
public final class _do extends IntrinsicLambda
{
    public static final _do INSTANCE = new _do(); 
    public static final String NAME = "do";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Lambda)arg);
    }

    public static Object invoke(final Lambda f)
    {
        return TransactionManager.apply(f);
    }
}
