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
 * Location info object.
 *
 * @author Basil Hosmer
 */
public final class Loc implements Comparable<Loc>
{
    // TODO doesn't belong here, maybe not anywhere
    public static final Loc INTRINSIC = new Loc("<intrinsic>");

    protected String path;
    protected int line;
    protected int column;

    public Loc(final String path, final int line, final int column)
    {
        this.path = path.replace('\\', '/');
        this.line = line;
        this.column = column;
    }

    public Loc(final String path)
    {
        this(path, 0, 0);
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(final String path)
    {
        this.path = path.replace('\\', '/');
    }

    public int getLine()
    {
        return line;
    }

    public void setLine(final int line)
    {
        this.line = line;
    }

    public int getColumn()
    {
        return column;
    }

    public void setColumn(final int column)
    {
        this.column = column;
    }

    public boolean isBefore(final Loc loc)
    {
        return compareTo(loc) == -1;
    }

    public int compareTo(final Loc loc)
    {
        return
            line > loc.line ? 1 :
                line < loc.line ? -1 :
                    column > loc.column ? 1 :
                        column < loc.column ? -1 :
                            0;
    }

    public String toString()
    {
        return path + (line > 0 ? "[" + line + (column > 0 ? ", " + column : "") + ']' : "");
    }

    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Loc loc = (Loc)o;

        if (column != loc.column) return false;
        if (line != loc.line) return false;
        if (path != null ? !path.equals(loc.path) : loc.path != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (path != null ? path.hashCode() : 0);
        result = 31 * result + line;
        result = 31 * result + column;
        return result;
    }
}
