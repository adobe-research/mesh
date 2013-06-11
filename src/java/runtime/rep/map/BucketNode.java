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
import runtime.rep.list.ListValue;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Bucket node - {@link MapNode} for multiple entries
 * with a common hash.
 *
 * @author Basil Hosmer
 */
final class BucketNode extends MapNode
{
    /**
     * array of entries.
     */
    private final SingleEntryNode[] entries;

    /**
     * cached key hash. package local
     */
    final int keyHash;

    /**
     * cached hash code, computed once by hashCode()
     */
    private int hash;

    public BucketNode(final SingleEntryNode[] entries)
    {
        assert entries.length > 1 : "empty or singleton bucket";
        this.entries = entries;
        this.keyHash = entries[0].keyHash;
        this.hash = -1;
    }

    public int size()
    {
        return entries.length;
    }

    public MapNode add(final int depth, final int keyHash, final int hashPath,
        final Object key, final Object value, final boolean mutate)
    {
        if (keyHash != this.keyHash)
            // different hash, this node becomes a submap
            return new SubmapNode(depth, this).
                add(depth, keyHash, hashPath, key, value, mutate);

        // same hash, node remains a bucket
        final int i = indexOf(key);
        if (i == -1)
        {
            // newly encountered key, add to bucket
            final int length = entries.length;
            final SingleEntryNode[] newEntries = new SingleEntryNode[length + 1];
            System.arraycopy(entries, 0, newEntries, 0, length);
            newEntries[length] = new SingleEntryNode(keyHash, key, value);
            return new BucketNode(newEntries);
        }
        else if (entries[i].getValue() != value)
        {
            // old key but new value, overwrite current entry
            final SingleEntryNode[] newEntries = mutate ? entries : entries.clone();
            newEntries[i] = new SingleEntryNode(keyHash, key, value);
            return new BucketNode(newEntries);
        }
        else
        {
            // already have this entry
            return this;
        }
    }

    public MapNode remove(final int keyHash, final int hashPath,
        final Object key)
    {
        // not our hash
        if (keyHash != this.keyHash)
            return this;

        final int i = indexOf(key);
        if (i == -1)
            return this;

        if (entries.length == 2)
            return i == 0 ? entries[1] : entries[0];

        // return cloned bucket sans this entry
        final SingleEntryNode[] newEntries = new SingleEntryNode[entries.length - 1];
        System.arraycopy(entries, 0, newEntries, 0, i);
        System.arraycopy(entries, i + 1, newEntries, i, newEntries.length - i);
        return new BucketNode(newEntries);
    }

    public Object get(final int keyHash, final int hashPath,
        final Object key)
    {
        final int i = indexOf(key);
        return i >= 0 ? entries[i].getValue() : null;
    }

    public MapNode apply(final Lambda f)
    {
        final int nents = entries.length;
        final SingleEntryNode[] newEntries = new SingleEntryNode[nents];

        for (int i = 0; i < nents; i++)
            newEntries[i] = entries[i].apply(f);

        return new BucketNode(newEntries);
    }

    public MapNode select(final ListValue list)
    {
        final int nents = entries.length;
        final SingleEntryNode[] newEntries = new SingleEntryNode[nents];

        for (int i = 0; i < nents; i++)
            newEntries[i] = entries[i].select(list);

        return new BucketNode(newEntries);
    }

    public MapNode select(final MapValue map)
    {
        final int nents = entries.length;
        final SingleEntryNode[] newEntries = new SingleEntryNode[nents];

        for (int i = 0; i < nents; i++)
            newEntries[i] = entries[i].select(map);

        return new BucketNode(newEntries);
    }

    public Iterator<Map.Entry<Object, Object>> entryIterator()
    {
        return new Iterator<Map.Entry<Object, Object>>()
        {
            private int i = 0;
            private final int n = size();

            public boolean hasNext()
            {
                return i < n;
            }

            public Map.Entry<Object, Object> next()
            {
                if (i == n)
                    throw new NoSuchElementException();

                return entries[i++];
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private int indexOf(final Object key)
    {
        for (int i = 0; i < entries.length; i++)
            if (entries[i].getKey().equals(key))
                return i;

        return -1;
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
            int result = keyHash;

            for (final SingleEntryNode entry : entries)
            {
                final Object value = entry.getValue();
                result += value != null ? value.hashCode() : 0;
            }

            hash = result;
        }

        return hash;
    }
}
