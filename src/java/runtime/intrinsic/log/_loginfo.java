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

import runtime.intrinsic._tostr;
import runtime.Logging;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * loginfo(x) == Logging.info({@link _tostr tostr}(x))
 *
 * @author Brent Baker
 */
public final class _loginfo extends IntrinsicLambda
{
    public static final _loginfo INSTANCE = new _loginfo(); 
    public static String NAME = "loginfo";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke(arg);
    }

    public static Tuple invoke(final Object obj)
    {
        // Need to escape the {} in the log message as we want them treated as strings
        // not as FormatElements for the MessageFormat.
        Logging.info(_tostr.invoke(obj).replaceAll("[{]", "'{'").replaceAll("[{]", "'}'"));
        return Tuple.UNIT;
    }
}
