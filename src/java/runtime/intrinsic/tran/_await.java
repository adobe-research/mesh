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

import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;
import runtime.tran.*;

/**
 * Transactional wait/notify.
 * wait(box, pred) puts current thread into wait state
 * until/unless pred(get(box)) returns true. pred() is
 * called each time a value is committed to box.
 *
 * @author Basil Hosmer
 */
public final class _await extends IntrinsicLambda
{
    public static final _await INSTANCE = new _await();
    public static final String NAME = "await";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Box)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Box box, final Lambda pred)
    {
        if (TransactionManager.getTransaction() == null)
        {
            final Waiter waiter = new Waiter(Boxes.from(box), pred);
            waiter.start();
        }

        return Tuple.UNIT;
    }
}
