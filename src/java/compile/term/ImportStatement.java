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
 * Requests that some or all of the definitions from
 * a specified module be loaded into a specified namespace.
 *
 * @author Keith McGuigan
 */
public final class ImportStatement implements Statement
{
    /**
     * import M
     */
    public static ImportStatement openUnqualified(final Loc loc, final String module)
    {
        return new ImportStatement(loc, WhiteList.open(), module, null);
    }

    /**
     * import M qualified / M into N
     */
    public static ImportStatement openQualified(final Loc loc, final String module,
        final String namespace)
    {
        return new ImportStatement(loc, WhiteList.open(), module, namespace);
    }

    /**
     * import x, y, z from M
     */
    public static ImportStatement enumUnqualified(final Loc loc,
        final List<String> symbols, final String module)
    {
        return new ImportStatement(loc, WhiteList.enumerated(symbols), module, null);
    }

    /**
     * import x, y, z from M qualified / M into N
     */
    public static ImportStatement enumQualified(final Loc loc,
        final List<String> symbols, final String module, final String namespace)
    {
        return new ImportStatement(loc, WhiteList.enumerated(symbols), module, namespace);
    }

    //
    // instance
    //

    private final Loc loc;
    private final WhiteList whiteList;
    private final String module;
    private final String namespace;

    /**
     * import M:                    whiteList=open,      module="M", namespace=null
     * import M qualified:          whiteList=open,      module="M", namespace="M"
     * import M into N:             whiteList=open,      module="M", namespace="N"
     * import x,y from M:           whiteList={"x","y"}, module="M", namespace=null
     * import x,y from M qualified: whiteList={"x","y"}, module="M", namespace="M"
     * import x,y from M into N:    whiteList={"x","y"}, module="M", namespace="N"
     */
    private ImportStatement(final Loc loc, final WhiteList whiteList,
        final String module, final String namespace)
    {
        this.loc = loc;
        this.whiteList = whiteList;
        this.module = module;
        this.namespace = namespace;
    }

    public String getModuleName()
    {
        return module;
    }

    public boolean isQualified()
    {
        return namespace != null;
    }

    public String getNamespace()
    {
        assert isQualified();
        return namespace;
    }

    public WhiteList getWhiteList()
    {
        return whiteList;
    }

    private StringBuilder dumpContent(final StringBuilder sb)
    {
        if (!whiteList.isOpen())
        {
            sb.append(whiteList.isEmpty() ? "()" :
                    StringUtils.join(whiteList.getEntries(), ", "));
            sb.append(" from ");
        }

        sb.append(module);

        if (isQualified())
        {
            if (namespace.equals(module))
                sb.append(" qualified");
            else
                sb.append(" into ").append(namespace);
        }

        return sb;
    }

    public String dumpAbbrev()
    {
        return dumpContent(new StringBuilder()).toString();
    }
    
    // Statement

    public String dump() 
    {
        return dumpContent(new StringBuilder("import ")).toString();
    }

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

        if (!whiteList.equals(that.whiteList)) return false;
        if (!module.equals(that.module)) return false;
        if (!(namespace == null ? that.namespace == null :
            namespace.equals(that.namespace))) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = whiteList.hashCode();
        result = 31 * result + module.hashCode();
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);

        return result;
    }
}
