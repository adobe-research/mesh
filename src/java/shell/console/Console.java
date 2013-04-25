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

import shell.FileServices;
import shell.ShellConfig;
import shell.ShellScriptManager;

import java.io.IOException;
import java.util.List;

public abstract class Console
{
    protected static final String PROMPT = "> ";

    protected Console()
    {
    }

    /**
     * Returns a completed line of input.
     */
    public abstract String readLine() throws IOException;

    /**
     * Display the prompt (if necessary) to stdout.
     */
    public abstract void displayPrompt();

    /**
     * Enter or exit block mode (initially false)
     */
    public abstract void setBlockMode(final boolean on);

    /**
     * Cleanup if necessary.
     */
    public abstract void dispose();

    /**
     * Select the default console type for the situation
     */
    public static Console create(final ShellConfig config, final ShellScriptManager ssm,
        final FileServices fs, final List<String> files)
    {
        Console console = null;
        if (files.isEmpty())
        {
            if (config.getReadline())
            {
                try
                {
                    // ReadlineConsole supports line-editing, history, and auto-complete
                    console = new ReadlineConsole(ssm);
                }
                catch (Exception e)
                {
                    System.err.println("Could not create readline console");
                }
            }

            if (console == null)
            {
                // Default console: using traditional in/out streams
                console = new StreamConsole();
            }
        }
        else
        {
            console = new CommandFileConsole(fs, files);
        }
        return console;
    }
}
