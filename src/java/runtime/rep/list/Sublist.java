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

import runtime.sys.Logging;
import runtime.rep.Lambda;
import runtime.rep.map.MapValue;

import java.util.Iterator;

/**
 * Position-constrained view on a backing list.
 *
 * @author Basil Hosmer
 */
final class Sublist extends AbstractListValue
{
    /**
     * Percentage of backing list viewed by sublist.
     * Used to decide whether or not to devirtualize
     * on a (sub-)sublist request.
     */
    public static final int SUBLIST_PCT_COPY_THRESHOLD = 64;

    /**
     * Total backing list size.
     * Used to decide whether or not to devirtualize
     * on a (sub-)sublist request.
     */
    public static final int LIST_SIZE_COPY_THRESHOLD = 32767;

    /**
     * factory, diverts some degenerate cases.
     * TODO should make the same decision as {@link #subList}
     */
    public static ListValue create(final ListValue list, final int from, final int to)
    {
        assert from >= 0 && from <= to && to <= list.size();

        return from == to ? PersistentList.EMPTY :
            (from == 0 && to == list.size()) ? list :
            new Sublist(list, from, to);
    }

    //
    // instance
    //

    private final ListValue list;
    private final int from;
    private final int to;

    private Sublist(final ListValue list, final int from, final int to)
    {
        this.list = list;
        this.from = from;
        this.to = to;
    }

    // ListValue

    public int find(final Object value)
    {
        final int n = size();

        int i = 0;

        while (i < n && !get(i).equals(value))
            i++;

        return i;
    }

    public ListValue append(final Object value)
    {
        return ChainedListPair.create(this, PersistentList.single(value));
    }

    public ListValue update(final int index, final Object value)
    {
        if (from + index >= to)
            throw new IndexOutOfBoundsException();

        return new Sublist(list.update(from + index, value), from, to);
    }

    public Iterator<Object> iterator(final int from, final int to)
    {
        return list.iterator(this.from + from, this.from + to);
    }

    public ListValue apply(final Lambda f)
    {
        final PersistentList result = PersistentList.alloc(size());

        int i = 0;
        for (final Object item : this)
            result.updateUnsafe(i++, f.apply(item));

        return result;
    }

    public void run(final Lambda f)
    {
        for (final Object item : this)
            f.apply(item);
    }

    public ListValue select(final ListValue list)
    {
        final PersistentList result = PersistentList.alloc(size()) ;

        int i = 0;
        for (final Object item : this)
            result.updateUnsafe(i++, list.get((Integer) item));

        return result;
    }

    public ListValue select(final MapValue map)
    {
        final PersistentList result = PersistentList.alloc(size());

        int i = 0;
        for (final Object item : this)
            result.updateUnsafe(i++, map.get(item));

        return result;
    }

    // List<Object>

    public int size()
    {
        return to - from;
    }

    public Object get(final int index)
    {
        if (index < 0 || index >= to - from)
            throw new IndexOutOfBoundsException();

        return list.get(from + index);
    }

    /**
     * note the copy when we're using too little of a big backing list
     * TODO move logic to {@link BigList#subList}
     */
    public ListValue subList(final int from, final int to)
    {
        checkRange(from, to);

        if (to == from)
            return PersistentList.EMPTY;

        final ListValue sub = (ListValue)list.subList(this.from + from, this.from + to);
        final int subsize = sub.size();
        final int listsize = list.size();

        if (listsize / subsize > SUBLIST_PCT_COPY_THRESHOLD &&
            listsize >= LIST_SIZE_COPY_THRESHOLD)
        {
            Logging.debug(
                "SubList.subList, copy threshold exceeded, copying (size = {0}, subsize = {1})",
                listsize, subsize);

            return PersistentList.init(sub.iterator(), subsize);
        }
        else
        {
            return sub;
        }
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
