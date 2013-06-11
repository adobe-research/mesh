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

import runtime.rep.Tuple;

/**
 * strcmp
 *
 * @author Basil Hosmer
 */
public final class _strcmp extends IntrinsicLambda
{
    public static final _strcmp INSTANCE = new _strcmp(); 
    public static final String NAME = "strcmp";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((String)args.get(0), (String)args.get(1));
    }

    public static int invoke(final String s, final String t)
    {
        return s.compareTo(t);
    }
}
