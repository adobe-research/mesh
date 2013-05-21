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
    private final String into;

    /**
     * import [* from] M:           symbols=null,      from="M", into=null
     * import x,y from M:           symbols={"x","y"}, from="M", into=null
     * import () from M:            symbols={},        from="M", into=null
     * import [* from] M into N:    symbols=null,      from="M", into="N"
     * import x,y from M into N:    symbols={"x","y"}, from="M", into="N"
     * import [* from] M qualified: symbols=null,      from="M", into="M"
     * import x,y from M qualified: symbols={"x","y"}, from="M", into="M"
     */
    public ImportStatement(final Loc loc, final List<String> symbols, 
        final String from, final String into)
    {
        this.loc = loc;
        this.symbols = isWildcard(symbols) ? null : symbols;
        this.from = from;
        this.into = into;
    }

    public String getFrom() { return from; }
    public String getInto() { return into; }
    public List<String> getSymbols() { return symbols; }

    public boolean isWildcard() { return isWildcard(symbols); }

    private boolean isWildcard(final List<String> syms) 
    { 
        return syms == null || (syms.size() == 1 && syms.get(0).equals("*"));
    }

    // Statement

    public String dump()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("import ");

        if (symbols != null && symbols.isEmpty())
            sb.append("()");
        else if (isWildcard(symbols)) 
            sb.append("*");
        else
        {
            assert symbols != null : "Else is a wildcard";
            String sep = "";
            for (final String sym : symbols)
            {
                sb.append(sep);
                sb.append(sym);
                sep = ",";
            }
        }

        sb.append(" from ");
        sb.append(from);

        if (into != null) {
            if (into.equals(from)) 
                sb.append(" qualified");
            else
            {
                sb.append(" into ");
                sb.append(into);
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

        final ImportStatement that = (ImportStatement)o;
        if (!from.equals(that.from)) return false;
        if (!checkNullEquals(into, that.into)) return false;
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
        int h = from.hashCode();
        if (symbols != null)
        {
            for (final String sym : symbols)
            {
                h = 31 * h + sym.hashCode();
            }
        }
        if (into != null) 
        {
            h = 31 * h + into.hashCode();
        }
        return h;
    }
}
