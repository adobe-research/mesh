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
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;
import runtime.tran.Box;
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
public final class Transfer extends IntrinsicLambda
{
    public static final String NAME = "transfer";

    private static final TypeParam A = new TypeParam("A", Kinds.STAR);
    private static final Type BOX_A = Types.app(Types.BOX, A);

    private static final TypeParam B = new TypeParam("B", Kinds.STAR);
    private static final Type BOX_B = Types.app(Types.BOX, B);

    private static final Type FUNC_A_B = Types.fun(A, B);

    private static final Type PARAM_TYPE = Types.tup(BOX_B, FUNC_A_B, BOX_A);

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
        return invoke((Box)args.get(0), (Lambda)args.get(1), (Box)args.get(2));
    }

    public static Tuple invoke(final Box write, final Lambda f, final Box read)
    {
        TransactionManager.transfer(write, f, read);
        return Tuple.UNIT;
    }
}