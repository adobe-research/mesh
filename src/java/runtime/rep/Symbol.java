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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime representation for values of type Symbol.
 * Symbols are interned in {@link #SYM_POOL}.
 *
 * @author Basil Hosmer
 */
public final class Symbol
{
    private static final ConcurrentHashMap<String, Symbol> SYM_POOL =
        new ConcurrentHashMap<String, Symbol>();

    /**
     * Sole supplier of Symbol values at runtime.
     */
    public static Symbol get(final String value)
    {
        final Symbol sym = SYM_POOL.get(value);

        if (sym != null)
            return sym;

        synchronized (SYM_POOL)
        {
            final Symbol oldSym = SYM_POOL.get(value);

            if (oldSym != null)
                return oldSym;

            final Symbol newSym = new Symbol(value);

            SYM_POOL.put(value, newSym);

            return newSym;
        }
    }

    //
    // instance
    //

    private final String value;

    private Symbol(final String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}