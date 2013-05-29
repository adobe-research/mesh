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

import runtime.rep.list.IntRange;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;

/**
 * Create int list [start, ..., start + extent)
 *
 * @author Basil Hosmer
 */
public final class _range extends IntrinsicLambda
{
    public static final _range INSTANCE = new _range(); 
    public static final String NAME = "range";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (Integer)args.get(1));
    }

    public static ListValue invoke(final int start, final int extent)
    {
        return IntRange.create(start, start + (extent >= 0 ? extent : -extent));
    }
}
