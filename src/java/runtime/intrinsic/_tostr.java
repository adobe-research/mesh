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

import compile.StringUtils;
import runtime.rep.Record;
import runtime.rep.Symbol;
import runtime.rep.Tuple;
import runtime.rep.Variant;
import runtime.rep.list.ListValue;
import runtime.rep.map.MapValue;
import runtime.tran.Box;

import java.util.ArrayList;
import java.util.Map;

/**
 * print a string representation of any value.
 * goal is print/parse round trip for a well-defined
 * subset of values.
 *
 * @author Basil Hosmer
 */
public final class _tostr extends IntrinsicLambda
{
    public static final _tostr INSTANCE = new _tostr(); 
    public static String NAME = "tostr";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke(arg);
    }

    public static String invoke(final Object value)
    {
        if (value instanceof String)
        {
            return ("\"" + StringUtils.escapeJava((String)value) + '"');
        }
        else if (value instanceof Symbol)
        {
            return "#" + ((Symbol)value).getValue();
        }
        else if (value instanceof ListValue)
        {
            return printList((ListValue)value);
        }
        else if (value instanceof MapValue)
        {
            return printMap((MapValue)value);
        }
        else if (value instanceof Tuple)
        {
            return printTuple((Tuple)value);
        }
        else if (value instanceof Record)
        {
            return printRecord((Record)value);
        }
        else if (value instanceof Variant)
        {
            final Variant variant = (Variant)value;
            return dumpKey(variant.getKey()) + " ! " + invoke(variant.getValue());
        }
        else if (value instanceof Box)
        {
            return "box(" + invoke(((Box)value).getValue()) + ")";
        }
        else if (value != null)
        {
            // numbers, bools, Lambdas
            return value.toString();
        }
        else
        {
            return "null";
        }
    }

    private static String printList(final ListValue list)
    {
        final ArrayList<String> prints = new ArrayList<String>(list.size());
        for (final Object item : list)
            prints.add(invoke(item));

        return "[" + StringUtils.join(prints, ", ") + "]";
    }

    private static String printMap(final MapValue map)
    {
        if (map.isEmpty())
            return "[:]";

        final ArrayList<String> prints = new ArrayList<String>(map.size());
        for (final Map.Entry<?, ?> entry : map.entrySet())
            prints.add(invoke(entry.getKey()) + ": " + invoke(entry.getValue()));

        return "[" + StringUtils.join(prints, ", ") + "]";
    }

    private static String printTuple(final Tuple tuple)
    {
        final int n = tuple.size();
        final ArrayList<String> prints = new ArrayList<String>(n);

        for (int i = 0; i < n; i++)
            prints.add(invoke(tuple.get(i)));

        return "(" + StringUtils.join(prints, ", ") +
            (n == 1 ? "," : "") +
            ")";
    }

    private static String printRecord(final Record record)
    {
        final int n = record.size();
        if (n == 0)
            return "(:)";

        final ArrayList<String> prints = new ArrayList<String>(n);
        for (int i = 0; i < record.size(); i++)
        {
            prints.add(dumpKey(record.getKey(i)) + ": " +
                invoke(record.getValue(i)));
        }

        return "(" + StringUtils.join(prints, ", ") + ")";
    }

    private static String dumpKey(final Object key)
    {
        return key instanceof Symbol ?
            ((Symbol)key).getValue() : invoke(key);
    }
}
