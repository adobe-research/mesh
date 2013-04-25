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
import compile.type.*;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;

/**
 * concatenate strings
 *
 * @author Basil Hosmer
 */
public final class StrCat extends IntrinsicLambda
{
    public static final String NAME = "strcat";

    private static final Type LIST_STR = Types.list(Types.STRING);

    public static final Type TYPE = Types.fun(LIST_STR, Types.STRING);

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
        return invoke((ListValue)arg);
    }

    public static String invoke(final ListValue strings)
    {
        return Joiner.on("").join(strings);
    }
}