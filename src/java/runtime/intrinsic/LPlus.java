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
import runtime.rep.list.ChainedListPair;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;

/**
 * long plus
 *
 * @author Basil Hosmer
 */
public final class LPlus extends IntrinsicLambda
{
    public static final String NAME = "lplus";

    private static final TypeParam T = new TypeParam("T");

    private static final Type LIST_T = Types.list(T);

    public static final Type TYPE = Types.fun(Types.tup(LIST_T, LIST_T), LIST_T);

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
        return invoke((ListValue)args.get(0), (ListValue)args.get(1));
    }

    public static ListValue invoke(final ListValue llist, final ListValue rlist)
    {
        return ChainedListPair.create(llist, rlist);
    }
}