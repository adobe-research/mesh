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
public interface IntrinsicsResolver
{
    /**
     * Finds an intrinsic that matches the let name, and verifies that the
     * runtime type matches the let's declared type.  Returns null and emits
     * Session.error() if verification fails or if no such intrinsic is found.
     */
    public IntrinsicLambda resolve(final LetBinding let);

    /**
     * Returns a previously resolved IntrinsicLambda.  If no such resolved
     * intrinsic is found this asserts and returns null.
     */
    public IntrinsicLambda get(final LetBinding let);
}
