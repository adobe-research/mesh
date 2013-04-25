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

import runtime.rep.Tuple;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.tran.MultiWaiter;
import runtime.tran.TransactionManager;

/**
 * Transactional wait/notify against a tuple of boxes.
 * wait(boxes, pred) puts current thread into wait state
 * until/unless pred(gets(boxes)) returns true. pred() is
 * called each time a value is committed to any box.
 *
 * @author Basil Hosmer
 */
public final class _awaits extends IntrinsicLambda
{
    public static final _awaits INSTANCE = new _awaits(); 
    public static final String NAME = "awaits";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Tuple)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Tuple boxes, final Lambda pred)
    {
        if (TransactionManager.getTransaction() == null)
            new MultiWaiter(boxes).wait(pred);

        return Tuple.UNIT;
    }
}
