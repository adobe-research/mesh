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

import java.util.ArrayList;
import java.util.List;

/**
 * An import or export whitelist.
 *
 * @author Keith McGuigan
 */
public class WhiteList
{
    private static final WhiteList OPEN = new WhiteList(null);

    /**
     * open whitelist (allows any name)
     */
    public static WhiteList open()
    {
        return OPEN;
    }

    /**
     * enumerated whitelist
     */
    public static WhiteList enumerated(final List<String> names)
    {
        return new WhiteList(names);
    }

    //
    // instance
    //

    /**
     * A null entries array indicates that all symbols pass.
     * An empty entries array indicates that no symbols psss.
     */
    private final List<String> entries;

    /**
     * Private constructor. entries == null indicates open list
     */
    private WhiteList(final List<String> entries)
    {
        this.entries = entries != null ? new ArrayList<String>(entries) : null;
    }

    /**
     * return true iff whitelist allows name
     */
    public boolean allows(final String name)
    {
        return isOpen() || entries.contains(name);
    }

    /**
     * return true iff whitelist is open (allows any name)
     */
    public boolean isOpen()
    {
        return entries == null;
    }

    /**
     * return true iff whitelist is empty (allows no name)
     */
    public boolean isEmpty()
    {
        return !isOpen() && entries.isEmpty();
    }

    /**
     * get symbol entrie, if list is not open
     */
    public List<String> getEntries()
    {
        assert !isOpen();
        return entries;
    }

    /**
     * true iff all our explicit entries resolve to definitions
     * in the given module
     */
    public boolean isValid(final Module module)
    {
        if (!isOpen())
        {
            for (final String entry : entries)
            {
                if (module.getLocalValueBinding(entry) == null &&
                    module.getLocalTypeBinding(entry) == null)
                    return false;
            }
        }

        return true;
    }
}
