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
 * Singleton list.
 *
 * @author Basil Hosmer
 */
final class SingletonList extends PersistentList
{
    private Object value;

    SingletonList(final Object value)
    {
        this.value = value;
    }

    // PersistentList

    public PersistentList appendUnsafe(final Object value)
    {
        return append(value);
    }

    public PersistentList updateUnsafe(final int index, final Object value)
    {
        if (index > 0)
            throw new IndexOutOfBoundsException();

        this.value = value;
        return this;
    }

    // ListValue

    public int find(final Object value)
    {
        return value.equals(this.value) ? 0 : 1;
    }

    public PersistentList append(final Object value)
    {
        return new SmallList(this.value, value);
    }

    public PersistentList update(final int index, final Object value)
    {
        if (index > 0)
            throw new IndexOutOfBoundsException();

        return new SingletonList(value);
    }

    public Iterator<Object> iterator(final int from, final int to)
    {
        assert from >= 0 && to <= 1 : "from = " + from + ", to = " + to;
        return Iterators.singletonIterator(value);
    }

    public ListValue apply(final Lambda f)
    {
        return single(f.apply(value));
    }

    public void run(final Lambda f)
    {
        f.apply(value);
    }

    public ListValue select(final ListValue list)
    {
        return single(list.get((Integer)value));
    }

    public ListValue select(final MapValue map)
    {
        return single(map.get(value));
    }

    // List<Object>

    public int size()
    {
        return 1;
    }

    public Object get(final int index)
    {
        if (index != 0)
            throw new IndexOutOfBoundsException();

        return value;
    }

    public ListValue subList(final int from, final int to)
    {
        return Sublist.create(this, from, to);
    }
}
