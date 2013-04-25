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

import com.google.common.collect.Iterators;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.Tuple;

/**
 * wraps {@link String#split(String)}
 *
 * @author Basil Hosmer
 */
public final class _strsplit extends IntrinsicLambda
{
    public static final _strsplit INSTANCE = new _strsplit(); 
    public static final String NAME = "strsplit";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((String)args.get(0), (String)args.get(1));
    }

    public static ListValue invoke(final String s, final String patt)
    {
        final String[] subs = s.split(patt);
        return PersistentList.init(Iterators.forArray(subs), subs.length);
    }
}
