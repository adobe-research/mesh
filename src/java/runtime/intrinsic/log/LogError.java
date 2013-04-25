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
package runtime.intrinsic.log;

import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.intrinsic.ToStr;
import runtime.Logging;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * logerror(x) == Logging.error({@link ToStr tostr}(x))
 *
 * @author Brent Baker
 */
public final class LogError extends IntrinsicLambda
{
    public static String NAME = "logerror";

    private static final TypeParam T = new TypeParam("T");

    public static final Type TYPE = Types.fun(T, Types.unit());

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
        return invoke(arg);
    }

    public static Tuple invoke(final Object obj)
    {
        // Need to escape the {} in the log message as we want them treated as strings
        // not as FormatElements for the MessageFormat.
        Logging.error(ToStr.invoke(obj).replaceAll("[{]", "'{'").replaceAll("[{]", "'}'"));
        return Tuple.UNIT;
    }
}