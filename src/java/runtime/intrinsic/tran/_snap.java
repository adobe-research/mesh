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

import runtime.intrinsic.IntrinsicLambda;
import runtime.tran.Box;

/**
 * Snapshot the value held in a box.
 * Unlike {@link Get}, snap() doesn't pin, even in
 * r/w transactions. Power tool, enough rope, etc.
 *
 * @author Basil Hosmer
 */
public final class _snap extends IntrinsicLambda
{
    public static final _snap INSTANCE = new _snap(); 
    public static final String NAME = "snap";

    public String getName()
    {
        return NAME;
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
