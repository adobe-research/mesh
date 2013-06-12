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
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

/**
 * Compose a function with a map, yielding a composite function
 * that uses the original function's result to index the map. E.g.
 * > f = compm({ iif($0, #ok, #err) }, [#ok: "OK", #err: "ERR"])
 * > f(false)
 * "ERR"
 *
 * @author Basil Hosmer
 */
public final class _compm extends IntrinsicLambda
{
    public static final _compm INSTANCE = new _compm(); 
    public static final String NAME = "compm";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Lambda)args.get(0), (MapValue)args.get(1));
    }

    public static Lambda invoke(final Lambda func, final MapValue map)
    {
        return new Lambda()
        {
            public Object apply(final Object x)
            {
                return map.get(func.apply(x));
            }
        };
    }
}
