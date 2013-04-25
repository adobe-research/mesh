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
package runtime.rep;

/**
 * constants used by persistent collections
 */
public interface PersistentConstants
{
    /**
     * Number of items per node.
     */
    int NODE_SIZE = 32;

    /**
     * Mask for a single entry in an index viewed as a path.
     */
    int PATH_MASK = NODE_SIZE - 1;

    /**
     * Number of bits in a node-sized chunk of an index.
     */
    int PATH_BITS = Integer.numberOfLeadingZeros(0) -
        Integer.numberOfLeadingZeros(PATH_MASK);
}
