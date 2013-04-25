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
 * true if integer argument is an index value for list argument
 *
 * @author Basil Hosmer
 */
public final class _isindex extends IntrinsicLambda
{
    public static final _isindex INSTANCE = new _isindex(); 
    public static final String NAME = "isindex";

    public String getName()
    {
        return NAME;
    }

    public final Boolean apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (ListValue)args.get(1));
    }

    public static boolean invoke(final int index, final ListValue list)
    {
        return index >= 0 && index < list.size();
    }
}
