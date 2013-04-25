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
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

/**
 * This class is used to represent both type constructors
 * and type abstractions, the latter with a non-null body
 * that is returned by reduce.
 *
 * @author Basil Hosmer
 */
public final class TypeCons extends NonScopeType
{
    public final String name;
    public final Kind kind;
    public final Type body;

    public TypeCons(final Loc loc, final String name, final Kind kind, final Type body)
    {
        super(loc);
        this.name = name;
        this.kind = kind;
        this.body = body;
    }

    public TypeCons(final String name, final Kind kind, final Type body)
    {
        this(Loc.INTRINSIC, name, kind, body);
    }

    public TypeCons(final String name, final Kind kind)
    {
        this(Loc.INTRINSIC, name, kind, null);
    }

    public TypeCons(final String name)
    {
        this(name, Kinds.STAR);
    }
    
    public String getName()
    {
        return name;
    }

    public Type getBody()
    {
        return body;
    }

    public boolean isAbs()
    {
        return body != null;
    }

    // Type

    public Kind getKind()
    {
        return kind;
    }

    public SubstMap unify(final Loc loc, final Type other, final TypeEnv env)
    {
        return
            other instanceof TypeVar ? SubstMap.bindVar(loc, (TypeVar)other, this) :
                other.deref().equals(this) ? SubstMap.EMPTY :
                    null;
    }

    public boolean equiv(final Type other, final EquivState state)
    {
        return equals(other.deref());
    }

    public <T> T accept(final TypeVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final TypeCons typeCons = (TypeCons)o;

        if (!kind.equals(typeCons.kind)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + kind.hashCode();
        return result;
    }
}
