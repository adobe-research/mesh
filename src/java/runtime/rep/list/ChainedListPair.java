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
 * Pair of lists representing a single list.
 *
 * @author Basil Hosmer
 */
public final class ChainedListPair implements ListValue
{
    /**
     * flattened list from list pair
     */
    public static ListValue create(final ListValue llist, final ListValue rlist)
    {
        return llist.size() == 0 ? rlist :
            rlist.size() == 0 ? llist :
            new ChainedListPair(llist, rlist);
    }

    //
    // instance
    //

    private final ListValue llist, rlist;
    private final int lsize;
    private final int size;

    private ChainedListPair(final ListValue llist, final ListValue rlist)
    {
        this.llist = llist;
        this.rlist = rlist;
        this.lsize = llist.size();
        this.size = this.lsize + rlist.size();
    }

    public int size()
    {
        return size;
    }

    public Object get(final int index)
    {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        return index < lsize ? llist.get(index) : rlist.get(index - lsize);
    }

    public int find(final Object value)
    {
        final int lfind = llist.find(value);
        return lfind < lsize ? lfind : lsize + rlist.find(value);
    }

    public ListValue append(final Object value)
    {
        return new ChainedListPair(llist, rlist.append(value));
    }

    public ListValue update(final int index, final Object value)
    {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        return index < lsize ?
            new ChainedListPair(llist.update(index, value), rlist) :
            new ChainedListPair(llist, rlist.update(index - lsize, value));
    }

    public ListValue subList(final int from, final int to)
    {
        return to <= lsize ? Sublist.create(llist, from, to) :
            from >= lsize ? Sublist.create(rlist, from - lsize, to - lsize) :
            Sublist.create(this, from, to);
    }

    public ListValue apply(final Lambda f)
    {
        return new ChainedListPair(llist.apply(f), rlist.apply(f));
    }

    public void run(final Lambda f)
    {
        llist.run(f);
        rlist.run(f);
    }

    public ListValue select(final ListValue base)
    {
        return new ChainedListPair(llist.select(base), rlist.select(base));
    }

    public ListValue select(final MapValue map)
    {
        return new ChainedListPair(llist.select(map), rlist.select(map));
    }

    // Iterable

    public Iterator<Object> iterator()
    {
        return iterator(0, size);
    }

    public Iterator<Object> iterator(final int from, final int to)
    {
        return new Iterator<Object>()
        {
            int i = from;
            Iterator<?> iter = i < lsize ?
                llist.iterator(i, llist.size()) :
                rlist.iterator(i - lsize, rlist.size());

            public boolean hasNext()
            {
                return i < to;
            }

            public Object next()
            {
                if (!hasNext())
                    throw new NoSuchElementException();

                if (i == lsize)
                    iter = rlist.iterator();

                i++;

                return iter.next();
            }

            public void remove()
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
