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
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

/**
 * Apply a map of arguments to a function, yielding map of results:
 * E.g. mapmf([#a: (2, 2), #b: (3, 3)], (+))
 * => [#a: 4, #b: 6]
 *
 * @author Basil Hosmer
 */
public final class _mapmf extends IntrinsicLambda
{
    public static final _mapmf INSTANCE = new _mapmf(); 
    public static final String NAME = "mapmf";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((MapValue)args.get(0), (Lambda)args.get(1));
    }

    public static MapValue invoke(final MapValue map, final Lambda func)
    {
        return map.apply(func);
    }
}
