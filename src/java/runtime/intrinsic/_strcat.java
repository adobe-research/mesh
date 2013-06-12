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
import runtime.rep.list.ListValue;

/**
 * concatenate strings
 *
 * @author Basil Hosmer
 */
public final class _strcat extends IntrinsicLambda
{
    public static final _strcat INSTANCE = new _strcat(); 
    public static final String NAME = "strcat";

    public String getName()
    {
        return NAME;
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
