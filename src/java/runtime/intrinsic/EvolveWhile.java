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
package runtime.intrinsic;

import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;

/**
 * Evolve (reduce) until stop condition is met, or inputs
 * are exhausted.
 *
 * @author Basil Hosmer
 */
public final class EvolveWhile extends IntrinsicLambda
{
    public static final String NAME = "evolve_while";

    private static final Type A = new TypeParam("A");
    private static final Type B = new TypeParam("B");
    private static final Type PRED_A = Types.fun(A, Types.BOOL);
    private static final Type FUNC_AB_A = Types.fun(Types.tup(A, B), A);
    private static final Type LIST_B = Types.list(B);

    public static final Type TYPE =
        Types.fun(Types.tup(PRED_A, A, FUNC_AB_A, LIST_B), A);

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
        return invoke(
            (Lambda)args.get(0),
            args.get(1),
            (Lambda)args.get(2),
            (ListValue)args.get(3));
    }

    public static Object invoke(
        final Lambda pred,
        final Object init,
        final Lambda f,
        final ListValue list)
    {
        Object result = init;

        for (final Object item : list)
        {
            if (!(Boolean)pred.apply(result))
                break;

            result = f.apply(Tuple.from(result, item));
        }

        return result;
    }
}