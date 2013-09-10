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
package runtime.rep.list;

import runtime.rep.Lambda;
import runtime.rep.map.MapValue;

import java.util.Iterator;
import java.util.List;

/**
 * Interface for runtime values of type List(T).
 * Extends java.util.Collection with copy-on-write and
 * utility methods, and requires implementors to
 * implement mutating List methods by throwing
 * {@link UnsupportedOperationException}.
 *
 * @author Basil Hosmer
 */
public interface ListValue extends List<Object>
{
    /**
     * Return first position containing an item for which
     * {@link Object#equals item.equals(value)} returns true,
     * or {@link #size()} if no such item exists.
     */
    int find(Object value);

    /**
     * Return new list with same items as self
     * and given value appended to end.
     */
    ListValue append(Object value);

    /**
     * Return new list with same items as self
     * but with given value given position. Valid
     * values of index are 0..size() - 1.
     * Note: throws IndexOutOfBoundsException on bad
     * index, currently
     */
    ListValue update(int index, Object value);

    /**
     * Return iterator over the selected range.
     * Makes implementation-specific iterators
     * available to sublists.
     */
    Iterator<Object> iterator(final int from, final int to);

    /**
     * map(list, f) => list.apply(f)
     */
    ListValue apply(final Lambda f);

    /**
     * {@link #apply} without result
     */
    void run(final Lambda f);

    /**
     * llmap(indexes, list) => indexes.select(list)
     */
    ListValue select(final ListValue list);

    /**
     * mlmap(keylist, map) => keylist.select(map)
     */
    ListValue select(final MapValue map);
}
