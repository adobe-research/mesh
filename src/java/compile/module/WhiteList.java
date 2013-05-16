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
package compile.module;

import java.util.*;

/**
 * An import or export whitelist.
 *
 * @author Keith McGuigan
 */
public class WhiteList
{
    // A null entries array indicates that all symbols pass.
    // An empty entries array indicates that no symbols psss.
    private final List<String> entries;

    private WhiteList(final List<String> names) 
    {
        if (names != null) 
        {
            this.entries = new ArrayList<String>(names.size());
            for (final String name : names) 
                this.entries.add(name);
        }
        else
        {
            this.entries = null;
        }
    }

    // Factory constructors
    public static WhiteList open() 
    {
        return new WhiteList(null);
    }

    public static WhiteList closed() 
    {
        return new WhiteList(Collections.<String>emptyList());
    }

    public static WhiteList enumerated(final List<String> names)
    {
        return new WhiteList(names);
    }

    public boolean allows(final String name) 
    {
        if (entries != null) 
        {
            for (final String entry : entries) 
                if (entry.equals(name))
                    return true;
            return false;
        }
        return true;
    }
}
