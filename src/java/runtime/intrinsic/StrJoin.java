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

import com.google.common.base.Joiner;
import compile.type.Type;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;

/**
 * join string list using separator
 *
 * @author Basil Hosmer
 */
public final class StrJoin extends IntrinsicLambda
{
    public static final String NAME = "strjoin";

    private static final Type LIST_STR = Types.list(Types.STRING);

    public static final Type TYPE =
        Types.fun(Types.tup(LIST_STR, Types.STRING), Types.STRING);

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
        return invoke((ListValue)args.get(0), (String)args.get(1));
    }

    public static String invoke(final ListValue strings, final String sep)
    {
        return Joiner.on(sep).join(strings);
    }
}