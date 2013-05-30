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
     * Looks up an intrinsic and verifies that it exists and that its
     * data type matches the signature in the let.
     *
     * On success, null is returned.  Otherwise the return value contains
     * the reason for the verification failure.
     */
    public abstract String verify(final LetBinding let);

    /**
     * Finds the previously verified intrinsic that matches the let name.
     */
    public abstract IntrinsicLambda resolve(final LetBinding let);
}
