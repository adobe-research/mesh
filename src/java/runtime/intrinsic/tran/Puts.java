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
 * Tuplized version of put().
 *
 * @author Basil Hosmer
 */
public final class Puts extends IntrinsicLambda
{
    public static final String NAME = "puts";

    private static final TypeParam VALS = new TypeParam("T", Kinds.STAR_LIST);
    private static final Type BOX_EACH_VALS = Types.tmap(VALS, Types.BOX);
    private static final Type TUP_VAL_BOXES = Types.app(Types.TUP, BOX_EACH_VALS);
    private static final Type TUP_VALS = Types.app(Types.TUP, VALS);
    private static final Type PARAM_TYPE = Types.tup(TUP_VAL_BOXES, TUP_VALS);

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
        return invoke((Tuple)args.get(0), (Tuple)args.get(1));
    }

    public static Tuple invoke(final Tuple boxes, final Tuple vals)
    {
        TransactionManager.puts(boxes, vals);
        return Tuple.UNIT;
    }
}