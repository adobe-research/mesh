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
package runtime.intrinsic.tran;

import runtime.rep.Tuple;
import runtime.intrinsic.IntrinsicLambda;
import runtime.tran.Box;

/**
 * Create a tuple of boxes initialized with a tuple of values.
 *
 * @author Basil Hosmer
 */
public final class _boxes extends IntrinsicLambda
{
    public static final _boxes INSTANCE = new _boxes();
    public static final String NAME = "boxes";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Tuple)arg);
    }

    public static Tuple invoke(final Tuple values)
    {
        final int w = values.size();

        final Object[] boxes = new Object[w];

        for (int i = 0; i < w; i++)
            boxes[i] = new Box(values.get(i));

        return Tuple.from(boxes);
    }
}
