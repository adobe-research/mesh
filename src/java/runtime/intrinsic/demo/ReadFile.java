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

import compile.type.Type;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Demo support--simple file read
 * TODO variant return
 *
 * @author Basil Hosmer

 */
public final class ReadFile extends IntrinsicLambda
{
    public static final String NAME = "readfile";

    public static final Type TYPE = Types.fun(Types.STRING, Types.STRING);

    private static final int BUFSIZE = 1024;

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
    }

    public final String apply(final Object arg)
    {
        return invoke((String)arg);
    }

    public static String invoke(final String path)
    {
        final StringBuilder fileData = new StringBuilder(BUFSIZE);
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(path));
            char[] buf = new char[BUFSIZE];
            int numRead;
            while ((numRead = reader.read(buf)) != -1)
            {
                final String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[BUFSIZE];
            }
        }
        catch (Exception e)
        {
            return "";
        }
        finally
        {
            try
            {
                if (reader != null)
                    reader.close();
            }
            catch (Exception ignored)
            {
            }
        }

        return fileData.toString();
    }
}