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
 * single-entry map node
 *
 * @author Basil Hosmer
 */
final class SingleEntryNode extends MapNode implements Map.Entry<Object, Object>
{
    /**
     * cached key hash. package local
     */
    final int keyHash;

    /**
     * entry key
     */
    private final Object key;

    /**
     * entry value
     */
    private final Object value;

    /**
     * keyHash should be equal to key.hashCode(). package local
     */
    SingleEntryNode(final int keyHash, final Object key, final Object value)
    {
        this.keyHash = keyHash;
        this.key = key;
        this.value = value;
    }

    // MapNode

    public int size()
    {
        return 1;
    }

    public MapNode add(final int depth, final int keyHash, final int hashPath,
        final Object key, final Object value, final boolean mutate)
    {
        // if different hashes, split into a submap
        if (keyHash != this.keyHash)
            return new SubmapNode(depth, this).
                add(depth, keyHash, hashPath, key, value, mutate);

        // if same keyHash but different keys, make a bucket
        if (!key.equals(this.key))
            return new BucketNode(
                new SingleEntryNode[]{this, new SingleEntryNode(keyHash, key, value)});

        // if same key but different values, replace single entry
        if (value != this.value)
            return new SingleEntryNode(keyHash, key, value);

        // otherwise, key and value are identical
        return this;
    }

    public MapNode remove(final int keyHash, final int hashPath, final Object key)
    {
        return keyHash == this.keyHash && key.equals(this.key) ? null : this;
    }

    public Object get(final int keyHash, final int hashPath, final Object key)
    {
        return keyHash == this.keyHash && key.equals(this.key) ? value : null;
    }

    public SingleEntryNode apply(final Lambda f)
    {
        return new SingleEntryNode(keyHash, key, f.apply(value));
    }

    public SingleEntryNode select(final ListValue list)
    {
        return new SingleEntryNode(keyHash, key, list.get((Integer)value));
    }

    public SingleEntryNode select(final MapValue map)
    {
        return new SingleEntryNode(keyHash, key, map.get(value));
    }

    public Iterator<Map.Entry<Object, Object>> entryIterator()
    {
        return new Iterator<Map.Entry<Object, Object>>()
        {
            private boolean done = false;

            public boolean hasNext()
            {
                return !done;
            }

            public Map.Entry<Object, Object> next()
            {
                if (done)
                    throw new NoSuchElementException();

                done = true;
                return SingleEntryNode.this;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    // Map.Entry

    public Object getKey()
    {
        return key;
    }

    public Object getValue()
    {
        return value;
    }

    public Object setValue(final Object value)
    {
        throw new UnsupportedOperationException();
    }

    // Object

    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof Map.Entry)
        {
            final Map.Entry<?,?> entry = (Map.Entry<?,?>)obj;

            return key.equals(entry.getKey()) &&
                value.equals(entry.getValue());
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return 31 * keyHash + (value != null ? value.hashCode() : 0);
    }
}
