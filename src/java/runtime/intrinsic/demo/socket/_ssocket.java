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

import java.io.IOException;

/**
 * Demo support.
 *
 * @author Basil Hosmer
 */
public final class _ssocket extends IntrinsicLambda
{
    public static final _ssocket INSTANCE = new _ssocket(); 
    public static final String NAME = "ssocket";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke((Integer)arg);
    }

    public static Object invoke(final int port)
    {
        try
        {
            return new java.net.ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

}
