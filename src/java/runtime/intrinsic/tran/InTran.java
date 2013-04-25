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

import compile.type.Type;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.tran.TransactionManager;

/**
 * Return rrue if we're in a transaction.
 * TODO remove, need sufficient control over nestability.
 *
 * @author Basil Hosmer
 */
public final class InTran extends IntrinsicLambda
{
    public static final String NAME = "intran";

    public static final Type TYPE = Types.fun(Types.unit(), Types.BOOL);

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
    }

    public Object apply(final Object arg)
    {
        return invoke();
    }

    public static boolean invoke()
    {
        return TransactionManager.inTransaction();
    }
}