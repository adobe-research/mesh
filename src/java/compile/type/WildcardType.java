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
 * Wildcard type, used to represent to-be-inferred "holes" in
 * larger compound type terms, e.g. unannotated function params.
 *
 * @author Basil Hosmer
 */
public final class WildcardType extends NonScopeType
{
    public WildcardType(final Loc loc)
    {
        super(loc);
    }

    public WildcardType()
    {
        this(Loc.INTRINSIC);
    }

    // Type

    public Kind getKind()
    {
        return Kinds.STAR;
    }
    
    public SubstMap unify(final Loc loc, final Type other, final TypeEnv env)
    {
        assert false : "WildcardType.unify()";
        return null;
    }

    public <T> T accept(final TypeVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    public boolean equiv(final Type other, final EquivState state)
    {
        return other.getKind() == Kinds.STAR;
    }
}