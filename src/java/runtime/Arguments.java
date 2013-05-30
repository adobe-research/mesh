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
package runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages runtime command-line arguments
 *
 * @author Keith McGuigan
 */
public final class Arguments
{
    private static List<String> arguments = new ArrayList<String>();

    /**
     * Add a new string argument
     */
    public static void add(final String value)
    {
        arguments.add(value);
    }

    /**
     * Retrieve a copy of the arguments
     */
    public static List<String> get()
    {
        final List<String> args = new ArrayList<String>(arguments.size());
        args.addAll(arguments);
        return args;
    }
}
