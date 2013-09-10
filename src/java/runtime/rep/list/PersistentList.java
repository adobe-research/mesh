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

import runtime.rep.PersistentConstants;

import java.util.Iterator;

/**
 * Persistent list base class. Our subclasses are the "real"
 * lists, others are wrappers.
 * <p/>
 * Persistent lists are immutable, but facilitate copy-on-write
 * semantics by sharing internal structure with copies returned
 * by normally-mutating methods like {@link #append} and
 * {@link #update}.
 * <p/>
 * NOTE: the use of persistent data structures to support
 * copy-on-write semantics, and the use of Phil Bagwell's
 * mapped tries for their implementation, is inspired by
 * Rich Hickey's prioneering work in Clojure.
 * <p/>
 * {@link BigList} is the general implementation, others
 * take advantage of smaller data size to degenerate.
 *
 * @author Basil Hosmer
 */
public abstract class PersistentList extends AbstractListValue
    implements PersistentConstants
{
    /**
     * Empty list singleton.
     */
    public static final PersistentList EMPTY = EmptyList.INSTANCE;

    /**
     * Create a new singleton list from value.
     */
    public static PersistentList single(final Object value)
    {
        return new SingletonList(value);
    }

    /**
     * Allocates but doesn't initialize list data.
     * Chooses representation based on number of items.
     */
    public static PersistentList alloc(final int size)
    {
        if (size == 0)
            return EMPTY;

        if (size == 1)
            return new SingletonList(null);

        if (size <= NODE_SIZE)
            return new SmallList(size);

        return new BigList(size);
    }

    /**
     * Create new list, initialize items from iterator and size.
     * Chooses representation based on number of items.
     */
    public static PersistentList init(final Iterator<?> iter, final int size)
    {
        if (size == 0)
            return EMPTY;

        if (size == 1)
            return new SingletonList(iter.next());

        if (size <= NODE_SIZE)
            return new SmallList(iter, size);

        return new BigList(iter, size);
    }

    //
    // instance
    //

    /**
     * initialized on demand.
     */
    int hash = 0;

    /**
     * Append item in place, if possible.
     * Used internally when we can guarantee isolation.
     * In-place behavior is <strong>not</strong> guaranteed,
     * i.e. we may make a copy anyway.
     */
    abstract public PersistentList appendUnsafe(Object value);

    /**
     * Update item in place.
     * Used internally when we can guarantee isolation.
     * In-place behavior is guaranteed (returns self-reference
     * for convenience).
     */
    abstract public PersistentList updateUnsafe(int index, Object value);

    // Object

    @Override
    public final int hashCode()
    {
        if (hash == 0)
        {
            hash = 1;
            for (final Object obj : this)
                hash = 31 * hash + obj.hashCode();
        }

        return hash;
    }
}
