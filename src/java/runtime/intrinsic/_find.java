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
import runtime.rep.Tuple;

/**
 * returns index of first occurence of item in list, or list size
 *
 * @author Basil Hosmer
 */
public final class _find extends IntrinsicLambda
{
    public static final _find INSTANCE = new _find(); 
    public static final String NAME = "find";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((ListValue)args.get(0), args.get(1));
    }

    public static int invoke(final ListValue list, final Object val)
    {
        return list.find(val);
    }
}
