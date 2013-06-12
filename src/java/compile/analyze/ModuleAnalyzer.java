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
package compile.analyze;

import compile.module.Module;

/**
 * Module analysis pipeline. Incoming module is as-parsed,
 * outgoing module is ready for CG.
 *
 * @author Basil Hosmer
 */
public final class ModuleAnalyzer
{
    /**
     * Push a new module through analysis steps. On successful completion,
     * module is ready for codegen, e.g. by {@link compile.gen.java.JavaUnitBuilder} for JVM.
     *
     * @param module module to be analyzed
     * @return success
     */
    public static boolean analyze(final Module module)
    {
        // setup namespaces and import symbols and types
        if (!new ImportResolver(module).resolve())
            return false;

        // collect value bindings, type bindings, module imports
        if (!new BindingCollector(module).collect())
            return false;
        
        // setup export list
        if (!new ExportResolver(module).resolve())
            return false;

        // resolve names
        if (!new RefResolver(module).resolve())
            return false;

        // check forward references
        if (!new RefChecker(module).check())
            return false;

        // infer and check types
        if (!new TypeChecker(module).check())
            return false;

        //
        // post-typecheck optimizations:
        //

        // constant folding/propagation
        if (!new ConstantReducer(module).reduce())
            return false;

        // TODO inlining, etc. etc.


        return true;
    }
}
