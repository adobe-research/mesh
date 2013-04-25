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
package compile;

/**
 * Common interface for dumpable objects.
 *
 * @author Basil Hosmer
 */
public interface Dumpable
{
    /**
     * return a descriptive (source-like) string representation of the type.
     */
    String dump();
}
