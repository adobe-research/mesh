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

import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;

/**
 * Manages runtime command-line arguments
 *
 * @author Keith McGuigan
 */
public final class Arguments
{
    private static ListValue arguments = PersistentList.EMPTY;

    /**
     * Add a new string argument
     */
    public static synchronized void add(final String value)
    {
        arguments = arguments.append(value);
    }

    /**
     * Retrieve a copy of the arguments
     */
    public static ListValue get()
    {
        return arguments;
    }
}
