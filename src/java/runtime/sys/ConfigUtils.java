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
package runtime.sys;

/**
 * Utilities for querying runtime configuration.
 *
 * @author Basil Hosmer
 */
public final class ConfigUtils
{
    /**
     * parse int property, or return default.
     */
    public static boolean parseBoolProp(final String propName, final boolean def)
    {
        final String prop = System.getProperty(propName);
        return prop == null ? def : Boolean.parseBoolean(prop);
    }

    /**
     * parse int property, or return default.
     */
    public static int parseIntProp(final String propName, final int def)
    {
        try
        {
            return Integer.parseInt(System.getProperty(propName));
        }
        catch (Exception e)
        {
            return def;
        }
    }

    /**
     * parse string property, or return default.
     */
    public static String parseStringProp(final String propName, final String def)
    {
        final String prop = System.getProperty(propName);
        return prop == null ? def : prop;
    }
}
