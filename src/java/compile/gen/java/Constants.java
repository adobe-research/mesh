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
package compile.gen.java;

/**
 * java codegen constants (agreements)
 *
 * @author Basil Hosmer
 */
public final class Constants
{
    /**
     *
     */
    public static final String INSTANCE = "INSTANCE";

    /**
     *
     */
    public static final String APPLY = "apply";

    /**
     *
     */
    public static final String INVOKE = "invoke";

    /**
     *
     */
    public static final String OBJECT = Object.class.getSimpleName();

    /**
     * Infix ops desugar to function calls. Most of the backing functions
     * are intrinsics, and currently the compiler has carnal knowledge of
     * the runtime support package and uses the intrinsic names directly.
     * Functions that are (instead) supplied in mesh source should have
     * their names defined here.
     *
     * Note: there is no guarantee that the source functions are actually loaded -
     * right now the convention is that they appear in std.m, and that std.m gets
     * preloaded before anything nontrivial.
     */
    public static final String COMPOSE_FN = "compose";
}
