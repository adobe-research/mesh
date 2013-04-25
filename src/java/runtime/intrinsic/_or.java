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
import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;

/**
 * Boolean (shortcutting) or of boolean value on left,
 * and boolean-returning closure on right.
 *
 * @author Basil Hosmer
 */
public final class _or extends IntrinsicLambda
{
    public static final _or INSTANCE = new _or(); 
    public static final String NAME = "or";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Boolean)args.get(0), (Lambda)args.get(1));
    }

    public static boolean invoke(final boolean left, final Lambda right)
    {
        return left || (Boolean)right.apply(Tuple.UNIT);
    }
}
