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
 * temp: throw with message if argument is false
 * TODO variant
 *
 * @author Basil Hosmer
 */
public final class _assert extends IntrinsicLambda
{
    public static final _assert INSTANCE = new _assert(); 
    public static final String NAME = "assert";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Boolean)args.get(0), (String)args.get(1));
    }

    public static Tuple invoke(final boolean arg, final String msg)
    {
        if (!arg)
        {
            throw new AssertionError(msg);
        }

        return Tuple.UNIT;
    }
}
