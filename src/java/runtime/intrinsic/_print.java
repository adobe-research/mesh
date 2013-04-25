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
import runtime.rep.Tuple;

/**
 * print(x) == {@link PrintStr printstr}({@link ToStr tostr}(x))
 *
 * @author Basil Hosmer
 */
public final class _print extends IntrinsicLambda
{
    public static final _print INSTANCE = new _print(); 
    public static String NAME = "print";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke(arg);
    }

    public static Tuple invoke(final Object obj)
    {
        System.out.println(_tostr.invoke(obj));
        System.out.flush();

        return Tuple.UNIT;
    }
}
