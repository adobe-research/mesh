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
import runtime.rep.Tuple;

/**
 * find() for strings
 * NOTE find substring, not char
 *
 * @author Basil Hosmer
 */
public final class _strfind extends IntrinsicLambda
{
    public static final _strfind INSTANCE = new _strfind(); 
    public static final String NAME = "strfind";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((String)args.get(0), (String)args.get(1));
    }

    public static int invoke(final String toseek, final String tofind)
    {
        final int index = toseek.indexOf(tofind);
        return index >= 0 ? index : toseek.length();
    }
}
