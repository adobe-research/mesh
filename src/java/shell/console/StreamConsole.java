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
package shell.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class StreamConsole extends Console
{
    private final BufferedReader reader;
    private boolean blockMode;

    StreamConsole()
    {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.blockMode = false;
    }

    public String readLine() throws IOException
    {
        return reader.readLine();
    }

    public void displayPrompt()
    {
        if (!blockMode)
        {
            System.out.print(Console.PROMPT);
        }
    }

    public void setBlockMode(final boolean on)
    {
        blockMode = on;
    }

    public void dispose()
    {
    }
}
