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
package runtime.rep.lambda;

import compile.term.ValueBinding;
import compile.type.Type;
import compile.type.visit.TypeDumper;

/**
 * Extends {@link Lambda} with attributes used
 * to hook up intrinsics.
 *
 * @author Basil Hosmer
 */
public abstract class IntrinsicLambda implements Lambda
{
    /**
     * Function name as it appears in source code.
     */
    public abstract String getName();

    /**
     * String returned when function is printed.
     */
    public String toString()
    {
        return "{ <intrinsic> }";
    }
}
