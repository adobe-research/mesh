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

import compile.Dumpable;
import compile.module.Module;

/**
 * Compilation unit interface--backend agnostic.
 * A unit is what a module is compiled into.
 * {@link UnitBuilder#build} drives
 * the pipeline from {@link Module} to finished {@link Unit}.
 *
 * @author Basil Hosmer
 */
public interface Unit extends Dumpable
{
    /**
     * Get unit's source module.
     */
    Module getModule();

    /**
     * Write a representation of the unit to the given file path
     */
    boolean write(String path);
}
