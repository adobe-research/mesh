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
import runtime.rep.lambda.Lambda;
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
public final class Update extends IntrinsicLambda
{
    public static final String NAME = "update";

    private static final TypeParam T = new TypeParam("T");
    private static final Type BOX_T = Types.box(T);
    private static final Type ENDO_T = Types.fun(T, T);
    private static final Type PARAM_TYPE = Types.tup(BOX_T, ENDO_T);
    public static final Type TYPE = Types.fun(PARAM_TYPE, Types.unit());

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
        final Tuple args = (Tuple)arg;
        return invoke((Box)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Box box, final Lambda f)
    {
        return TransactionManager.update(box, f);
    }
}