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

import runtime.rep.list.ChainedListPair;
import runtime.rep.list.ListValue;
import runtime.rep.Tuple;

/**
 * list concatenation
 *
 * @author Basil Hosmer
 */
public final class _lplus extends IntrinsicLambda
{
    public static final _lplus INSTANCE = new _lplus(); 
    public static final String NAME = "lplus";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((ListValue)args.get(0), (ListValue)args.get(1));
    }

    public static ListValue invoke(final ListValue llist, final ListValue rlist)
    {
        return rlist.size() == 1 ?
            llist.append(rlist.get(0)) :
            ChainedListPair.create(llist, rlist);
    }
}
