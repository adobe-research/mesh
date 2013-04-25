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
import compile.type.Type;

/**
 * Common superclass of {@link ParamBinding} and {@link LetBinding}.
 *
 * @author Basil Hosmer
 */
public abstract class ValueBinding implements Binding, ValueStatement
{
    protected final Loc loc;
    protected final String name;
    protected Type declaredType;

    protected ValueBinding(final Loc loc, final String name, final Type declaredType)
    {
        this.loc = loc;
        this.name = name;
        this.declaredType = declaredType;
    }

    // Binding

    public final String getName()
    {
        return name;
    }

    // Typed

    public boolean hasDeclaredType()
    {
        return declaredType != null;
    }

    public Type getDeclaredType()
    {
        return declaredType;
    }
    
    public void setDeclaredType(final Type type)
    {
        declaredType = type;
    }

    // Statement

    public final boolean isBinding()
    {
        return true;
    }

    // Located

    public final Loc getLoc()
    {
        return loc;
    }
}
