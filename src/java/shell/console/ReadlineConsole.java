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

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import shell.ShellScriptManager;

import java.io.IOException;

// Uses jline2 for console implementation 
class ReadlineConsole extends Console
{
    private final ConsoleReader reader;

    ReadlineConsole(final ShellScriptManager ssm) throws IOException
    {
        this.reader = new ConsoleReader();
        this.reader.setPrompt(Console.PROMPT);
        this.reader.addCompleter(new AutoCompleter(ssm));
    }

    public String readLine() throws IOException
    {
        return reader.readLine();
    }

    public void displayPrompt()
    {
        // ConsoleReader takes care of prompting
    }

    public void setBlockMode(final boolean on)
    {
        reader.setPrompt(on ? "" : Console.PROMPT);
    }

    public void dispose()
    {
        try
        {
            TerminalFactory.get().restore();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
