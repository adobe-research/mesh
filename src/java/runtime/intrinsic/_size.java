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
 * list size.
 * TODO collection TC
 *
 * @author Basil Hosmer
 */
public final class _size extends IntrinsicLambda
{
    public static final _size INSTANCE = new _size(); 
    public static final String NAME = "size";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((ListValue)arg);
    }

    public static int invoke(final ListValue list)
    {
        return list.size();
    }
}
