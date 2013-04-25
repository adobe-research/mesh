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
package compile;

/**
 * Name-related string utilities
 *
 * @author Basil Hosmer
 */
public final class NameUtils
{
    /**
     * @param s String to unqualify
     * @return the substring of s that follows the last occurence of '.', or all of s if '.' does not occur
     */
    public static String unqualify(final String s, final char sep)
    {
        final int last = s.lastIndexOf(sep);
        return last >= 0 ? s.substring(last + 1) : s;
    }

    /**
     * @param s String to unqualify
     * @return the substring of s that follows the last occurence of '.', or all of s if '.' does not occur
     */
    public static String unqualify(final String s)
    {
        return unqualify(s, '.');
    }

    /**
     * @param s String to extract qualifier from
     * @return the substring of s that preceds the last occurence of '.', or the empty string if '.' does not occur
     */
    public static String qualifier(final String s, final char sep)
    {
        final int last = s.lastIndexOf(sep);
        return last >= 0 ? s.substring(0, last) : "";
    }

    /**
     * @param s String to extract qualifier from
     * @return the substring of s that preceds the last occurence of '.', or the empty string if '.' does not occur
     */
    public static String qualifier(final String s)
    {
        return qualifier(s, '.');
    }

    /**
     * Given a path string of the form [path/]name[.ext], returns name.
     *
     * @param path Path string to extract filename stem from
     * @return filename stem
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static String getFilenameStemFromPath(String path)
    {
        final int lastSlash = path.lastIndexOf('/');

        if (lastSlash >= 0)
        {
            path = path.substring(lastSlash + 1);
        }

        final int firstDot = path.indexOf('.');

        return firstDot >= 0 ? path.substring(0, firstDot) : path;
    }

    /**
     * Valid names are nonempty strings in which
     * <ul>
     * <li>the first character is either a letter (as determined by java.lang.Character.isLetter()) or the underscore character;
     * <li>subsequent characters are either letters (java.lang.Character.isLetter()), digits
     * (java.lang.Character.isDigit()), or the underscore character.
     * </ul>
     *
     * @param s String to test
     * @return true if s is a Fiber name
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public static boolean isValidName(final String s)
    {
        if (s.length() == 0)
        {
            return false;
        }

        if (!isNameFirstChar(s.charAt(0)))
        {
            return false;
        }

        for (int i = 1; i < s.length(); i++)
        {
            if (!isNameFollowChar(s.charAt(i)))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Legal first characters for names are letters (as defined by java.lang.Character.isLetter()) and the
     * underscore character.
     *
     * @param ch character to test
     * @return true if ch is a valid name first character
     */
    private static boolean isNameFirstChar(final char ch)
    {
        return Character.isLetter(ch) || ch == '_';
    }

    /**
     * A non-initial character in a name must either be a legal first character, or be a digit as determined by
     * java.lang.Character.isDigit().
     *
     * @param ch character to test
     * @return true if ch is a valid non-initial name character
     */
    private static boolean isNameFollowChar(final char ch)
    {
        return isNameFirstChar(ch) || Character.isDigit(ch);
    }
}
