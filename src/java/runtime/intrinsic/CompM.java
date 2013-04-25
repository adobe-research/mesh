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

import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
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
public final class CompM extends IntrinsicLambda
{
    public static final String NAME = "compm";

    private static final TypeParam K = new TypeParam("K");
    private static final TypeParam X = new TypeParam("X");
    private static final TypeParam Y = new TypeParam("Y");
    private static final Type FUNC_X_K = Types.fun(X, K);
    private static final Type MAP_K_Y = Types.map(K, Y);
    private static final Type FUNC_X_Y = Types.fun(X, Y);

    public static final Type TYPE = Types.fun(Types.tup(FUNC_X_K, MAP_K_Y), FUNC_X_Y);

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
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