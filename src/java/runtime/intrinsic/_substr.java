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

import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * substr(str, start, len)
 * TODO probably temp
 *
 * @author Basil Hosmer
 */
public final class _substr extends IntrinsicLambda
{
    public static final _substr INSTANCE = new _substr(); 
    public static final String NAME = "substr";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((String)args.get(0), (Integer)args.get(1), (Integer)args.get(2));
    }

    public static String invoke(final String s, final int start, final int len)
    {
        return s.substring(start, start + len);
    }
}
