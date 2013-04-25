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
import compile.type.TypeParam;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.tran.Box;
import runtime.tran.TransactionManager;

/**
 * Own a box. Valid only in a transaction. returns box value.
 *
 * @author Basil Hosmer
 */
public final class Own extends IntrinsicLambda
{
    public static final String NAME = "own";

    private static final TypeParam T = new TypeParam("T");
    private static final Type BOX_T = Types.box(T);
    public static final Type TYPE = Types.fun(BOX_T, T);

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
        return invoke((Box)arg);
    }

    public static Object invoke(final Box box)
    {
        return TransactionManager.own(box);
    }
}