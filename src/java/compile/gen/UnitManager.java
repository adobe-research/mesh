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
package compile.gen;

/**
 * Provides unit loading and unloading services. As a straddler
 * between compile and runtime worlds, these services really
 * belong more in the shell than the compiler, but we're making
 * the choice to colocate here currently, since this allows all
 * carnal knowledge to be package-internal.
 */
public interface UnitManager
{
    /**
     *
     */
    boolean loadUnit(final Unit unit);

    /**
     *
     */
    boolean unloadUnit(final Unit unit);

    /**
     *
     */
    void resetInternals();
}
