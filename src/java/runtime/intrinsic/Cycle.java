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
import runtime.rep.Tuple;

/**
 * iterate on an endofunction while guard predicate returns true.
 * iteration is started with specified initial argument, which is
 * passed through the guard before being applied.
 *
 * @author Basil Hosmer
 */
public final class Cycle extends IntrinsicLambda
{
    public static final String NAME = "cycle";

    private static final Type T = new TypeParam("T");
    private static final Type FUNCTION_T_BOOL = Types.fun(T, Types.BOOL);
    private static final Type FUNCTION_T_T = Types.fun(T, T);
    public static final Type TYPE =
        Types.fun(Types.tup(FUNCTION_T_BOOL, T, FUNCTION_T_T), T);

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
        return invoke((Lambda)args.get(0), args.get(1), (Lambda)args.get(2));
    }

    public static Object invoke(final Lambda pred, final Object initial, final Lambda f)
    {
        Object result = initial;

        while ((Boolean)pred.apply(result))
            result = f.apply(result);

        return result;
    }
}