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
 * guarded execution: guard(cond, def, block) { cond ? def : block() }
 *
 * @author Basil Hosmer
 */
public final class _guard extends IntrinsicLambda
{
    public static final _guard INSTANCE = new _guard(); 
    public static final String NAME = "guard";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Boolean)args.get(0), args.get(1), (Lambda)args.get(2));
    }

    public static Object invoke(final boolean cond, final Object def, final Lambda block)
    {
        return cond ? def : block.apply(Tuple.UNIT);
    }
}
