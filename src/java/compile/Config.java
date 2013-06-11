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

import compile.gen.IntrinsicsResolver;
import compile.gen.UnitBuilder;
import compile.gen.UnitManager;
import compile.gen.java.JavaIntrinsicsResolver;
import compile.gen.java.JavaUnitBuilder;
import compile.gen.java.JavaUnitManager;
import compile.term.ImportStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiler configuration props etc.
 * Currently we're holding three kinds of things here:
 *
 * 1. properties that are invariant once the compiler is built,
 * e.g. the implicit imports list.
 *
 * 2. properties that should be pluggable, mostly having to do
 * with target back-end, but are currently also invariant once
 * built, e.g. intrinsics resolution and code generation services
 *
 * 3. properties that are per-compilation configurable, such as
 * search path. Note that there are other such properties in
 * {@link shell.ShellConfig}, the difference being that the
 * properties found here should have behavioral meaning independent
 * of the context in which the compiler is being run (e.g. shell,
 * AOT).
 *
 *
 * @author Basil Hosmer
 */
public final class Config
{
    /**
     * Search paths used for source file lookup.
     */
    private static List<String> SearchPaths = new ArrayList<String>();

    /**
     * Compiler-defined list of imports that are prepended
     * by default to every script.
     */
    public static final List<ImportStatement> ImplicitImports =
        new ArrayList<ImportStatement>();

    static
    {
        ImplicitImports.add(ImportStatement.openUnqualified(Loc.INTRINSIC, "lang"));
    }

    /**
     * Initialize target-specific back end components.
     * TODO decouple other hard-linked back end components and merge these sets
     */
    static
    {
        IntrinsicsResolver.setImpl(new JavaIntrinsicsResolver());
        UnitBuilder.setImpl(new JavaUnitBuilder());
    }

    //
    // methods
    //

    /**
     *
     */
    public static synchronized List<String> getSearchPaths()
    {
        return SearchPaths;
    }

    /**
     *
     */
    public static synchronized void addSearchPath(final String path)
    {
        SearchPaths.add(path);
    }

    /**
     * True if passed module name is an implicit import.
     * Note that implicit imports may themselves have imports,
     * which will return false from this function.
     */
    public static boolean isImplicitImport(final String moduleName)
    {
        for (final ImportStatement implicitImport : ImplicitImports)
            if (implicitImport.getModuleName().equals(moduleName))
                return true;

        return false;
    }

    /**
     * return target-specific unit manager
     * TODO make pluggable and integrate with other back-end choices, above
     */
    public static UnitManager newUnitManager()
    {
        return new JavaUnitManager();
    }
}
