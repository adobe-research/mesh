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
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;

/**
 * returns index of first occurence of item in list, or list size
 *
 * @author Basil Hosmer
 */
public final class Find extends IntrinsicLambda
{
    public static final String NAME = "find";

    private static final TypeParam T = new TypeParam("T");
    private static final Type LIST_T = Types.list(T);

    public static final Type TYPE = Types.fun(Types.tup(LIST_T, T), Types.INT);

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
        return invoke((ListValue)args.get(0), args.get(1));
    }

    public static int invoke(final ListValue list, final Object val)
    {
        return list.find(val);
    }
}