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

/**
 * rest(list) = all but first element of non-empty list
 * empty list throws, currently.
 *
 * @author Basil Hosmer
 */
public final class _rest extends IntrinsicLambda
{
    public static final _rest INSTANCE = new _rest(); 
    public static final String NAME = "rest";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((ListValue)arg);
    }

    public static ListValue invoke(final ListValue list)
    {
        return list.subList(1, list.size());
    }
}
