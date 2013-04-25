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
package compile.term;

import compile.Loc;
import compile.type.NonScopeType;

/**
 * A type binding is either a {@link TypeDef} or a {@link compile.type.TypeParam}.
 * The targets of {@link compile.type.TypeRef} terms are type bindings.
 * Note that a type binding is also a type.
 *
 * @author Basil Hosmer
 */
public abstract class TypeBinding extends NonScopeType implements Binding
{
    protected String name;

    public TypeBinding(final Loc loc, final String name)
    {
        super(loc);
        this.name = name;
    }

    // Binding

    public final String getName()
    {
        return name;
    }

    public boolean isLet()
    {
        return false;
    }

    // Statement

    public boolean isBinding()
    {
        return true;
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TypeBinding namedType = (TypeBinding)o;

        if (!name.equals(namedType.name)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
