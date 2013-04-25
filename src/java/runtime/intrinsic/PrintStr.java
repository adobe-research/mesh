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
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * print string to System.out.
 *
 * @author Basil Hosmer
 */
public final class PrintStr extends IntrinsicLambda
{
    public static String NAME = "printstr";

    public static final Type TYPE = Types.fun(Types.STRING, Types.unit());

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
        return invoke((String)arg);
    }

    public static Tuple invoke(final String obj)
    {
        if (obj != null)
            System.out.println(obj);

        return Tuple.UNIT;
    }
}