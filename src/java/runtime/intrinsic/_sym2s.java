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
import runtime.rep.Symbol;

/**
 * sym to string
 *
 * @author Basil Hosmer
 */
public final class _sym2s extends IntrinsicLambda
{
    public static final _sym2s INSTANCE = new _sym2s(); 
    public static final String NAME = "sym2s";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Symbol)arg);
    }

    public static String invoke(final Symbol sym)
    {
        return sym.getValue();
    }
}
