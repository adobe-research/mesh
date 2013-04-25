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
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

import java.util.Map;

/**
 * Merge two maps. Right map wins where keysets overlap.
 *
 * @author Basil Hosmer
 */
public final class _mplus extends IntrinsicLambda
{
    public static final _mplus INSTANCE = new _mplus(); 
    public static final String NAME = "mplus";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((MapValue)args.get(0), (MapValue)args.get(1));
    }

    public static MapValue invoke(final MapValue left, final MapValue right)
    {
        MapValue result = left;

        for (final Map.Entry<?, ?> entry : right.entrySet())
            result = result.assoc(entry.getKey(), entry.getValue());

        return result;
    }
}
