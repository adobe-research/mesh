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

import compile.Session;
import shell.FileServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

class CommandFileConsole extends Console
{
    private final String[] files;
    private final FileServices fileServices;
    private int currentFile;
    private BufferedReader reader;

    CommandFileConsole(final FileServices fs, final List<String> commandFiles)
    {
        this.fileServices = fs;
        this.files = commandFiles.toArray(new String[commandFiles.size()]);
        this.currentFile = -1;
        openNextFile();
    }

    private boolean eof()
    {
        return currentFile >= files.length;
    }

    private void openNextFile()
    {
        reader = null;
        ++currentFile;
        if (!eof())
        {
            try
            {
                final Reader r = fileServices.getReader(files[currentFile]);
                if (r != null)
                {
                    reader = new BufferedReader(r);
                }
            }
            catch (Exception e)
            {
                Session.error("Unabled to read command file {0}", files[currentFile]);
            }
        }
    }

    public String readLine() throws IOException
    {
        String line = null;
        if (!eof())
        {
            if (reader == null)
            {
                openNextFile();
                line = readLine();
            }
            else
            {
                line = reader.readLine();
                if (line == null)
                {
                    reader.close();
                    openNextFile();
                    line = readLine();
                }
            }
        }
        return line;
    }

    public void displayPrompt()
    {
    }

    public void setBlockMode(final boolean on)
    {
    }

    public void dispose()
    {
        try
        {
            if (reader != null)
            {
                reader.close();
            }
            reader = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
