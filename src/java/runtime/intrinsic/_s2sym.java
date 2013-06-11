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

import runtime.rep.Symbol;

/**
 * string to symbol.
 * Parse failures throw currently. TODO variants.
 *
 * @author Basil Hosmer
 */
public final class _s2sym extends IntrinsicLambda
{
    public static final _s2sym INSTANCE = new _s2sym(); 
    public static final String NAME = "s2sym";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((String)arg);
    }

    public static Symbol invoke(final String str)
    {
        return Symbol.get(str);
    }
}
