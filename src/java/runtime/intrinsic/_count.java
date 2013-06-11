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
import runtime.rep.list.ListValue;

/**
 * count(n) returns a list of ints [0, ..., n - 1].
 * Absolute value is taken when n < 0
 *
 * @author Basil Hosmer
 */
public final class _count extends IntrinsicLambda
{
    public static final _count INSTANCE = new _count(); 
    public static final String NAME = "count";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Integer)arg);
    }

    public static ListValue invoke(final int n)
    {
        return IntRange.create(0, n >= 0 ? n : Math.abs(n));
    }
}
