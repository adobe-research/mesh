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
 * while combinator.
 * run a block repeatedly while guard predicate returns true.
 *
 * @author Basil Hosmer
 */
public final class _while extends IntrinsicLambda
{
    public static final _while INSTANCE = new _while(); 
    public static final String NAME = "while";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Lambda)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Lambda pred, final Lambda f)
    {
        final Tuple unit = Tuple.UNIT;

        while ((Boolean)pred.apply(unit))
            f.apply(unit);

        return unit;
    }
}
