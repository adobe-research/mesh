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

import com.google.common.collect.Iterators;
import runtime.rep.Lambda;
import runtime.rep.map.MapValue;

import java.util.Iterator;

/**
 * Empty list.
 *
 * @author Basil Hosmer
 */
final class EmptyList extends PersistentList
{
    public static final EmptyList INSTANCE = new EmptyList();

    private EmptyList()
    {
    }

    // PersistentList

    public PersistentList appendUnsafe(final Object value)
    {
        return append(value);
    }

    public PersistentList updateUnsafe(final int index, final Object value)
    {
        return update(index, value);
    }

    // ListValue

    public int find(final Object value)
    {
        return 0;
    }

    public PersistentList append(final Object value)
    {
        return single(value);
    }

    public PersistentList update(final int index, final Object value)
    {
        throw new IndexOutOfBoundsException();
    }

    public Iterator<Object> iterator(final int from, final int to)
    {
        assert from == 0 && to == 0;
        return Iterators.emptyIterator();
    }

    public ListValue apply(final Lambda f)
    {
        return EMPTY;
    }

    public void run(final Lambda f)
    {
    }

    public ListValue select(final ListValue list)
    {
        return EMPTY;
    }

    public ListValue select(final MapValue map)
    {
        return EMPTY;
    }

    // List<Object>

    public int size()
    {
        return 0;
    }

    public Object get(final int index)
    {
        throw new IndexOutOfBoundsException();
    }

    public ListValue subList(final int from, final int to)
    {
        return Sublist.create(this, from, to);
    }
}
