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

import compile.type.*;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;
import runtime.tran.Box;
import runtime.tran.TransactionManager;
import runtime.tran.Waiter;

/**
 * Transactional wait/notify.
 * wait(box, pred) puts current thread into wait state
 * until/unless pred(get(box)) returns true. pred() is
 * called each time a value is committed to box.
 *
 * @author Basil Hosmer
 */
public final class Await extends IntrinsicLambda
{
    public static final String NAME = "await";

    private static final TypeParam T = new TypeParam("T");
    private static final Type BOX_T = Types.box(T);
    private static Type FUNC_T_BOOL = Types.fun(T, Types.BOOL);

    public static final Type TYPE = Types.fun(
        Types.tup(BOX_T, FUNC_T_BOOL),
        Types.unit());

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

    public static Tuple invoke(final Box box, final Lambda pred)
    {
        if (TransactionManager.getTransaction() == null)
            new Waiter(box).await(pred);

        return Tuple.UNIT;
    }
}