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

import runtime.rep.Tuple;
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;

/**
 * functional map: apply a function to a list of arguments,
 * yielding a congruent list of results. Aliased to infix
 * operator '|' in {@link compile.parse.Ops}.
 *
 * @author Basil Hosmer
 */
public final class _map extends IntrinsicLambda
{
    public static final _map INSTANCE = new _map(); 
    public static final String NAME = "map";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((ListValue)args.get(0), (Lambda)args.get(1));
    }

    public static ListValue invoke(final ListValue args, final Lambda func)
    {
        return args.apply(func);
    }
}
