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

import runtime.rep.list.ListValue;
import runtime.rep.map.MapValue;
import runtime.rep.Record;
import runtime.rep.Tuple;

/**
 * empty(T) true if collection or structure is empty
 * TODO use TCs for poly once available
 *
 * @author Basil Hosmer
 */
public final class _empty extends IntrinsicLambda
{
    public static final _empty INSTANCE = new _empty(); 
    public static final String NAME = "empty";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke(arg);
    }

    public static boolean invoke(final Object obj)
    {
        if (obj instanceof ListValue)
        {
            return ((ListValue)obj).size() == 0;
        }
        else if (obj instanceof MapValue)
        {
            return ((MapValue)obj).isEmpty();
        }
        else if (obj instanceof Tuple)
        {
            return ((Tuple)obj).size() == 0;
        }
        else if (obj instanceof Record)
        {
            return ((Record)obj).size() == 0;
        }
        else if (obj instanceof String)
        {
            return ((String)obj).length() == 0;
        }
        else if (obj == null)
        {
            throw new NullPointerException("empty(null)");
        }
        else
        {
            return false;
        }
    }
}
