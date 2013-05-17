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

/**
 * An entry in an import or export whitelist.
 *
 * @author Keith McGuigan
 */
public class WhiteListEntry
{
    private static final String WILD_SPEC = ".*";

    private final String value;
    private final boolean wildcard; // if true, then "<value>.*" is accepted

    public WhiteListEntry(final String entry) 
    {
        if (entry.endsWith(WILD_SPEC))
        {
            this.value = entry.substring(0, entry.length() - WILD_SPEC.length());
            this.wildcard = true;
        }
        else
        {
            this.value = entry;
            this.wildcard = false;
        }
    }

    boolean matches(final String qname) 
    {
        return qname.equals(value) || (wildcard && qname.startsWith(value + "."));
    }

    boolean startsWith(final String prefix) 
    {
        return value.startsWith(prefix);
    }
}
