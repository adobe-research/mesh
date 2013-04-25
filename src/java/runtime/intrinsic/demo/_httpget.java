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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Demo support--super simple http get
 * TODO variant return
 *
 * @author Basil Hosmer
 */
public final class _httpget extends IntrinsicLambda
{
    public static final _httpget INSTANCE = new _httpget(); 
    public static final String NAME = "httpget";

    public String getName()
    {
        return NAME;
    }

    public final String apply(final Object arg)
    {
        return invoke((String)arg);
    }

    public static String invoke(final String s)
    {
        final String urlStr = s.startsWith("http://") ? s : "http://" + s;

        try
        {
            final URL url = new URI(urlStr).toURL();
            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect(); // force a connection

            final InputStream in = conn.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            final StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                buffer.append(line);

            reader.close();
            return buffer.toString();
        }
        catch (URISyntaxException e)
        {
            return "";
        }
        catch (Exception e)
        {
            System.out.println("" + Thread.currentThread().getId() +
                " httpget " + urlStr + ": " + e.getClass().getSimpleName() + " " +
                e.getMessage());

            return "";
        }
    }
}
