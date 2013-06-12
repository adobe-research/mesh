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

import compile.gen.java.JavaUnit;
import compile.module.Module;

/**
 * Service for building compilation units from modules.
 * Hooked up to backend-specific implementation by compiler.
 * See similar setup in {@link IntrinsicsResolver}.
 *
 * @author Basil Hosmer
 */
public final class UnitBuilder
{
    /**
     * Back end specific implementation interface.
     */
    public interface Impl
    {
        /**
         * For shell/debug convenience: provide access to the most recent
         * Unit built, whether or not the build was successful.
         */
        Unit getLastBuildAttempt();

        /**
         * Generate a {@link compile.gen.java.JavaUnit} for given Module.
         */
        JavaUnit build(final Module module, final UnitDictionary unitDictionary);
    }

    /**
     * Singleton implementation.
     */
    private static Impl IMPL;

    /**
     * Install a particular back-end implementation, once.
     * TODO needs to be part of a larger back-end pluggability setup
     */
    public static synchronized void setImpl(final Impl impl)
    {
        assert IMPL == null;
        IMPL = impl;
    }

    public static synchronized Unit getLastBuildAttempt()
    {
        return IMPL.getLastBuildAttempt();
    }

    public static synchronized Unit build(
        final Module module, final UnitDictionary unitDictionary)
    {
        return IMPL.build(module, unitDictionary);
    }
}
