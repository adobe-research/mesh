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
package compile.term;

import compile.Loc;
import java.util.List;

/**
 * Causes a module to be loaded and a namespace created for it's symbols.
 *
 * @author Keith McGuigan
 */
public final class ExportStatement implements Statement
{
    private final Loc loc;
    private final List<String> symbols;
    private final boolean localsOnly;

    public ExportStatement(final Loc loc, final List<String> symbols)
    {
        this.loc = loc;
        this.symbols = isWildcard(symbols) ? null : symbols;
        this.localsOnly = isLocalsOnly(symbols);
    }

    public List<String> getSymbols() { return symbols; }
    public boolean getLocalsOnly() { return localsOnly; }

    private static boolean isSpecial(final List<String> syms, final String str) 
    {
        return syms != null && syms.size() == 1 && syms.get(0).equals(str); 
    }

    private static boolean isEmpty(final List<String> syms) 
    { 
        return syms != null && syms.size() == 0;
    }

    private static boolean isWildcard(final List<String> syms) 
    { 
        return syms == null || isSpecial(syms, "*"); 
    }

    private static boolean isLocalsOnly(final List<String> syms) 
    { 
        return isSpecial(syms, "."); 
    }

    // Statement

    public String dump()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("export ");

        if (isEmpty(symbols))
            sb.append("()");
        else if (isWildcard(symbols))
            sb.append("*");
        else if (localsOnly)
            sb.append(".");
        else
        {
            String sep = "";
            for (final String sym : symbols)
            {
                sb.append(sep);
                sb.append(sym);
                sep = ",";
            }
        }
        return sb.toString();
    }

    // Statement

    public final boolean isBinding()
    {
        return false;
    }

    // Located

    public final Loc getLoc()
    {
        return loc;
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ExportStatement that = (ExportStatement)o;
        if (localsOnly != that.localsOnly) return false;
        if (!checkNullEquals(symbols, that.symbols)) return false;
        return true;
    }

    private static boolean checkNullEquals(final Object o1, final Object o2) 
    {
        if ((o1 == null) != (o2 == null)) return false;
        if (o1 == null) return true;
        return o1.equals(o2);
    }

    @Override
    public int hashCode()
    {
        int h = 0;
        if (symbols != null)
        {
            for (final String sym : symbols)
            {
                h = 31 * h + sym.hashCode();
            }
        }
        return h;
    }
}
