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
package compile.gen;

import compile.term.LetBinding;
import runtime.intrinsic.IntrinsicLambda;

/**
 * Service for finding intrinsics and verifying that their runtime
 * signatures match the source-declared signatures.
 *
 * Intrinsics resolvers are back-end specific. Currently
 * {@link compile.Config} calls our {@link #setImpl} method to install
 * a back-end-specific instance directly, but full back-end pluggability
 * will require that the intrinsics resolver comes bundled with e.g. a back-
 * end specific code generator and so on, all of which are still currently
 * either set separately or hard-linked.
 *
 * @author Keith McGuigan
 */
public final class IntrinsicsResolver
{
    /**
     * Back end specific implementation interface.
     */
    public interface Impl
    {
        /**
         * Looks up an intrinsic and verifies that it exists and that its
         * data type matches the signature in the let.
         *
         * On success, null is returned.  Otherwise the return value contains
         * the reason for the verification failure.
         */
        String verify(final LetBinding let);

        /**
         * Finds the previously verified intrinsic that matches the let name.
         */
        IntrinsicLambda resolve(final LetBinding let);
    }

    /**
     * Singleton implementation.
     */
    private static Impl IMPL;

    /**
     * Install a particular back-end implementation, once.
     * TODO needs to be part of a larger back-end pluggability setup, see header
     */
    public static synchronized void setImpl(final Impl impl)
    {
        assert IMPL == null;
        IMPL = impl;
    }

    /**
     * Looks up an intrinsic and verifies that it exists and that its
     * data type matches the signature in the let.
     *
     * On success, null is returned.  Otherwise the return value contains
     * the reason for the verification failure.
     */
    public static synchronized String verify(final LetBinding let)
    {
        return IMPL.verify(let);
    }

    /**
     * Finds the previously verified intrinsic that matches the let name.
     */
    public static synchronized IntrinsicLambda resolve(final LetBinding let)
    {
        return IMPL.resolve(let);
    }
}
