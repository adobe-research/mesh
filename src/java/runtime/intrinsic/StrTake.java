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

import compile.type.Type;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

/**
 * strtake(str, n) = first n if n > 0, last -n if n < 0. n > list size wraps
 * TODO should be replaced by take over list TC, which string implements
 *
 * @author Basil Hosmer
 */
public final class StrTake extends IntrinsicLambda
{
    public static final String NAME = "strtake";

    public static final Type TYPE = Types.fun(
        Types.tup(Types.INT, Types.STRING),
        Types.STRING);

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Integer)args.get(0), (String)args.get(1));
    }

    public static String invoke(final int n, final String s)
    {
        final int size = s.length();

        if (size == 0)
        {
            return s;
        }
        else
        {
            final int extent = Math.abs(n);

            if (extent == size)
            {
                return s;
            }
            else if (extent < size)
            {
                // capture fast non-rollover cases
                return n >= 0 ? s.substring(0, n) : s.substring(size + n);
            }
            else
            {
                final StringBuilder buf = new StringBuilder(Math.abs(n));
                if (n >= 0)
                {
                    for (int i = 0; i < n; i++)
                        buf.append(s.charAt(i % size));
                }
                else
                {
                    for (int i = n + 1; i <= 0; i++)
                        buf.append(s.charAt(size + (i % size) - 1));
                }
                return buf.toString();
            }
        }
    }
}