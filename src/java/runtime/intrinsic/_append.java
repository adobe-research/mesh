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
 * Return new list with original list's contents
 * with new value appended.
 *
 * @author Basil Hosmer
 */
public final class _append extends IntrinsicLambda
{
    public static final _append INSTANCE = new _append(); 
    public static final String NAME = "append";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object obj)
    {
        final Tuple args = (Tuple)obj;
        return invoke((ListValue)args.get(0), args.get(1));
    }

    public static ListValue invoke(final ListValue list, final Object item)
    {
        return list.append(item);
    }
}
