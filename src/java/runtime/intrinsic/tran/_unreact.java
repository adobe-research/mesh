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
import runtime.rep.Lambda;
import runtime.rep.Tuple;
import runtime.tran.Box;
import runtime.tran.TransactionManager;

/**
 * Remove a reactor function from a box.
 * Function equality is identity, so you need to pass
 * the reactor function itself.
 *
 * @author Basil Hosmer
 */
public final class _unreact extends IntrinsicLambda
{
    public static final _unreact INSTANCE = new _unreact();
    public static final String NAME = "unreact";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Box)args.get(0), (Lambda)args.get(1));
    }

    public static Box invoke(final Box box, final Lambda reactor)
    {
        TransactionManager.apply(new Lambda()
        {
            public Object apply(final Object unit)
            {
                TransactionManager.own(box);
                box.removeReactor(reactor);
                return null;
            }
        });

        return box;
    }
}
