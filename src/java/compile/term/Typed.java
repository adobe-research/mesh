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
import compile.type.Type;

/**
 * Common interface for typed objects.
 *
 * @author Basil Hosmer
 */
public interface Typed extends Located, Dumpable
{
    /**
     * Get calculated type. Null until type inference/checking.
     */
    Type getType();

    /**
     * Used by {@link compile.analyze.TypeChecker}
     */
    void setType(Type type);

    /**
     * Note: true even when declared type is partial, e.g. lambdas
     * with a partially annotated signature.
     */
    boolean hasDeclaredType();

    /**
     * Return declared type or null.
     */
    Type getDeclaredType();

    /**
     * 
     */
    void setDeclaredType(Type type);
}
