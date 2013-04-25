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
import runtime.rep.lambda.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;

/**
 * Compose a function with a list, yielding a composite function
 * that uses the original function's result to index the list.
 * E.g.
 * > f = compl({ $0 % 3 }, ["One", "Two", "Three"])
 * > f(100)
 * "Two"
 *
 * @author Basil Hosmer
 */
public final class _compl extends IntrinsicLambda
{
    public static final _compl INSTANCE = new _compl(); 
    public static final String NAME = "compl";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Lambda)args.get(0), (ListValue)args.get(1));
    }

    public static Lambda invoke(final Lambda func, final ListValue list)
    {
        return new Lambda()
        {
            public Object apply(final Object x)
            {
                return list.get((Integer)func.apply(x));
            }
        };
    }
}
