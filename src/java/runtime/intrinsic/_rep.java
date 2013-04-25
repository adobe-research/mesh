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
import runtime.rep.list.RepeatList;
import runtime.rep.Tuple;

/**
 * rep(n, item)
 *
 * @author Basil Hosmer
 */
public final class _rep extends IntrinsicLambda
{
    public static final _rep INSTANCE = new _rep(); 
    public static final String NAME = "rep";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), args.get(1));
    }

    public static ListValue invoke(final int n, final Object item)
    {
        return new RepeatList(n, item);
    }
}
