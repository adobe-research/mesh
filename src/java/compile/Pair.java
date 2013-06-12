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
 * Simple pair structure.
 *
 * @author Basil Hosmer
 */
public final class Pair<L, R>
{
    /**
     * lets us forego explicit type params
     */
    public static<L, R> Pair<L, R> create(final L l, final R r)
    {
        return new Pair<L, R>(l, r);
    }

    //
    // instance
    //

    public L left;
    public R right;

    public Pair(final L left, final R right)
    {
        this.left = left;
        this.right = right;
    }

    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Pair<?,?> pair = (Pair<?,?>)o;

        if (left != null ? !left.equals(pair.left) : pair.left != null) return false;
        if (right != null ? !right.equals(pair.right) : pair.right != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (left != null ? left.hashCode() : 0);
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }
}
