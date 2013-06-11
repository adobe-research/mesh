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
package runtime.rep.map;

import runtime.rep.Lambda;
import runtime.rep.PersistentConstants;
import runtime.rep.list.ListValue;

import java.util.*;

/**
 * Persistent map class. Immutable, facilitates copy-on-write
 * semantics by sharing internal structure with copies returned
 * by normally-mutating methods like {@link #assoc} and
 * {@link #remove}.
 *
 * NOTE: the use of persistent data structures to support
 * copy-on-write semantics, and the use of Phil Bagwell's
 * mapped tries for their implementation, is inspired by
 * Rich Hickey's prioneering work in Clojure.
 *
 * @author Basil Hosmer
 */
public final class PersistentMap implements MapValue, PersistentConstants
{
    /**
     * All versioned maps grow from the empty map, except when created unsafely
     * (see {@link #fresh}).
     */
    public static final PersistentMap EMPTY = fresh();

    /**
     * Create a new empty map. This entry point is used by internals that build
     * up a private map by appending unsafely; see e.g. {@link #assocUnsafe}.
     */
    public static PersistentMap fresh()
    {
        return new PersistentMap(null);
    }

    /**
     * return new version of map with new association added
     */
    public static PersistentMap single(final Object key, final Object val)
    {
        return new PersistentMap(new SingleEntryNode(key.hashCode(), key, val));
    }

    //
    // instance
    //

    /**
     * root map node, null for empty map
     */
    private MapNode root;

    /**
     * private constructor
     */
    private PersistentMap(final MapNode root)
    {
        this.root = root;
    }

    // MapValue

    public final PersistentMap assoc(final Object key, final Object val)
    {
        if (root == null)
            return new PersistentMap(new SingleEntryNode(key.hashCode(), key, val));

        final MapNode added = root.add(key, val, false);

        return added == root ? this : new PersistentMap(added);
    }

    /**
     * UNSAFE - assoc in place, if possible. used internally when
     * we can guarantee no aliases (including in codegen)
     */
    public PersistentMap assocUnsafe(final Object key, final Object val)
    {
        if (root == null)
        {
            root = new SingleEntryNode(key.hashCode(), key, val);
        }
        else
        {
            root = root.add(key, val, true);
        }

        return this;
    }

    public PersistentMap unassoc(final Object key)
    {
        final MapNode deleted = root == null ? null : root.remove(key);

        return deleted == null ? EMPTY :
            deleted == root ? this : new PersistentMap(deleted);
    }

    // java.util.Map

    public Object get(final Object key)
    {
        return root == null ? null : root.get(key);
    }

    public int size()
    {
        return root == null ? 0 : root.size();
    }

    public boolean isEmpty()
    {
        return root == null;
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(final Object key)
    {
        return get(key) != null;
    }

    public boolean containsValue(final Object value)
    {
        return values().contains(value);
    }

    public Set<Map.Entry<Object, Object>> entrySet()
    {
        return new AbstractSet<Map.Entry<Object, Object>>()
        {
            public Iterator<Map.Entry<Object, Object>> iterator()
            {
                return PersistentMap.this.entryIterator();
            }

            public int size()
            {
                return PersistentMap.this.size();
            }

            public int hashCode()
            {
                return PersistentMap.this.hashCode();
            }

            public boolean contains(final Object obj)
            {
                if (!(obj instanceof Map.Entry))
                    return false;

                final Map.Entry<?,?> e = (Map.Entry<?,?>)obj;
                final Object v = PersistentMap.this.get(e.getKey());

                return v != null && v.equals(e.getValue());
            }
        };
    }

    public Set<Object> keySet()
    {
        return new AbstractSet<Object>()
        {
            @Override
            public Iterator<Object> iterator()
            {
                final Iterator<Map.Entry<Object, Object>> iterator =
                    PersistentMap.this.entryIterator();

                return new Iterator<Object>()
                {
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    public Object next()
                    {
                        return iterator.next().getKey();
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size()
            {
                return PersistentMap.this.size();
            }

            @Override
            public boolean contains(final Object o)
            {
                return PersistentMap.this.containsKey(o);
            }
        };
    }

    private Iterator<Map.Entry<Object, Object>> entryIterator()
    {
        if (root == null)
        {
            return new Iterator<Map.Entry<Object, Object>>()
            {
                public boolean hasNext()
                {
                    return false;
                }

                public Map.Entry<Object, Object> next()
                {
                    throw new NoSuchElementException();
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }
        else
        {
            return root.entryIterator();
        }
    }

    public Object put(final Object key, final Object value)
    {
        throw new UnsupportedOperationException();
    }

    public void putAll(final Map<?,?> t)
    {
        throw new UnsupportedOperationException();
    }

    public Object remove(final Object key)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<Object> values()
    {
        return new AbstractCollection<Object>()
        {
            public Iterator<Object> iterator()
            {
                final Iterator<Map.Entry<Object, Object>> iterator =
                    PersistentMap.this.entryIterator();

                return new Iterator<Object>()
                {
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    public Object next()
                    {
                        return iterator.next().getValue();
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public int size()
            {
                return PersistentMap.this.size();
            }
        };
    }

    public PersistentMap apply(final Lambda f)
    {
        return new PersistentMap(root.apply(f));
    }

    public PersistentMap select(final ListValue list)
    {
        return new PersistentMap(root.select(list));
    }

    public PersistentMap select(final MapValue map)
    {
        return new PersistentMap(root.select(map));
    }

    // Object

    public boolean equals(final Object obj)
    {
        if (obj == this)
            return true;

        if (!(obj instanceof Map))
            return false;

        final Map<?,?> map = (Map<?,?>)obj;

        if (map.size() != size())
            return false;

        for (final Map.Entry<?,?> e : entrySet())
        {
            final Object v = map.get(e.getKey());

            if (v == null || !v.equals(e.getValue()))
                return false;
        }

        return true;
    }

    public int hashCode()
    {
        return root == null ? 0 : root.hashCode();
    }
}