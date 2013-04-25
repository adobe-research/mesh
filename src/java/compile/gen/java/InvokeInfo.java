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
package compile.gen.java;

import compile.term.LambdaTerm;

/**
 * Packages information about how to generate calls to invoke().
 *
 * @author Basil Hosmer
 */
public class InvokeInfo
{
    enum InvokeMode
    {
        Scatter, NoScatter
    }

    /**
     * lambda being invoked, null if intrinsic
     */
    public final LambdaTerm lambda;

    /**
     * says whether to scatter arguments
     */
    public final InvokeMode mode;

    /**
     * if non-null, static call to invoke() through classname is ok
     */
    public final String className;

    /**
     *
     */
    InvokeInfo(final LambdaTerm lambda, final InvokeMode mode, final String fullName)
    {
        this.lambda = lambda;
        this.mode = mode;
        this.className = fullName;
    }
}
