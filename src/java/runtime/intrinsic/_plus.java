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

import compile.Session;
import runtime.rep.list.ListValue;
import runtime.rep.map.MapValue;
import runtime.rep.Tuple;

/**
 * intrinsic plus() - typed over all types, structures give RTE currently
 * TODO tighten up once TC-like functionality becomes available
 *
 * @author Basil Hosmer
 */
public final class _plus extends IntrinsicLambda
{
    public static final _plus INSTANCE = new _plus(); 
    public static final String NAME = "plus";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke(args.get(0), args.get(1));
    }

    public static Object invoke(final Object left, final Object right)
    {
        if (left instanceof Integer)
        {
            return (Integer)left + (Integer)right;
        }
        else if (left instanceof Double)
        {
            return (Double)left + (Double)right;
        }
        else if (left instanceof Long)
        {
            return (Long)left + (Long)right;
        }
        else if (left instanceof Float)
        {
            return (Float)left + (Float)right;
        }
        else if (left instanceof String)
        {
            return (String)left + right;
        }
        else if (left instanceof ListValue)
        {
            return _lplus.invoke((ListValue)left, (ListValue)right);
        }
        else if (left instanceof MapValue)
        {
            return _mplus.invoke((MapValue)left, (MapValue)right);
        }
        else if (left instanceof Boolean)
        {
            return (Boolean)left || (Boolean)right;
        }
        else
        {
            Session.error("internal error: plus() undefined over {0}, {1} ({2}, {3})",
                _tostr.invoke(left), _tostr.invoke(right),
                left.getClass().getName(), right.getClass().getName());

            return left;
        }
    }
}
