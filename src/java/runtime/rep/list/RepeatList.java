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

import runtime.rep.lambda.Lambda;
import runtime.rep.map.MapValue;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Virtualized list that repeats a particular value
 * a given number of times.
 *
 * @author Basil Hosmer
 */
public final class RepeatList implements ListValue
{
    private final int size;
    private final Object value;

    public RepeatList(final int size, final Object value)
    {
        this.size = size;
        this.value = value;
    }

    public int size()
    {
        return size;
    }

    public Object get(final int index)
    {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        return value;
    }

    public int find(final Object val)
    {
        return value.equals(val) ? 0 : size;
    }

    public ListValue append(final Object value)
    {
        return ChainedListPair.create(this, PersistentList.single(value));
    }

    public PersistentList update(final int index, final Object value)
    {
        return PersistentList.init(iterator(), size).updateUnsafe(index, value);
    }

    public ListValue subList(final int from, final int to)
    {
        checkRange(from, to);
        return new RepeatList(to - from, value);
    }

    public ListValue apply(final Lambda f)
    {
        final PersistentList result = PersistentList.alloc(size);

        for (int i = 0; i < size; i++)
            result.updateUnsafe(i, f.apply(value));

        return result;
    }

    public void run(final Lambda f)
    {
        for (int i = 0; i < size; i++)
            f.apply(value);
    }

    public ListValue select(final ListValue base)
    {
        return new RepeatList(size(),  base.get((Integer)value));
    }

    public ListValue select(final MapValue base)
    {
        return new RepeatList(size(),  base.get(value));
    }

    public Iterator<Object> iterator()
    {
        return iterator(0, size);
    }

    public Iterator<Object> iterator(final int from, final int to)
    {
        assert from >= 0 && to <= size;

        return new Iterator<Object>()
        {
            int i = from;

            public boolean hasNext()
            {
                return i < to;
            }

            public Object next()
            {
                if (!hasNext())
                    throw new NoSuchElementException();

                i++;
                return value;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private boolean checkRange(final int from, final int to)
    {
        if (from < 0)
            throw new IndexOutOfBoundsException("(from = " + from + ") < 0");
        else if (to < from)
            throw new IndexOutOfBoundsException("(to = " + to + ") < (from = " + from + ")");
        else if (to > size())
            throw new IndexOutOfBoundsException("(to = " + to + ") > (size = " + size() + ")");

        return true;
    }

    @Override
    public final boolean equals(final Object o)
    {
        if (o == this)
        {
            return true;
        }
        else if (o instanceof ListValue)
        {
            final ListValue other = (ListValue)o;

            if (size() != other.size())
                return false;

            final Iterator<?> e1 = iterator();
            final Iterator<?> e2 = other.iterator();

            while (e1.hasNext() && e2.hasNext())
                if (!e1.next().equals(e2.next()))
                    return false;

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public final int hashCode()
    {
        int hash = 1;

        for (final Object obj : this)
            hash = 31 * hash + obj.hashCode();

        return hash;
    }
}