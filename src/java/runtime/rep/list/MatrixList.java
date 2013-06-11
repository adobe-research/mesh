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
import java.util.NoSuchElementException;

/**
 * Sequence of regular-length lists representing a single list.
 *
 * @author Basil Hosmer
 */
public final class MatrixList implements ListValue
{
    //
    // instance
    //

    private final ListValue lists;
    private final int size;
    private final int stride;

    public MatrixList(final ListValue lists, final int stride)
    {
        this.lists = lists;
        this.size = lists.size() * stride;
        this.stride = stride;

        assert stride > 0;
    }

    public int size()
    {
        return size;
    }

    public Object get(final int index)
    {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        return ((ListValue)lists.get(index / stride)).get(index % stride);
    }

    public int find(final Object value)
    {
        int base = 0;
        for (final Object item : lists)
        {
            final ListValue list = (ListValue)item;
            final int i = list.find(value);
            if (i < stride)
                return base + i;
            base += stride;
        }
        return base;
    }

    public ListValue append(final Object value)
    {
        return ChainedListPair.create(this, new SingletonList(value));
    }

    public ListValue update(final int index, final Object value)
    {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        final int li = index / stride;
        final ListValue list = (ListValue)lists.get(li);

        return new MatrixList(
            lists.update(li, list.update(index % stride, value)), stride);
    }

    public ListValue subList(final int from, final int to)
    {
        return Sublist.create(this, from, to);
    }

    public ListValue apply(final Lambda f)
    {
        final PersistentList result = PersistentList.alloc(size);

        int i = 0;
        for (final Object list : lists)
            for (final Object item : (ListValue)list)
                result.updateUnsafe(i++, f.apply(item));

        return result;
    }

    public void run(final Lambda f)
    {
        for (final Object list : lists)
            ((ListValue)list).run(f);
    }

    public ListValue select(final ListValue base)
    {
        final PersistentList result = PersistentList.alloc(size);

        int i = 0;
        for (final Object list : lists)
            for (final Object item : (ListValue)list)
                result.updateUnsafe(i++, base.get((Integer)item));

        return result;
    }

    public ListValue select(final MapValue map)
    {
        final PersistentList result = PersistentList.alloc(size);

        int i = 0;
        for (final Object list : lists)
            for (final Object item : (ListValue)list)
                result.updateUnsafe(i++, map.get(item));

        return result;
    }

    // Iterable

    public Iterator<Object> iterator()
    {
        return iterator(0, size);
    }

    public Iterator<Object> iterator(final int from, final int to)
    {
        final int li = from / stride;

        return new Iterator<Object>()
        {
            final Iterator<?> listIter = lists.iterator(li, lists.size());

            int i = from;
            int j = from % stride;

            Iterator<?> itemIter =
                ((ListValue)listIter.next()).iterator(j, stride);

            public final boolean hasNext()
            {
                return i < to;
            }

            public final Object next()
            {
                if (i == to)
                    throw new NoSuchElementException();

                if (j == stride)
                {
                    itemIter = ((ListValue)listIter.next()).iterator();
                    j = 0;
                }

                i++;
                j++;

                return itemIter.next();
            }

            public final void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (obj instanceof ListValue)
        {
            final ListValue other = (ListValue)obj;

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
