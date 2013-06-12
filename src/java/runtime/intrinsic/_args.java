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

import runtime.sys.Arguments;
import runtime.rep.list.ListValue;

/**
 * Retrieves command line arguments
 *
 * @author Keith McGuigan
 */
public final class _args extends IntrinsicLambda
{
    public static final _args INSTANCE = new _args();
    public static final String NAME = "args";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke();
    }

    public static ListValue invoke()
    {
        return Arguments.get();
    }
}
