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
 * Pair of lists representing a single list.
 *
 * @author Basil Hosmer
 */
public final class ChainedListPair extends AbstractListValue
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
    private final int lsize, rsize;
    private final int size;

    private ChainedListPair(final ListValue llist, final ListValue rlist)
    {
        this.llist = llist;
        this.rlist = rlist;
        this.lsize = llist.size();
        this.rsize = rlist.size();
        this.size = this.lsize + this.rsize;
    }

    // ListValue

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

    public Iterator<Object> iterator(final int from, final int to)
    {
        return new Iterator<Object>()
        {
            int i = from;

            // note: odd i == lsize case avoids redundant alloc in next()
            Iterator<?> iter = i < lsize ? llist.iterator(i, lsize) :
                i > lsize ? rlist.iterator(i - lsize, rsize) :
                    null;

            public final boolean hasNext()
            {
                return i < to;
            }

            public final Object next()
            {
                if (i == to)
                    throw new NoSuchElementException();

                if (i == lsize)
                    iter = rlist.iterator();

                i++;

                return iter.next();
            }

            public final void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
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

    // List<Object>

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

    public ListValue subList(final int from, final int to)
    {
        return to <= lsize ? Sublist.create(llist, from, to) :
            from >= lsize ? Sublist.create(rlist, from - lsize, to - lsize) :
                Sublist.create(this, from, to);
    }
}
