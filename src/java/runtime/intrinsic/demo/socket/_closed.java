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
package runtime.intrinsic.demo.socket;

import runtime.intrinsic.demo.ServerSocket;
import runtime.rep.lambda.IntrinsicLambda;

/**
 * Demo support.
 *
 * @author Basil Hosmer
 */
public final class _closed extends IntrinsicLambda
{
    public static final _closed INSTANCE = new _closed(); 
    public static final String NAME = "closed";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke(arg);
    }

    public static boolean invoke(final Object obj)
    {
        return ((java.net.ServerSocket)obj).isClosed();
    }
}
