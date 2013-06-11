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
import compile.StringUtils;
import compile.module.WhiteList;

import java.util.List;

/**
 * Specifies the set of definitions to be exported from a module.
 *
 * @author Keith McGuigan
 */
public final class ExportStatement implements Statement
{
    public static ExportStatement open(final Loc loc)
    {
        return new ExportStatement(loc, WhiteList.open());
    }

    public static ExportStatement enumerated(final Loc loc, final List<String> symbols)
    {
        return new ExportStatement(loc, WhiteList.enumerated(symbols));
    }

    //
    // instance
    //

    private final Loc loc;
    private final WhiteList whiteList;

    /**
     * export *:    symbols=open,
     * export x,y:  symbols={"x","y"}
     */
    private ExportStatement(final Loc loc, final WhiteList whiteList)
    {
        this.loc = loc;
        this.whiteList = whiteList;
    }

    /**
     * return the explicitly enumerated set of symbols to export
     */
    public WhiteList getWhiteList()
    {
        return whiteList;
    }

    // Statement

    public String dump()
    {
        return "export " + (
            whiteList.isOpen() ? "*" :
            whiteList.isEmpty() ? "()" :
            StringUtils.join(whiteList.getEntries(), ", "));
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

        if (!whiteList.equals(that.whiteList))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return whiteList.hashCode();
    }
}
