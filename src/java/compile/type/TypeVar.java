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

import com.google.common.collect.Sets;
import compile.Loc;
import compile.Session;
import compile.type.constraint.Constraint;
import compile.type.kind.Kind;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeVisitor;

import java.util.Set;

/**
 * Type variable.
 *
 * @author Basil Hosmer
 */
public final class TypeVar extends NonScopeType
{
    private final String name;
    private final Kind kind;
    private Constraint constraint;
    private TypeParam sourceParam;
    private Set<TypeParam> unifiedParams;

    private TypeVar(final Loc loc, final String name,
        final Kind kind, final Constraint constraint,
        final TypeParam sourceParam)
    {
        super(loc);
        this.name = name;
        this.kind = kind;
        this.constraint = constraint;
        this.sourceParam = sourceParam;
        this.unifiedParams = Sets.newIdentityHashSet();

        if (sourceParam != null)
            unifiedParams.add(sourceParam);

        if (kind == null)
            assert false : "null kind in TypeVar ctor: " + name;

        if (constraint == null)
            assert false : "null constraint in TypeVar ctor" + name;
    }

    public TypeVar(final Loc loc, final String name,
        final Kind kind, final Constraint constraint)
    {
        this(loc, name, kind, constraint, null);
    }

    /**
     * Note that param constraint is installed here
     * without modification--must be replaced by
     * instantiation after construction.
     */
    public TypeVar(final String name, final TypeParam sourceParam)
    {
        this(sourceParam.getLoc(), name, sourceParam.getKind(),
            sourceParam.getConstraint(), sourceParam);
    }

    public String getName()
    {
        return name;
    }

    public Constraint getConstraint()
    {
        return constraint;
    }

    public void setConstraint(final Constraint constraint)
    {
        this.constraint = constraint;
    }

    public boolean hasSourceParam()
    {
        return sourceParam != null;
    }

    public TypeParam getSourceParam()
    {
        return sourceParam;
    }

    public Set<TypeParam> getUnifiedParams()
    {
        return unifiedParams;
    }
    
    public void addUnifiedParam(final TypeParam param)
    {
        this.unifiedParams.add(param);
    }

    public void addUnifiedParams(final Set<TypeParam> params)
    {
        this.unifiedParams.addAll(params);
    }

    public TypeParam getBackingParam(final Set<String> exclude, final ScopeType scope)
    {
        if (sourceParam != null &&
            !exclude.contains(sourceParam.getName()) &&
            (scope.getLoc().equals(sourceParam.getTypeScope().getLoc())))
        {
            if (Session.isDebug())
                Session.debug(loc,
                    "type var {0}:in-scope source param {1} available, using",
                    name, sourceParam.dump());

            return sourceParam;
        }

        // next best is a unified param from the original scope
        if (!unifiedParams.isEmpty())
        {
            for (final TypeParam unified : unifiedParams)
            {
                if (!exclude.contains(unified.getName()) &&
                    (scope.getLoc().equals(unified.getTypeScope().getLoc())))
                {
                    if (Session.isDebug())
                        Session.debug(loc,
                            "type var {0}:in-scope unified param {1} available, using",
                            name, unified.dump());

                    return unified;
                }
            }
        }

        return null;
    }

    // Type

    public Kind getKind()
    {
        return kind;
    }

    /**
     * quantify this singleton type var.
     *
     * Note: per the "types don't come from nowhere" rule, there should
     * be no legitimate cases where a lone type variable is quantified.
     */
    public Type quantify(final SubstMap newParams, final SubstMap ambientParams)
    {
        if (newParams.containsKey(this))
        {
            // see header comment
            Session.error(loc, "quantifying lone type var {0}", dump());

            final TypeParam param = (TypeParam)newParams.get(this);
            final TypeRef typeRef = new TypeRef(loc, param);
            typeRef.addParam(param);
            return typeRef;
        }

        // normal case
        return ambientParams.containsKey(this) ?
            new TypeRef(loc, (TypeParam)ambientParams.get(this)) :
            this;
    }

    /**
     * build a param map for this singleton type var.
     *
     * Note: per the "types don't come from nowhere" rule, there should
     * be no legitimate cases where a param map is built for a lone type variable.
     */
    public SubstMap buildParamMap(final Set<TypeVar> vars,
        final int nameGenOffset, final TypeEnv env)
    {
        if (vars.contains(this))
        {
            Session.error(loc, "building param map for lone type var {0}", dump());

            final TypeParam param = hasSourceParam() ?
                getSourceParam() : new TypeParam(loc, name, kind, constraint);

            final SubstMap subst = SubstMap.bindVar(loc, this, param, env);

            if (subst == null)
            {
                Session.error(loc, "internal error: failed to bind var {0} to param {1}",
                    dump(), param.dump());

                return SubstMap.EMPTY;
            }

            return subst;
        }

        // normal case
        return SubstMap.EMPTY;
    }
    
    public SubstMap unify(final Loc loc, Type other, final TypeEnv env)
    {
        other = other.deref();

        return kind.equals(other.getKind()) ?
            SubstMap.bindVar(loc, this, other.deref(), env) :
            null;
    }

    public boolean equiv(final Type other, final EquivState state)
    {
        assert false : "TypeVar.equiv()";
        return false;
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

        final TypeVar typeVar = (TypeVar)o;

        if (!name.equals(typeVar.name)) return false;
        if (!kind.equals(typeVar.kind)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + kind.hashCode();
        return result;
    }
}