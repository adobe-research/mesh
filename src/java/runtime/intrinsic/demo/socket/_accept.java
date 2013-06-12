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

import compile.Session;
import runtime.intrinsic.IntrinsicLambda;
import runtime.rep.Lambda;
import runtime.rep.Tuple;

import java.io.*;
import java.net.Socket;

/**
 * Demo support.
 *
 * @author Basil Hosmer
 */
public final class _accept extends IntrinsicLambda
{
    public static final _accept INSTANCE = new _accept(); 
    public static final String NAME = "accept";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke(args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Object arg0, final Lambda handler)
    {
        final java.net.ServerSocket serverSocket = (java.net.ServerSocket)arg0;

        try
        {
            final Socket requestSocket = serverSocket.accept();

            final InputStream is = requestSocket.getInputStream();
            final DataOutputStream os =
                new DataOutputStream(requestSocket.getOutputStream());

            final StringBuilder sb = new StringBuilder();
            final Reader reader = new InputStreamReader(is);
            final int BUFSIZE = 8192;
            final char chars[] = new char[BUFSIZE];
            int bytesRead;
            do
            {
                bytesRead = reader.read(chars, 0, BUFSIZE);
                if (bytesRead > 0)
                    sb.append(chars, 0, bytesRead);
            }
            while (bytesRead == BUFSIZE &&
                (chars[BUFSIZE - 1] != '\n' || chars[BUFSIZE - 2] != '\n'));

            final String in = sb.toString();

            if (Session.isDebug())
                Session.debug("read {0}", in);

            final String out = (String)handler.apply(in);

            os.writeUTF(out);
            os.flush();

            is.close();
            os.close();
            requestSocket.close();

        }
        catch (IOException e)
        {
            if (!e.getMessage().toLowerCase().equals("socket closed"))
                System.out.println(e);
        }

        return Tuple.UNIT;
    }
}
