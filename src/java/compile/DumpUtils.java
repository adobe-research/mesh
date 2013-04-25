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
package compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for dumping Dumpable objects and collections.
 *
 * @author Basil Hosmer
 */
public final class DumpUtils
{
    private static String dumpItem(final Dumpable item)
    {
        return item != null ? item.dump() : "null";
    }

    public static List<String> dumpEach(final Iterable<? extends Dumpable> items)
    {
        final List<String> dumps = new ArrayList<String>();
        for (final Dumpable item : items)
            dumps.add(dumpItem(item));
        return dumps;
    }

    public static String dumpList(final Iterable<? extends Dumpable> items)
    {
        return StringUtils.join(dumpEach(items), ", ");
    }

    public static String dumpList(final Iterable<? extends Dumpable> items, final String sep)
    {
        return StringUtils.join(dumpEach(items), sep);
    }

    public static String dumpMap(final Map<? extends Dumpable, ? extends Dumpable> map)
    {
        if (map.isEmpty())
            return "";

        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<? extends Dumpable, ? extends Dumpable> entry : map.entrySet())
            sb.append(", ").append(dumpItem(entry.getKey())).append(": ").append(
                dumpItem(entry.getValue()));

        return sb.substring(2);
    }
}
