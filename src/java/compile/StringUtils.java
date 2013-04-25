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

import java.util.Collection;
import java.util.Iterator;

/**
 * additions/overrides to commons.lang.String[*]Utils
 */
public final class StringUtils
{
    /**
     *
     */
    public static String join(final Iterable<String> strings, final String sep)
    {
        final StringBuilder b = new StringBuilder();
        final Iterator<String> iter = strings.iterator();

        if (iter.hasNext())
        {
            b.append(iter.next());
            while (iter.hasNext())
                b.append(sep).append(iter.next());
        }

        return b.toString();
    }

    /**
     *
     */
    public static String joinAndDelimit(
        final Collection<String> strings, final String sep, final String delim)
    {
        return joinAndDelimit(strings, sep, delim, delim);
    }

    /**
     *
     */
    public static String joinAndDelimit(final Collection<String> strings,
        final String sep, final String ldelim, final String rdelim)
    {
        final String joined = join(strings, sep);
        return joined.length() > 0 ? ldelim + joined + rdelim : "";
    }

    /**
     * NOTE: logic here is basically from Apache Commons Lang's
     * StringEscapeUtils.escapeJavaStyleString, but hacked to
     * suppress escapes the Javassist lexer can't handle.
     */
    public static String escapeJava(final String s)
    {
        final int n = s.length();
        final StringBuilder b = new StringBuilder();

        for (int i = 0; i < n; i++)
        {
            final char ch = s.charAt(i);

            /* // handle unicode
            if (ch > 0xfff) {
                w.write("\\u" + hex(ch));
            } else if (ch > 0xff) {
                w.write("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                w.write("\\u00" + hex(ch));
            } else */

            if (ch < 32)
            {
                switch (ch)
                {
                    /* case '\b':
                        out.write("\\b");
                        break; */
                    case '\n':
                        b.append("\\n");
                        break;
                    case '\t':
                        b.append("\\t");
                        break;
                    case '\f':
                        b.append("\\f");
                        break;
                    case '\r':
                        b.append("\\r");
                        break;
                    default:
                        /* if (ch > 0xf)
                            out.write("\\u00" + hex(ch));
                        else
                            out.write("\\u000" + hex(ch)); */
                        b.append(ch);
                        break;
                }
            }
            else
            {
                switch (ch)
                {
                    case '"':
                        b.append("\\\"");
                        break;
                    case '\\':
                        b.append("\\\\");
                        break;
                    case '/':
                        b.append("\\/");
                        break;
                    default:
                        b.append(ch);
                        break;
                }
            }
        }

        return b.toString();
    }

    /**
     * unescape java strings well enough for our purposes.
     * Note: rather than throwing, we pass invalid
     * unicode sequences escaped through to the result.
     */
    public static String unescapeJava(final String s)
    {
        final StringBuilder b = new StringBuilder();
        final int n = s.length();

        for (int i = 0; i < n; )
        {
            final char c = s.charAt(i++);

            if (c == '\\' && i < n)
            {
                final char e = s.charAt(i++);

                switch (e)
                {
                    case 'u':
                    {
                        if (i < n - 3)
                        {
                            try
                            {
                                b.append((char)Integer.parseInt("" + s.charAt(i++) +
                                    s.charAt(i++) + s.charAt(i++) + s.charAt(i++), 16));

                                continue;
                            }
                            catch (NumberFormatException nfe)
                            {
                                i -= 4;
                            }
                        }

                        b.append("\\u");
                        break;
                    }
                    case '\\':
                        b.append('\\');
                        break;
                    case '\'':
                        b.append('\'');
                        break;
                    case '\"':
                        b.append('"');
                        break;
                    case 'r':
                        b.append('\r');
                        break;
                    case 'f':
                        b.append('\f');
                        break;
                    case 't':
                        b.append('\t');
                        break;
                    case 'n':
                        b.append('\n');
                        break;
                    case 'b':
                        b.append('\b');
                        break;
                    default :
                        b.append(c).append(e);
                        break;
                }
            }
            else
            {
                b.append(c);
            }
        }

        return b.toString();
    }
}
