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
package compile.type;

import compile.Loc;
import compile.type.visit.*;

import java.util.Set;

/**
 * Common base implementation for all types.
 *
 * @author Basil Hosmer
 */
public abstract class AbstractType implements Type
{
    protected Loc loc;

    public AbstractType(final Loc loc)
    {
        this.loc = loc;
    }

    // Type

    public final void setLoc(final Loc loc)
    {
        this.loc = loc;
    }

    public Type deref()
    {
        return this;
    }

    public Type eval()
    {
        return this;
    }

    public final boolean hasVars()
    {
        return TypeVarCollector.check(this);
    }
    
    public final Set<TypeVar> getVars()
    {
        return TypeVarCollector.collect(this);
    }

    public final Type subst(final SubstMap substMap)
    {
        return new TypeVarSubstitutor(this, substMap).apply();
    }

    public boolean equiv(final Type other)
    {
        return equiv(other, new EquivState());
    }
    
    public boolean hasWildcards()
    {
        return WildcardTypeScanner.scan(this);
    }

    // Located

    public final Loc getLoc()
    {
        return loc;
    }

    // Dumpable

    public final String dump()
    {
        return TypeDumper.dump(this);
    }
}
