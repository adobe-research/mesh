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
public final class ImportStatement implements Statement
{
    private final Loc loc;
    private final List<String> symbols;
    private final String from;
    private final String as;

    /**
     *
     */
    public ImportStatement(final Loc loc, final List<String> symbols, 
        final String from, final String as)
    {
        this.loc = loc;
        this.symbols = symbols;
        this.from = from;
        this.as = as;
    }

    public String getFrom() { return from; }
    public String getAs() { return as; }
    public List<String> getSymbols() { return symbols; }

    public boolean isWildcard() 
    { 
        return symbols != null && symbols.size() == 1 && symbols.get(0).equals("*"); 
    }

    public boolean qualifiedOnly()
    {
        return symbols == null;
    }

    // Statement

    public String dump()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("import ");
        if (!qualifiedOnly()) 
        {
            if (isWildcard()) 
            {
                sb.append("*");
            }
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
            sb.append(" from ");
        }
        sb.append(from);
        if (as != null) 
        {
            sb.append(" as ");
            sb.append(as);
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

        final ImportStatement that = (ImportStatement)o;
        if (!from.equals(that.from)) return false;
        if (!checkNullEquals(symbols, that.symbols)) return false;
        return checkNullEquals(as, that.as);
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
        int h = from.hashCode();
        if (symbols != null) 
        {
            for (final String sym : symbols)
            {
                h = 31 * h + sym.hashCode();
            }
        }
        if (as != null) 
        {
            h = 31 * h + as.hashCode();
        }
        return h;
    }
}
