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
import runtime.rep.Tuple;

/**
 * join string list using separator
 *
 * @author Basil Hosmer
 */
public final class _strjoin extends IntrinsicLambda
{
    public static final _strjoin INSTANCE = new _strjoin(); 
    public static final String NAME = "strjoin";

    public String getName()
    {
        return NAME;
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
