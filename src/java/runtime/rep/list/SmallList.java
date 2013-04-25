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
 * Representation class for persistent lists with
 * {@link PersistentList#NODE_SIZE} items or fewer.
 * Instances are created by {@link PersistentList#alloc}
 * only.
 *
 * @author Basil Hosmer
 */
final class SmallList extends PersistentList
{
    private final Object[] data;

    /**
     * allocates but doesn't initialize data array
     */
    SmallList(final int size)
    {
        assert size <= NODE_SIZE;
        this.data = (Object[])BigList.allocData(size, 1);
    }

    /**
     * initializes data array from iterator
     */
    SmallList(final Iterator<?> iter, final int size)
    {
        assert size <= NODE_SIZE;
        this.data = (Object[])BigList.initData(size, 1, iter);
    }

    /**
     * takes ownership of passed data array
     */
    SmallList(final Object... data)
    {
        assert data.length <= NODE_SIZE;
        this.data = data;
    }

    public int size()
    {
        return data.length;
    }

    public Object get(final int index)
    {
        if (index < 0 || index >= size())
            throw new IndexOutOfBoundsException("index = " + index + ", size = " + size());

        return data[index];
    }

    public int find(final Object value)
    {
        for (int i = 0; i < data.length; i++)
            if (data[i].equals(value))
                return i;

        return data.length;
    }

    public PersistentList append(final Object value)
    {
        final int size = data.length;

        if (size == NODE_SIZE)
        {
            return new BigList(NODE_SIZE + 1,
                new Object[]{data, BigList.wrap(1, value)});
        }
        else
        {
            final Object[] result = new Object[size + 1];
            System.arraycopy(data, 0, result, 0, size);
            result[size] = value;

            return new SmallList(result);
        }
    }

    public PersistentList appendUnsafe(final Object value)
    {
        return append(value);
    }

    public PersistentList update(final int index, final Object value)
    {
        return new SmallList(updateData(index, value, false));
    }

    public PersistentList updateUnsafe(final int index, final Object value)
    {
        updateData(index, value, true);
        return this;
    }

    private Object[] updateData(final int index, final Object item, final boolean mutate)
    {
        final Object[] result = mutate ? data : data.clone();
        result[index] = item;
        return result;
    }

    public ListValue subList(final int from, final int to)
    {
        return Sublist.create(this, from, to);
    }

    public PersistentList apply(final Lambda f)
    {
        return new SmallList(applyData(f));
    }

    private Object[] applyData(final Lambda f)
    {
        final int size = data.length;
        final Object[] result = new Object[size];
        for (int i = 0; i < size; i++)
            result[i] = f.apply(data[i]);
        return result;
    }

    public void run(final Lambda f)
    {
        runData(f);
    }

    private void runData(final Lambda f)
    {
        final int size = data.length;
        for (int i = 0; i < size; i++)
            f.apply(data[i]);
    }

    public PersistentList select(final ListValue list)
    {
        final int size = data.length;
        final Object[] result = new Object[size];
        for (int i = 0; i < size; i++)
            result[i] = list.get((Integer)data[i]);

        return new SmallList(result);
    }

    public PersistentList select(final MapValue map)
    {
        final int size = data.length;
        final Object[] result = new Object[size];
        for (int i = 0; i < size; i++)
            result[i] = map.get(data[i]);

        return new SmallList(result);
    }

    // Iterable

    public Iterator<Object> iterator(final int from, final int to)
    {
        assert from >= 0 && to <= data.length;

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

                return data[i++];
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}
