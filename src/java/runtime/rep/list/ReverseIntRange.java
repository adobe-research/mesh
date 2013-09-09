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
 * Virtualized list [start .. end], with start > end.
 * Instances are created by {@link IntRange#create}.
 *
 * @author Basil Hosmer
 */
public final class ReverseIntRange extends AbstractListValue
{
    private final int start;
    private final int end;
    private final int size;

    ReverseIntRange(final int start, final int end)
    {
        assert start >= end;

        this.start = start;
        this.end = end;
        this.size = start - end;
    }

    // ListValue

    public int find(final Object value)
    {
        final int i = (Integer)value;
        return i >= end && i < start ? start - i : size;
    }

    public ListValue append(final Object value)
    {
        return ChainedListPair.create(this, PersistentList.single(value));
    }

    public ListValue update(final int index, final Object value)
    {
        return PersistentList.init(iterator(), size()).updateUnsafe(index, value);
    }

    public Iterator<Object> iterator(final int from, final int to)
    {
        assert from >= 0 && to <= size;

        return new Iterator<Object>()
        {
            int i = start - from;
            final int stop = start - to;

            public boolean hasNext()
            {
                return i > stop;
            }

            public Object next()
            {
                if (!hasNext())
                    throw new NoSuchElementException();

                return i--;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public ListValue apply(final Lambda f)
    {
        final PersistentList result = PersistentList.alloc(size);

        for (int index = 0, arg = start; arg > end; index++, arg--)
            result.updateUnsafe(index, f.apply(arg));

        return result;
    }

    public void run(final Lambda f)
    {
        for (int i = start; i > end; i--)
            f.apply(i);
    }

    public ListValue select(final ListValue list)
    {
        final PersistentList result = PersistentList.alloc(size);

        int i = 0;
        for (final Object item : this)
            result.updateUnsafe(i++, list.get((Integer) item));

        return result;
    }

    public ListValue select(final MapValue map)
    {
        final PersistentList result = PersistentList.alloc(size);

        int i = 0;
        for (final Object item : this)
            result.updateUnsafe(i++, map.get(item));

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
            throw new IndexOutOfBoundsException();

        return start - index;
    }

    public ListValue subList(final int from, final int to)
    {
        checkRange(from, to);
        return new ReverseIntRange(start - from, start - to);
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
}
