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

import runtime.rep.Lambda;
import runtime.rep.Record;
import runtime.rep.Tuple;
import runtime.rep.Variant;

/**
 * select combinator
 * NOTE: not currently declared as an intrinsic, because
 * its type isn't yet expressible in the type language.
 * Specifically, we don't yet have type-level zip,
 * nor a type-level list singleton maker (here "List").
 *
 *  <Key, Vals:[*], R>
 *      select(sel:Var(Assoc(K, Vs)), cases:Rec(Assoc(K, Cone(Vs, R))));
 *
 * @author Basil Hosmer
 */
public final class _cond extends IntrinsicLambda
{
    public static final _cond INSTANCE = new _cond();
    public static final String NAME = "cond";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Variant)args.get(0), (Record)args.get(1));
    }

    public static Object invoke(final Variant sel, final Record handlers)
    {
        return ((Lambda)handlers.get(sel.getKey())).apply(sel.getValue());
    }
}
