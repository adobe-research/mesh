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
package shell;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

/**
 * Services for creating FileReaders from names,
 * given a {@link ShellConfig}
 *
 * @author Basil Hosmer
 */
public final class FileServices
{
    private final ShellConfig shellConfig;

    public FileServices(final ShellConfig shellConfig)
    {
        this.shellConfig = shellConfig;
    }

    public Reader getReader(final String fileName)
    {
        final File file = findFile(fileName);
        return file != null ? getReader(file) : null;
    }

    public Reader getReader(final File file)
    {
        try
        {
            return new FileReader(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public File findFile(final String name)
    {
        File file = new File(name);
        if (file.exists())
            return file;

        for (final String pathElement : shellConfig.getScriptPath())
        {
            file = new File(pathElement, name);
            if (file.exists())
                return file;
        }

        return null;
    }
}
