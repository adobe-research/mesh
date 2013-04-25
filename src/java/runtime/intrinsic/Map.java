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
import runtime.rep.Tuple;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.list.ListValue;

/**
 * functional map: apply a function to a list of arguments,
 * yielding a congruent list of results. Aliased to infix
 * operator '|' in {@link compile.parse.Ops}.
 *
 * @author Basil Hosmer
 */
public final class Map extends IntrinsicLambda
{
    public static final String NAME = "map";

    private static final TypeParam X = new TypeParam("X");
    private static final TypeParam Y = new TypeParam("Y");
    private static final Type LIST_X = Types.list(X);
    private static final Type FUNC_X_Y = Types.fun(X, Y);
    private static final Type LIST_Y = Types.list(Y);

    public static final Type TYPE = Types.fun(Types.tup(LIST_X, FUNC_X_Y), LIST_Y);

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
        return invoke((ListValue)args.get(0), (Lambda)args.get(1));
    }

    public static ListValue invoke(final ListValue args, final Lambda func)
    {
        return args.apply(func);
    }
}