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
 * Persistent list implementation for lists bigger than
 * {@link #NODE_SIZE}. Contents are stored in a trie, with
 * index values treated as a sequence of bitfields forming
 * a path.
 *
 * @author Basil Hosmer
 */
public final class BigList extends PersistentList
{
    /**
     * Number of items in the list.
     */
    private final int size;

    /**
     * Tree height.
     */
    private final int height;

    /**
     * Maximum capacity of a tree of this height.
     */
    private final int capacity;

    /**
     * List contents.
     */
    private final Object[] data;

    /**
     * allocates but doesn't initialize data tree
     */
    BigList(final int size)
    {
        this(size, allocData(size, height(size)));
    }

    /**
     * initializes data tree from iterator
     */
    BigList(final Iterator<?> iter, final int size)
    {
        this(size, initData(size, height(size), iter));

    }

    /**
     * package-local for access from other list impls
     */
    BigList(final int size, final Object[] data)
    {
        assert size > NODE_SIZE;

        this.size = size;
        this.height = height(size);
        this.capacity = capacity(height);
        this.data = data;
    }

    /**
     * build data tree for a list of a given size.
     * height is passed along to avoid recalculation,
     * must be equal to {@link #height height(size)}.
     */
    static Object[] allocData(final int size, final int height)
    {
        if (height == 1)
            return new Object[size];

        final int childcap = capacity(height - 1);
        final int div = size / childcap;
        final int rem = size % childcap;
        final boolean ragged = rem > 0;

        final Object[] result = new Object[div + (ragged ? 1 : 0)];

        for (int i = 0; i < div; i++)
            result[i] = allocData(childcap, height - 1);

        if (ragged)
            result[div] = allocData(rem, height - 1);

        return result;
    }

    /**
     *
     */
    static Object[] initData(final int size, final int height,
        final Iterator<?> iter)
    {
        if (height == 1)
        {
            final Object[] result = new Object[size];

            for (int i = 0; i < size; i++)
                result[i] = iter.next();

            return result;
        }

        final int childcap = capacity(height - 1);
        final int div = size / childcap;
        final int rem = size % childcap;
        final boolean ragged = rem > 0;

        final Object[] result = new Object[div + (ragged ? 1 : 0)];

        for (int i = 0; i < div; i++)
            result[i] = initData(childcap, height - 1, iter);

        if (ragged)
            result[div] = initData(rem, height - 1, iter);

        return result;
    }

    // PersistentList

    public PersistentList appendUnsafe(final Object value)
    {
        if (size == capacity)
            return append(value);

        return new BigList(size + 1,
            appendData(data, size, height, capacity, value, true));
    }

    /**
     * append item to data tree. data must not be at capacity.
     */
    private static Object[] appendData(final Object data,
        final int origsize,
        final int height,
        final int capacity,
        final Object item,
        final boolean mutate)
    {
        if (height == 1)
        {
            // at leaf array
            final Object[] array = (Object[])data;
            final int length = array.length;

            final Object[] result = new Object[length + 1];

            System.arraycopy(array, 0, result, 0, length);
            result[length] = item;

            return result;
        }
        else
        {
            // non-leaf array
            final Object[] array = (Object[])data;
            final int length = array.length;

            final Object[] result;

            final int childcap = capacity >>> PATH_BITS;
            if (origsize % childcap == 0)
            {
                // item begins a new rightmost subtree
                result = new Object[length + 1];
                System.arraycopy(array, 0, result, 0, length);
                result[length] = wrap(height - 1, item);
            }
            else
            {
                // item falls within current rightmost subtree
                result = mutate ? array : array.clone();
                final int i = length - 1;
                result[i] =
                    appendData(result[i], origsize, height - 1, childcap, item, mutate);
            }

            return result;
        }
    }

    public PersistentList updateUnsafe(final int index, final Object value)
    {
        updateData(data, height, index, value, true);
        return this;
    }

    /**
     * Return tree data with item at given index replaced.
     * tree height is passed; must agree with data.
     * index must be less than current data size (unchecked).
     * If mutate flag is true, modification is in-place.
     */
    private static Object[] updateData(final Object data,
        final int height,
        final int index,
        final Object item,
        final boolean mutate)
    {
        if (height == 1)
        {
            final Object[] array = (Object[])data;

            final Object[] result = mutate ? array : array.clone();

            result[index & PATH_MASK] = item;

            return result;
        }
        else
        {
            final Object[] array = (Object[])data;

            final Object[] result = mutate ? array : array.clone();

            final int i = (index >>> ((height - 1) * PATH_BITS)) & PATH_MASK;

            result[i] = updateData(result[i], height - 1, index, item, mutate);

            return result;
        }
    }

    // ListValue

    public int find(final Object value)
    {
        int i = 0;

        for (final Object item : this)
        {
            if (item.equals(value))
                break;
            i++;
        }

        return i;
    }

    public PersistentList append(final Object value)
    {
        if (size == capacity)
            return new BigList(size + 1,
                new Object[]{data, wrap(height, value)});

        return new BigList(size + 1,
            appendData(data, size, height, capacity, value, false));
    }

    public BigList update(final int index, final Object value)
    {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("index = " + index + ", size = " + size);

        return new BigList(size,
            updateData(data, height, index, value, false));
    }

    public Iterator<Object> iterator(final int from, final int to)
    {
        assert from >= 0 && to <= size;

        return new Iterator<Object>()
        {
            int i = from;
            int off = i % NODE_SIZE;
            Object[] node = nodeForIndex(i);

            public boolean hasNext()
            {
                return i < to;
            }

            public Object next()
            {
                if (i == to)
                    throw new NoSuchElementException();

                if (off == NODE_SIZE)
                {
                    node = nodeForIndex(i);
                    off = 0;
                }

                final Object value = node[off];

                i++;
                off++;

                return value;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Object[] nodeForIndex(final int i)
    {
        Object[] node = data;

        for (int shift = (height - 1) * PATH_BITS; shift > 0; shift -= PATH_BITS)
            node = (Object[])node[(i >>> shift) & PATH_MASK];

        return node;
    }

    public PersistentList apply(final Lambda f)
    {
        return new BigList(size, applyData(f, data, size, height, capacity));
    }

    private static Object[] applyData(final Lambda f, final Object data,
        final int size, final int height, final int capacity)
    {
        if (size == 0)
            return null;

        final Object[] result;

        if (height == 1)
        {
            final Object[] array = (Object[])data;

            result = new Object[size];

            for (int i = 0; i < size; i++)
                result[i] = f.apply(array[i]);
        }
        else
        {
            final Object[] array = (Object[])data;

            final int childcap = capacity >> PATH_BITS;
            final int div = size / childcap;
            final int rem = size % childcap;
            final boolean ragged = rem > 0;

            result = new Object[div + (ragged ? 1 : 0)];

            for (int i = 0; i < div; i++)
                result[i] = applyData(f, array[i], childcap, height - 1, childcap);

            if (ragged)
                result[div] = applyData(f, array[div], rem, height - 1, childcap);
        }

        return result;
    }

    public void run(final Lambda f)
    {
        runData(f, data, size, height, capacity);
    }

    private static void runData(final Lambda f, final Object data,
        final int size, final int height, final int capacity)
    {
        if (size == 0)
            return;

        if (height == 1)
        {
            final Object[] array = (Object[])data;

            for (int i = 0; i < size; i++)
                f.apply(array[i]);
        }
        else
        {
            final Object[] array = (Object[])data;

            final int childcap = capacity >> PATH_BITS;
            final int div = size / childcap;
            final int rem = size % childcap;

            for (int i = 0; i < div; i++)
                runData(f, array[i], childcap, height - 1, childcap);

            if (rem > 0)
                runData(f, array[div], rem, height - 1, childcap);
        }
    }

    public BigList select(final ListValue list)
    {
        return new BigList(size,
            selectData(list, data, size, height, capacity));
    }

    private static Object[] selectData(final ListValue list, final Object data,
        final int size, final int height, final int capacity)
    {
        final Object[] result;

        if (height == 1)
        {
            final Object[] array = (Object[])data;

            result = new Object[size];

            for (int i = 0; i < size; i++)
                result[i] = list.get((Integer)array[i]);
        }
        else
        {
            final Object[] array = (Object[])data;

            final int childcap = capacity >> PATH_BITS;
            final int div = size / childcap;
            final int rem = size % childcap;
            final boolean ragged = rem > 0;

            result = new Object[div + (ragged ? 1 : 0)];

            for (int i = 0; i < div; i++)
                result[i] = selectData(list, array[i], childcap, height - 1, childcap);

            if (ragged)
                result[div] = selectData(list, array[div], rem, height - 1, childcap);
        }

        return result;
    }

    public BigList select(final MapValue map)
    {
        return new BigList(size,
            selectData(map, data, size, height, capacity));
    }

    private static Object[] selectData(final MapValue map, final Object data,
        final int size, final int height, final int capacity)
    {
        final Object[] result;

        if (height == 1)
        {
            final Object[] array = (Object[])data;

            result = new Object[size];

            for (int i = 0; i < size; i++)
                result[i] = map.get(array[i]);
        }
        else
        {
            final Object[] array = (Object[])data;

            final int childcap = capacity >> PATH_BITS;
            final int div = size / childcap;
            final int rem = size % childcap;
            final boolean ragged = rem > 0;

            result = new Object[div + (ragged ? 1 : 0)];

            for (int i = 0; i < div; i++)
                result[i] = selectData(map, array[i], childcap, height - 1, childcap);

            if (ragged)
                result[div] = selectData(map, array[div], rem, height - 1, childcap);
        }

        return result;
    }

    // List<Object>

    public final int size()
    {
        return size;
    }

    public Object get(final int index)
    {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("index = " + index + ", size = " + size);

        Object[] node = data;

        for (int shift = (height - 1) * PATH_BITS; shift > 0; shift -= PATH_BITS)
            node = (Object[])node[(index >>> shift) & PATH_MASK];

        return node[index & PATH_MASK];
    }

    public ListValue subList(final int from, final int to)
    {
        return Sublist.create(this, from, to);
    }

    //
    // static helpers
    //

    /**
     * capacity of tree of given height
     */
    private static int capacity(final int height)
    {
        return height == 0 ? 0 : (1 << (height * PATH_BITS));
    }

    /**
     * height of given size tree
     */
    private static int height(final int size)
    {
        return size == 0 ? 0 :
            1 + (31 - Integer.numberOfLeadingZeros(size - 1)) / PATH_BITS;
    }

    /**
     * wrap a value in a given number of singleton arrays to
     * make a subtree of the given height.
     */
    static Object wrap(final int height, final Object value)
    {
        assert height > 0;
        return height == 1 ? new Object[]{value} :
            new Object[]{wrap(height - 1, value)};
    }
}

