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

import runtime.rep.Lambda;
import runtime.rep.Tuple;

/**
 * Boolean (shortcutting) and of boolean value on left,
 * and boolean-returning closure on right.
 *
 * @author Basil Hosmer
 */
public final class _and extends IntrinsicLambda
{
    public static final _and INSTANCE = new _and(); 
    public static final String NAME = "and";

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
        return left && (Boolean)right.apply(Tuple.UNIT);
    }
}
