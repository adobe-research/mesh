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
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.Tuple;

/**
 * draw(n, max) returns list of n random ints
 * in the range 0..max - 1
 *
 * @author Basil Hosmer
 */
public final class _draw extends IntrinsicLambda
{
    public static final _draw INSTANCE = new _draw(); 
    public static final String NAME = "draw";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (Integer)args.get(1));
    }

    public static ListValue invoke(final int n, final int max)
    {
        final PersistentList result = PersistentList.alloc(n);

        for (int i = 0; i < n; i++)
            result.updateUnsafe(i, (int)(Math.random() * max));

        return result;
    }
}
