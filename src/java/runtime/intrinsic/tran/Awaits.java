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
import compile.type.kind.Kinds;
import runtime.rep.Tuple;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.tran.MultiWaiter;
import runtime.tran.TransactionManager;

/**
 * Transactional wait/notify against a tuple of boxes.
 * wait(boxes, pred) puts current thread into wait state
 * until/unless pred(gets(boxes)) returns true. pred() is
 * called each time a value is committed to any box.
 *
 * @author Basil Hosmer
 */
public final class Awaits extends IntrinsicLambda
{
    public static final String NAME = "awaits";

    private static final TypeParam VALS = new TypeParam("T", Kinds.STAR_LIST);
    private static final Type BOX_EACH_VALS = Types.tmap(VALS, Types.BOX);
    private static final Type TUP_VAL_BOXES = Types.app(Types.TUP, BOX_EACH_VALS);
    private static final Type TUP_VALS = Types.app(Types.TUP, VALS);
    private static final Type VALS_PRED = Types.fun(TUP_VALS, Types.BOOL);
    private static final Type PARAM_TYPE = Types.tup(TUP_VAL_BOXES, VALS_PRED);

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
        return invoke((Tuple)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Tuple boxes, final Lambda pred)
    {
        if (TransactionManager.getTransaction() == null)
            new MultiWaiter(boxes).wait(pred);

        return Tuple.UNIT;
    }
}