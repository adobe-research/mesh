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
import runtime.rep.Tuple;
import runtime.tran.TransactionManager;

/**
 * Tuplized version of own()
 *
 * @author Basil Hosmer
 */
public final class Owns extends IntrinsicLambda
{
    public static final String NAME = "owns";

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
        return TransactionManager.owns(boxes);
    }
}