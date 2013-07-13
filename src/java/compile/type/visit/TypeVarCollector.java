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
import compile.type.TypeVar;

import java.util.LinkedHashSet;

/**
 * Collects the type variables from a type term.
 *
 * @author Basil Hosmer
 */
public final class TypeVarCollector extends TypeVisitorBase<Object>
{
    /**
     * Check for the presence of vars in type
     */
    public static boolean check(final Type type)
    {
        final TypeVarCollector collector = new TypeVarCollector();

        collector.vars = null;
        collector.found = false;

        collector.visitType(type);

        return collector.found;
    }

    /**
     * Collect type variables mentioned in type
     */
    public static LinkedHashSet<TypeVar> collect(final Type type)
    {
        final TypeVarCollector collector = new TypeVarCollector();

        collector.vars = new LinkedHashSet<TypeVar>();
        collector.found = false;

        collector.visitType(type);

        return collector.vars;
    }

    //
    // instance
    //

    private LinkedHashSet<TypeVar> vars;
    private boolean found;

    /**
     * shortcut in check mode
     */
    @Override
    protected Object visitType(final Type type)
    {
        return found ? null : super.visitType(type);
    }

    /**
     *
     */
    @Override
    public Object visit(final TypeVar var)
    {
        if (vars != null)
        {
            vars.add(var);
            vars.addAll(var.getConstraint().getVars());
        }
        else
        {
            found = true;
        }

        return null;
    }
}