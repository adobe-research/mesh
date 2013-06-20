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
 * Add a reactor to a box and return it.
 *
 * @author Basil Hosmer
 */
public final class _react extends IntrinsicLambda
{
    public static final _react INSTANCE = new _react();
    public static final String NAME = "react";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Box)args.get(0), (Lambda)args.get(1));
    }

    public static Lambda invoke(final Box box, final Lambda reactor)
    {
        TransactionManager.apply(new Lambda()
        {
            public Object apply(final Object unit)
            {
                TransactionManager.own(box);
                box.addReactor(reactor);
                return null;
            }
        });

        return reactor;
    }
}
