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
package runtime.intrinsic.tran;

import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.tran.Box;

/**
 * Snapshot the value held in a box.
 * Unlike {@link Get}, snap() doesn't pin, even in
 * r/w transactions. Power tool, enough rope, etc.
 *
 * @author Basil Hosmer
 */
public final class Snap extends IntrinsicLambda
{
    public static final String NAME = "snap";

    private static final TypeParam T = new TypeParam("T");
    private static final Type BOX_T = Types.box(T);
    public static final Type TYPE = Types.fun(BOX_T, T);

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
        return invoke((Box)arg);
    }

    public static Object invoke(final Box box)
    {
        return box.snapValue();
    }
}