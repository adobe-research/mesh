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
package runtime.rep;

/**
 * Runtime representation for values of type Tup(...).
 *
 * @author Basil Hosmer
 */
public final class Tuple
{
    /**
     * the unit tuple
     */
    public static final Tuple UNIT = new Tuple(new Object[]{});

    /**
     *
     */
    public static Tuple from(final Object[] items)
    {
        return items.length == 0 ? UNIT : new Tuple(items);
    }

    /**
     *
     */
    public static Tuple from(final Object a, final Object b)
    {
        return new Tuple(new Object[]{a, b});
    }

    /**
     *
     */
    public static Tuple from(final Object a, final Object b, final Object c)
    {
        return new Tuple(new Object[]{a, b, c});
    }

    //
    // instance
    //

    private final Object[] items;
    private int hash;

    public Tuple(final Object[] items)
    {
        this.items = items;
        this.hash = 0;
    }

    public int size()
    {
        return items.length;
    }

    public Object get(final int i)
    {
        return items[i];
    }

    /**
     * CAUTION: internal use only, caller must ensure
     * no unintended aliasing etc.
     */
    public void set(final int i, final Object v)
    {
        items[i] = v;
        hash = 0;
    }

    @Override
    public final boolean equals(final Object o)
    {
        if (this == o) return true;

        if (!(o instanceof Tuple))
            return false;

        final Tuple t = (Tuple)o;

        final int size = size();

        if (t.size() != size)
            return false;

        for (int i = 0; i < size; i++)
            if (!get(i).equals(t.get(i)))
                return false;

        return true;
    }

    @Override
    public final int hashCode()
    {
        if (hash == 0)
        {
            hash = 1;

            final int size = size();
            for (int i = 0; i < size; i++)
                hash = 31 * hash + get(i).hashCode();
        }

        return hash;
    }
}