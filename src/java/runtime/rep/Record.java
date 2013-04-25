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
 * Runtime representation for values of type Rec(...).
 * Note: will be tightened up along with type system
 * changes around records.
 *
 * @author Basil Hosmer
 */
public final class Record
{
    /**
     * the empty record
     */
    public static final Record EMPTY = new Record(new Object[]{}, new Object[]{});

    /**
     *
     */
    public static Record from(final Object[] keys, final Object[] vals)
    {
        return keys.length == 0 ? EMPTY : new Record(keys, vals);
    }

    //
    // instance
    //

    private final Object[] keys;
    private final Object[] values;
    private int hash;

    public Record(final Object[] keys, final Object[] values)
    {
        this.keys = keys;
        this.values = values;
        this.hash = -1;

        assert keys.length == values.length;
    }

    /**
     * record width
     */
    public int size()
    {
        return keys.length;
    }

    /**
     * get key by position
     */
    public Object getKey(final int i)
    {
        return keys[i];
    }

    /**
     * get value by position
     */
    public Object getValue(final int i)
    {
        return values[i];
    }

    /**
     * get field by key
     */
    public Object get(final Object key)
    {
        for (int i = 0; i < keys.length; i++)
            if (key.equals(keys[i]))
                return values[i];

        return null;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
            return true;

        if (!(obj instanceof Record))
            return false;

        final Record rec = (Record)obj;

        final int size = size();

        if (rec.size() != size)
            return false;

        // test in-order prefix before permuting
        int i = 0;
        for (; i < size; i++)
        {
            if (!getKey(i).equals(rec.getKey(i)))
                break;

            if (!getValue(i).equals(rec.getValue(i)))
                return false;
        }

        // if we're equal so far but not finished,
        // test equality mod permutation for the rest
        if (i < size)
        {
            for (; i < size; i++)
            {
                final Object val = getValue(i);

                final Object recVal = rec.get(getKey(i));

                if (recVal == null || !recVal.equals(val))
                    return false;
            }
        }

        return true;
    }

    /**
     * Note: as with maps, permutations must produce the same hashcode.
     */
    @Override
    public final int hashCode()
    {
        if (hash == -1)
        {
            final int size = size();

            if (size == 0)
                return 0;

            hash = 0;
            for (int i = 0; i < size; i++)
                hash += getKey(i).hashCode();

            for (int i = 0; i < size; i++)
                hash += getValue(i).hashCode();
        }

        return hash;
    }
}