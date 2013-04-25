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
package compile.term;

import compile.Dumpable;
import compile.Located;

/**
 * A statement can either be a binding or a plain term.
 *
 * @author Basil Hosmer
 */
public interface Statement extends Located, Dumpable
{
    /**
     * True if we're an instance of {@link Binding}
     */
    boolean isBinding();
}
