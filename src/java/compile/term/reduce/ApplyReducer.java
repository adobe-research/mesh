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
package compile.term.reduce;

import compile.term.Term;

/**
 * ApplyTerm reducer interface.
 * A particular implementation will reduce an applications
 * of a particular, implicit base term, e.g. {@link IntPlusReducer}
 * reduces applications of plus over ints
 *
 * @author Basil Hosmer
 */
public interface ApplyReducer
{
    /**
     * Attempt to reduce application to the given arguments.
     * Return reduced term if successful, otherwise null.
     */
    Term reduce(Term arg);
}
