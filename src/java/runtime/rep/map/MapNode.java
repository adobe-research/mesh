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

/**
 * Map node base class.
 *
 * @author Basil Hosmer
 */
abstract class MapNode
{
    /**
     * number of entries under this node
     */
    abstract int size();

    /**
     * return a new version of this <strong>top level</strong> node,
     * with the given association added. Mutate flag controls in-place
     * updating.
     */
    MapNode add(final Object key, final Object val, final boolean mutate)
    {
        final int keyHash = key.hashCode();
        return add(0, keyHash, keyHash, key, val, mutate);
    }

    /**
     * return a new version of this node, with the given association added.
     * Node must be at the given depth. Hash must be Hash.invoke(key), passed
     * explicitly to avoid recalc. Mutate flag controls in-place updating.
     */
    abstract MapNode add(final int depth, int keyHash, int hashPath,
        Object key, Object val, boolean mutate);

    /**
     * return a new version of this <strong>top level</strong> node,
     * with the given association removed.
     */
    MapNode remove(final Object key)
    {
        final int keyHash = key.hashCode();
        return remove(keyHash, keyHash, key);
    }

    /**
     * return a new version of this node, with the given association removed.
     * Node must be at the given depth. Hash must be Hash.invoke(key), passed
     * explicitly to avoid recalc.
     */
    abstract MapNode remove(int keyHash, int hashPath, Object key);

    /**
     * find the value associated with given key under this <strong>top-level</strong> node.
     */
    Object get(final Object key)
    {
        final int keyHash = key.hashCode();
        return get(keyHash, keyHash, key);
    }

    /**
     * find an entry with this key under this node. node must be at the given
     * depth. Hash must be key.hashCode(), passed explicitly to avoid recalc.
     */
    abstract Object get(int keyHash, int hashPath, Object key);

    /**
     * apply lambda to all entries under this node
     */
    abstract MapNode apply(final Lambda f);

    /**
     * select into list using all entries under this node
     */
    abstract MapNode select(final ListValue list);

    /**
     * select into map using all entries under this node
     */
    abstract MapNode select(final MapValue map);

    /**
     * iterator over all entries under this node
     */
    abstract Iterator<Map.Entry<Object, Object>> entryIterator();
}
