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

import runtime.rep.PersistentConstants;
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A submap node is a complete hashmap whose entries
 * share a common key hash prefix, which locates the
 * submap in the trie.
 *
 * @author Basil Hosmer
 */
final class SubmapNode extends MapNode implements PersistentConstants
{
    /**
     * total number of entries under us
     */
    private int size;

    /**
     * each bit represents the hash slice whose value is the bit's position,
     * with bits set to 1 where a child is present
     */
    private int bits;

    /**
     * array of child nodes, 1 for each 1 bit in {@link #bits}. packed
     */
    private MapNode[] children;

    /**
     * cached hash code
     */
    private int hash;

    /**
     * create a new map node with a single child bucket.
     */
    SubmapNode(final int depth, final BucketNode bucket)
    {
        this(bucket.size(), toBit(bucket.keyHash, depth), new MapNode[]{bucket});
    }

    /**
     * create a new map node with a single child entry.
     */
    SubmapNode(final int depth, final SingleEntryNode entry)
    {
        this(entry.size(), toBit(entry.keyHash, depth), new MapNode[]{entry});
    }

    /**
     * create a new map node with the given children. hashes and children
     * must agree, i.e. hashes must have the same number of 1 bits as children
     * has items, and the 1 bit positions must correspond to the hash slices
     * of the children.
     */
    SubmapNode(final int size, final int bits, final MapNode[] children)
    {
        this.size = size;
        this.bits = bits;
        this.children = children;
        this.hash = -1;
    }

    public int size()
    {
        return size;
    }

    /**
     * add a new association. depth must be the current depth of this node,
     * hash must be Hash.invoke(key). mutate determines whether node can be
     * modified in-place.
     */
    public MapNode add(final int depth, final int keyHash, final int hashPath,
        final Object key, final Object val, final boolean mutate)
    {
        final int bit = 1 << (hashPath & PATH_MASK);
        final int pos = toPos(bit);

        if ((bits & bit) == 0)
        {
            // we have nothing at this hash bit - insert new entry into packed array
            final int nkids = children.length;
            final MapNode[] newkids = new MapNode[nkids + 1];

            newkids[pos] = new SingleEntryNode(keyHash, key, val);

            System.arraycopy(children, 0, newkids, 0, pos);
            System.arraycopy(children, pos, newkids, pos + 1, nkids - pos);

            return new SubmapNode(size + 1, bits | bit, newkids);
        }
        else
        {
            // we have a child for this hash bit, send child down to it
            final MapNode child = children[pos];
            final int oldSize = child.size();
            final MapNode added = child.add(depth + 1,
                keyHash, hashPath >>> PATH_BITS, key, val, mutate);

            if (mutate)
            {
                size = size() - oldSize + added.size();

                if (added != child)
                    children[pos] = added;

                this.hash = -1;

                return this;
            }
            else
            {
                if (added == child)
                    // if we're not mutating, this means no change
                    return this;

                // otherwise, return new version with modified child
                final MapNode[] newkids = children.clone();
                newkids[pos] = added;

                return new SubmapNode(size - oldSize + added.size(), bits, newkids);
            }
        }
    }

    public MapNode remove(final int keyHash, final int hashPath, final Object key)
    {
        final int bit = 1 << (hashPath & PATH_MASK);

        if ((bits & bit) == 0)
        {
            // we have nothing at this hash position
            return this;
        }
        else
        {
            // we have a child at this hash position, send deletion down
            final int off = toPos(bit);
            final MapNode child = children[off];
            final MapNode deleted = children[off].remove(
                keyHash, keyHash >>> PATH_BITS, key);

            if (deleted == null)
            {
                // deletion left child empty
                final int remains = bits & ~bit;

                if (remains == 0)
                {
                    // that was our only child, so now we're empty as well
                    return null;
                }
                else
                {
                    // we have other kids, return new version with this child removed
                    final MapNode[] newkids = new MapNode[children.length - 1];
                    System.arraycopy(children, 0, newkids, 0, off);
                    System.arraycopy(children, off + 1, newkids, off,
                        newkids.length - off);
                    return new SubmapNode(size - 1, remains, newkids);
                }
            }
            else if (deleted == child)
            {
                // deletion left child unchanged
                return this;
            }
            else
            {
                // deletion modified child but left it nonempty
                final MapNode[] newkids = children.clone();
                newkids[off] = deleted;

                return new SubmapNode(size - 1, bits, newkids);
            }
        }
    }

    public Object get(final int keyHash, final int hashPath, final Object key)
    {
        final int bit = 1 << (hashPath & PATH_MASK);

        return (bits & bit) == 0 ? null :
            children[toPos(bit)].get(keyHash,
                hashPath >>> PersistentConstants.PATH_BITS, key);
    }

    public MapNode apply(final Lambda f)
    {
        final int n = children.length;
        final MapNode[] newkids = new MapNode[n];

        for (int i = 0; i < n; i++)
            newkids[i] = children[i].apply(f);

        return new SubmapNode(size, bits, newkids);
    }

    public MapNode select(final ListValue list)
    {
        final int n = children.length;
        final MapNode[] newkids = new MapNode[n];

        for (int i = 0; i < n; i++)
            newkids[i] = children[i].select(list);

        return new SubmapNode(size, bits, newkids);
    }

    public MapNode select(final MapValue map)
    {
        final int n = children.length;
        final MapNode[] newkids = new MapNode[n];

        for (int i = 0; i < n; i++)
            newkids[i] = children[i].select(map);

        return new SubmapNode(size, bits, newkids);
    }

    public Iterator<Map.Entry<Object, Object>> entryIterator()
    {
        // NOTE: taking heavy advantage of guaranteed non-emptiness of self and children
        return new Iterator<Map.Entry<Object, Object>>()
        {
            private int i = 1;
            private Iterator<Map.Entry<Object, Object>> sub =
                children[0].entryIterator();

            public boolean hasNext()
            {
                return i < children.length || sub.hasNext();
            }

            public Map.Entry<Object, Object> next()
            {
                if (sub.hasNext())
                {
                    return sub.next();
                }
                else if (i < children.length)
                {
                    sub = children[i++].entryIterator();
                    return sub.next();
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * extract the 5-bit hash slice for this depth
     */
    private static int slice(final int hash, final int depth)
    {
        return (hash >>> (depth * PATH_BITS)) & PATH_MASK;
    }

    /**
     * hash slice for each depth will be a 5-bit number.
     * return a bitfield with the corresponding bit set.
     */
    private static int toBit(final int hash, final int depth)
    {
        return 1 << slice(hash, depth);
    }

    /**
     * return the position of a given bit.
     * input should have a single bit set.
     */
    private int toPos(final int bit)
    {
        return Integer.bitCount(bits & (bit - 1));
    }

    // Object

    /**
     * Note: order-independent
     */
    @Override
    public int hashCode()
    {
        if (hash == -1)
        {
            int result = 0;

            for (final Object child : children)
                result += child.hashCode();

            hash = result;
        }

        return hash;
    }
}
