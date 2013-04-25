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
package runtime.intrinsic.demo.processing;

import compile.type.Type;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class PrOpen extends IntrinsicLambda
{
    public static final String NAME = "propen";

    private static final Type BLOCK =
        Types.fun(Types.unit(), Types.unit());

    private static final Type METHOD_MAP_TYPE = Types.map(Types.SYMBOL, BLOCK);

    public static final Type TYPE =
        Types.fun(Types.tup(Types.STRING, METHOD_MAP_TYPE),
            Types.unit());

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
        return invoke((String)args.get(0), (MapValue)args.get(1));
    }

    public static Tuple invoke(final String title, final MapValue methodMap)
    {
        Processing.open(title, methodMap);
        return Tuple.UNIT;
    }
}