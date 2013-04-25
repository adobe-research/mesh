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
import runtime.rep.Tuple;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class PrFill extends IntrinsicLambda
{
    public static final String NAME = "prfill";

    public static final Type TYPE = Types.fun(Types.INT, Types.unit());

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
        return invoke((Integer)arg);
    }

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Tuple invoke(final int i)
    {
        if (Processing.INSTANCE != null)
        {
            if (i < 256)
            {
                Processing.INSTANCE.fill(i);
            }
            else
            {
                // processing issue - fill(i > 255) doesn't work right
                Processing.INSTANCE.fill((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
            }
        }

        return Tuple.UNIT;
    }
}