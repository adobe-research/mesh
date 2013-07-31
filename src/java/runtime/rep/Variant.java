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
 * Runtime representation class for values of type Var(...).
 * Note: in flux.
 *
 * @author Basil Hosmer
 */
public final class Variant
{
    private final Object key, value;

    public Variant(final Object key, final Object value)
    {
        this.key = key;
        this.value = value;
    }

    public Object getKey()
    {
        return key;
    }

    public Object getValue()
    {
        return value;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof Variant)
        {
            final Variant variant = (Variant)obj;

            return key.equals(variant.key) &&
                value.equals(variant.value);
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}