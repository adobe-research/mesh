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

import com.google.common.io.Closeables;
import runtime.intrinsic.IntrinsicLambda;
import runtime.rep.Tuple;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Demo support--simple file write
 *
 * @author Basil Hosmer
 */
public final class _writefile extends IntrinsicLambda
{
    public static final _writefile INSTANCE = new _writefile(); 
    public static final String NAME = "writefile";

    public String getName()
    {
        return NAME;
    }

    public final Boolean apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((String)args.get(0), (String)args.get(1));
    }

    public static boolean invoke(final String path, final String contents)
    {
        final File file = new File(path);

        FileWriter fw = null;
        try
        {
            fw = new FileWriter(file, false);
            fw.write(contents);
        }
        catch (IOException ignore)
        {
            return false;
        }
        finally
        {
            Closeables.closeQuietly(fw);
        }

        return true;
    }
}
