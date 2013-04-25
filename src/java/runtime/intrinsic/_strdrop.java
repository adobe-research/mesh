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
 * strdrop(str, n) = drops first n if n > 0, last -n if n < 0. n > list size is held to size
 * TODO should be replaced by take over list TC, which string implements
 *
 * @author Basil Hosmer
 */
public final class _strdrop extends IntrinsicLambda
{
    public static final _strdrop INSTANCE = new _strdrop(); 
    public static final String NAME = "strdrop";

    public String getName()
    {
        return NAME;
    }

    public final String apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (String)args.get(1));
    }

    public static String invoke(final int n, final String s)
    {
        final int size = s.length();
        return n >= 0 ? s.substring(Math.min(n, size)) :
            s.substring(0, Math.max(0, size + n));
    }
}
