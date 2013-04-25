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

import compile.term.ValueBinding;

/**
 * Super for class generators e.g. {@link LambdaClassGenerator},
 * {@link ModuleClassGenerator}. Holds common helpers etc.
 *
 * @author Basil Hosmer
 */
public class ClassGenerator
{
    /**
     * format a typed field declaration from a binding term
     */
    protected static String formatFieldDecl(final ValueBinding valueBinding,
                                            final boolean isPublic,
                                            final boolean isFinal,
                                            final StatementFormatter statementFormatter)
    {
        return (isPublic ? "public " : "private ") + (isFinal ? "final " : "") +
            statementFormatter.formatTypeName(valueBinding) + " " +
            StatementFormatter.formatName(valueBinding.getName());
    }
}
