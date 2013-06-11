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
import runtime.sys.Logging;
import runtime.intrinsic.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * logdebug(x) == Logging.debug({@link _tostr tostr}(x))
 *
 * @author Brent Baker
 */
public final class _logdebug extends IntrinsicLambda
{
    public static final _logdebug INSTANCE = new _logdebug(); 
    public static String NAME = "logdebug";

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
        Logging.debug(_tostr.invoke(obj).replaceAll("[{]", "'{'").replaceAll("[{]", "'}'"));
        return Tuple.UNIT;
    }
}
