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

import runtime.rep.lambda.IntrinsicLambda;
import compile.term.LetBinding;

/**
 * Find intrinsics verify that thier runtime signatures match the
 * source-declared signatures.
 *
 * @author Keith McGuigan
 */
public abstract class IntrinsicsResolver
{
    public static abstract class Factory
    {
        public abstract IntrinsicsResolver create();
    }

    public static Factory factory;

    private static final ThreadLocal<IntrinsicsResolver> LOCAL =
        new ThreadLocal<IntrinsicsResolver>()
        {
            protected IntrinsicsResolver initialValue()
            {
                return factory.create();
            }
        };

    public static IntrinsicsResolver getThreadLocal()
    {
        return LOCAL.get();
    }

    /**
     * Finds an intrinsic that matches the let name, and verifies that the
     * runtime type matches the let's declared type.  If this returns null,
     * then getErrorMessage can be called to indicate why.
     */
    public abstract IntrinsicLambda resolve(final LetBinding let);

    /**
     * If the last resolve call failed, this will return an error message
     * indicating why.  If the last resolve call succeeded, this returns null.
     */
    public abstract String getErrorMessage();
}
