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
import runtime.rep.Tuple;

/**
 * long difference
 *
 * @author Basil Hosmer
 */
public final class _lminus extends IntrinsicLambda
{
    public static final _lminus INSTANCE = new _lminus(); 
    public static final String NAME = "lminus";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Long)args.get(0), (Long)args.get(1));
    }

    public static long invoke(final long arg0, final long arg1)
    {
        return arg0 - arg1;
    }
}
