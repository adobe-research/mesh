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
package runtime;

import runtime.rep.lambda.IntrinsicLambda;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Used for recording types from intrinsic lets to be later used
 * by IntrinsicLambda.toString()
 *
 * @author Keith McGuigan
 */
public final class IntrinsicTypeRecorder
{
    private static final Map<IntrinsicLambda, String> types =
        new IdentityHashMap<IntrinsicLambda, String>();

    public static IntrinsicLambda record(
        final IntrinsicLambda lambda, final String type)
    {
        types.put(lambda, type);
        return lambda;
    }

    public static String retrieve(final IntrinsicLambda lambda)
    {
        return types.get(lambda);
    }
}
