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
package runtime.intrinsic.demo;

import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

import java.io.IOException;
import java.net.*;

/**
 * Demo support--simple http head.
 *
 * @author Basil Hosmer
 */
public final class _httphead extends IntrinsicLambda
{
    public static final _httphead INSTANCE = new _httphead(); 
    public static final String NAME = "httphead";

    public String getName()
    {
        return NAME;
    }

    public final Object apply(final Object arg)
    {
        return invoke((String)arg);
    }

    public static ListValue invoke(final String s)
    {
        final String urlStr = s.startsWith("http://") ? s : "http://" + s;

        try
        {
            final URL url = new URI(urlStr).toURL();
            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect(); // force a connection

            ListValue result = PersistentList.EMPTY;

            for (int headerIdx = 0; true; headerIdx++)
            {
                final String headerValue = conn.getHeaderField(headerIdx);
                if (headerValue == null)
                    break;

                final String headerKey = conn.getHeaderFieldKey(headerIdx);

                result = result.append(
                    headerKey != null && !headerKey.isEmpty() ?
                        headerKey + ": " + headerValue :
                        headerValue);
            }

            return result;
        }
        catch (URISyntaxException e)
        {
            return PersistentList.EMPTY;
        }
        catch (IOException e)
        {
            return PersistentList.EMPTY;
        }
    }
}
