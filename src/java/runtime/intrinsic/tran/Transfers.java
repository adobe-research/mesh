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
import runtime.tran.TransactionManager;

/**
 * Takes a tuple of input boxes, a function and a tuple of output boxes.
 * Within a transaction, runs the function on the input boxes and writes
 * the result to the output boxes. If the function doesn't attempt any box
 * operations requiring relationships outside those already established for
 * reading arguments and writing the result, then it is guaranteed to run
 * without retrying.
 *
 * @author Basil Hosmer
 */
public final class Transfers extends IntrinsicLambda
{
    public static final String NAME = "transfers";

    private static final TypeParam INS = new TypeParam("Ins", Kinds.STAR_LIST);
    private static final Type TUP_INS = Types.app(Types.TUP, INS);
    private static final Type BOX_EACH_INS = Types.tmap(INS, Types.BOX);
    private static final Type TUP_IN_BOXES = Types.app(Types.TUP, BOX_EACH_INS);

    private static final TypeParam OUTS = new TypeParam("Outs", Kinds.STAR_LIST);
    private static final Type TUP_OUTS = Types.app(Types.TUP, OUTS);
    private static final Type BOX_EACH_OUTS = Types.tmap(OUTS, Types.BOX);
    private static final Type TUP_OUT_BOXES = Types.app(Types.TUP, BOX_EACH_OUTS);

    private static final Type ASSIGN_FUNC = Types.fun(TUP_INS, TUP_OUTS);

    private static final Type PARAM_TYPE =
        Types.tup(TUP_OUT_BOXES, ASSIGN_FUNC, TUP_IN_BOXES);

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
        return invoke((Tuple)args.get(0), (Lambda)args.get(1), (Tuple)args.get(2));
    }

    public static Tuple invoke(
        final Tuple writes, final Lambda f, final Tuple reads)
    {
        TransactionManager.transfers(writes, f, reads);
        return Tuple.UNIT;
    }
}