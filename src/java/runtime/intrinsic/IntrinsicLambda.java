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
package runtime.intrinsic;

import runtime.rep.Lambda;

/**
 * Extends {@link runtime.rep.Lambda} with attributes used
 * to hook up intrinsics.
 *
 * @author Basil Hosmer
 */
public abstract class IntrinsicLambda implements Lambda
{
    /**
     * local used for pretty-printed signature. See {@link #setSigDump}.
     */
    private String typeParamsDump;
    private String valueParamsDump;

    /**
     * Intrinsic lambdas are declared in source with an explicit type
     * signature. Init code for the intrinsic's declaring module calls
     * us to store a string dump of the signature here, for use in
     * toString(). We return a self-reference so init code can inline
     * this call into the intrinsic binding's initializer.
     *
     * Note: synchronization is unlikely to be needed, but since our
     * current setup has a static INSTANCE per intrinsic, it's not
     * impossible, if two modules declaring the same intrinsic are
     * initialized on concurrent threads.
     */
    public synchronized IntrinsicLambda setSigDump(
        final String typeParamsDump, final String valueParamsDump)
    {
        this.typeParamsDump = typeParamsDump;
        this.valueParamsDump = valueParamsDump;

        return this;
    }

    /**
     * Function name as it appears in source code.
     */
    public abstract String getName();

    /**
     * String returned when function is printed.
     */
    public String toString()
    {
        return
            valueParamsDump == null ? "{ <intrinsic> }" :
            typeParamsDump == null ? "{ " + valueParamsDump + " => <intrinsic> }" :
            typeParamsDump + " { " + valueParamsDump + " => <intrinsic> }";
    }
}
