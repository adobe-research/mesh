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
 * conditional value selection (immediate if)
 *
 * @author Basil Hosmer
 */
public final class _iif extends IntrinsicLambda
{
    public static final _iif INSTANCE = new _iif(); 
    public static final String NAME = "iif";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Boolean)args.get(0), args.get(1), args.get(2));
    }

    public static Object invoke(final boolean cond, final Object t, final Object f)
    {
        return cond ? t : f;
    }
}
