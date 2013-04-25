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

import compile.type.Type;
import compile.type.Types;
import runtime.intrinsic.demo.ServerSocket;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Tuple;

import java.io.IOException;

/**
 * Demo support.
 *
 * @author Basil Hosmer
 */
public final class Close extends IntrinsicLambda
{
    public static final String NAME = "close";

    public static final Type TYPE =
        Types.fun(ServerSocket.INSTANCE.getType(), Types.unit());

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
    }

    public Object apply(final Object arg)
    {
        return invoke(arg);
    }

    public static Tuple invoke(final Object obj)
    {
        final java.net.ServerSocket socket = (java.net.ServerSocket)obj;
        try
        {
            if (!socket.isClosed())
                socket.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        return Tuple.UNIT;
    }
}
