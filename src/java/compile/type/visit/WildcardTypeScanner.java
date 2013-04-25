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
package compile.type.visit;

import compile.type.Type;
import compile.type.WildcardType;

/**
 * Scans for wildcards in a type term.
 *
 * @author Basil Hosmer
 */
public final class WildcardTypeScanner extends TypeVisitorBase<Object>
{
    private static final ThreadLocal<WildcardTypeScanner> LOCAL =
        new ThreadLocal<WildcardTypeScanner>()
        {
            protected WildcardTypeScanner initialValue()
            {
                return new WildcardTypeScanner();
            }
        };

    public static boolean scan(final Type type)
    {
        final WildcardTypeScanner scanner = LOCAL.get();

        scanner.found = false;
        scanner.visitType(type);
        return scanner.found;
    }

    //
    // instance
    //

    private boolean found;

    // TypeVisitor

    @Override
    protected Object visitType(final Type type)
    {
        if (!found)
            super.visitType(type);

        return null;
    }

    @Override
    public String visit(final WildcardType wildcard)
    {
        found = true;
        return null;
    }
}
