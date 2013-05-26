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
 * Integer bit shift right with sign extension
 *
 * @author Keith McGuigan
 */
public final class _shiftr extends IntrinsicLambda
{
    public static final _shiftr INSTANCE = new _shiftr(); 
    public static final String NAME = "shiftr";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (Integer)args.get(1));
    }

    public static int invoke(final int arg0, final int arg1)
    {
        return arg0 >> arg1;
    }
}
