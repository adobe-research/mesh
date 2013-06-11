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

import java.util.Map;

/**
 * Interface for runtime values of type Map(K, V).
 *
 * Adds versioning mutator and application/selection methods
 * to {@link java.util.Map}.
 *
 * Implementations should return {@link UnsupportedOperationException}
 * for Map's ordinary mutator methods.
 *
 * @author Basil Hosmer
 */
public interface MapValue extends Map<Object, Object>
{
    /**
     * Return new version of ourselves with given association
     * added. Overwrites any existing association.
     */
    MapValue assoc(Object key, Object value);

    /**
     * Return new version of ourselves with any association
     * on given key removed.
     */
    MapValue unassoc(Object key);

    /**
     * Return new map containing our keys, associated with
     * values obtained by applying passed function to our values.
     */
    MapValue apply(Lambda f);

    /**
     * Return new map containing our keys, associated with
     * values obtained by selecting items from the given list,
     * using our values as indexes.
     */
    MapValue select(final ListValue list);

    /**
     * Return new map containing our keys, associated with
     * values obtained by selecting items from the given map,
     * using our values as keys.
     */
    MapValue select(final MapValue map);
}
