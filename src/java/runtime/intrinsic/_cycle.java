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
import runtime.rep.Tuple;

/**
 * iterate on an endofunction while guard predicate returns true.
 * iteration is started with specified initial argument, which is
 * passed through the guard before being applied.
 *
 * TODO once escape analysis is used to optimize away boxes,
 * there's no reason why this couldn't be moved to source.
 * Same with other similar intrinsics.
 *
 * @author Basil Hosmer
 */
public final class _cycle extends IntrinsicLambda
{
    public static final _cycle INSTANCE = new _cycle(); 
    public static final String NAME = "cycle";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke(args.get(0), (Lambda)args.get(1), (Lambda)args.get(2));
    }

    public static Object invoke(final Object initial, final Lambda pred, final Lambda f)
    {
        Object result = initial;

        while ((Boolean)pred.apply(result))
            result = f.apply(result);

        return result;
    }
}
