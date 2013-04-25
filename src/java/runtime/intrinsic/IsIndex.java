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
 * true if integer argument is an index value for list argument
 *
 * @author Basil Hosmer
 */
public final class IsIndex extends IntrinsicLambda
{
    public static final String NAME = "isindex";

    public static final Type LIST_T = Types.list(new TypeParam("T"));

    public static final Type TYPE = Types.fun(
        Types.tup(Types.INT, LIST_T),
        Types.BOOL);

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
    }

    public final Boolean apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (ListValue)args.get(1));
    }

    public static boolean invoke(final int index, final ListValue list)
    {
        return index >= 0 && index < list.size();
    }
}