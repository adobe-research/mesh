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

import runtime.rep.list.ChainedLists;
import runtime.rep.list.ListValue;

/**
 * Concatenate multiple lists together.
 *
 * @author Basil Hosmer
 */
public final class _flatten extends IntrinsicLambda
{
    public static final _flatten INSTANCE = new _flatten(); 
    public static final String NAME = "flatten";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((ListValue) arg);
    }

    public static ListValue invoke(final ListValue lists)
    {
        return ChainedLists.create(lists);
    }
}
