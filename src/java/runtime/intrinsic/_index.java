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
import runtime.rep.list.IntRange;
import runtime.rep.list.ListValue;

/**
 * index(list) returns a list of ints [0, ..., size(list) - 1]
 *
 * @author Basil Hosmer
 */
public final class _index extends IntrinsicLambda
{
    public static final _index INSTANCE = new _index();
    public static final String NAME = "index";

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
        return IntRange.create(0, list.size());
    }
}
