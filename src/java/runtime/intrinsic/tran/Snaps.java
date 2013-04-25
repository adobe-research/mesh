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
public final class Snaps extends IntrinsicLambda
{
    public static final String NAME = "snaps";

    private static final TypeParam VALS = new TypeParam("Ts", Kinds.STAR_LIST);
    private static final Type BOX_EACH_VALS = Types.tmap(VALS, Types.BOX);
    private static final Type TUP_VAL_BOXES = Types.app(Types.TUP, BOX_EACH_VALS);
    private static final Type TUP_VALS = Types.app(Types.TUP, VALS);

    public static final Type TYPE = Types.fun(TUP_VAL_BOXES, TUP_VALS);

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
        return invoke((Tuple)arg);
    }

    public static Tuple invoke(final Tuple boxes)
    {
        return TransactionManager.snaps(boxes);
    }
}