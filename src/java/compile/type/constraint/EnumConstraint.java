package compile.type.constraint;

import compile.Loc;
import compile.Pair;
import compile.type.EnumType;
import compile.type.Type;
import compile.type.TypeEnv;
import compile.type.TypeVar;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeInstantiator;

import java.util.Collections;
import java.util.Set;

/**
 * Constrains a typevar to be a variant type with at least
 * the given set of cases.
 */
public final class EnumConstraint implements Constraint
{
    private final EnumType enumType;

    public EnumConstraint(final EnumType enumType)
    {
        this.enumType = enumType;
    }

    // Constraint

    public Pair<? extends Constraint, SubstMap> merge(
        final Constraint constraint, final TypeEnv env)
    {
        if (constraint == Constraint.ANY)
            return Pair.create(this, SubstMap.EMPTY);

        if (!(constraint instanceof EnumConstraint))
            return null;

        final EnumConstraint enumConstraint = (EnumConstraint)constraint;

        // NOTE: reverse order tends to accumulate constraints in code order,
        // given the polarity of unify() args in type checker. ugh
        final Pair<? extends EnumType, SubstMap> merged =
            enumConstraint.enumType.merge(enumType, env);

        if (merged == null)
            return null;

        return Pair.create(new EnumConstraint(merged.left), merged.right);
    }

    public SubstMap satisfy(final Loc loc, final Type type, final TypeEnv env)
    {
        if (type instanceof EnumType)
        {
            return ((EnumType)type).subsume(loc, this.enumType, env);
        }
        else
        {
            return type.unify(loc, this.enumType.getBaseType(), env);
        }
    }

    public Constraint subst(final SubstMap substMap)
    {
        return this;
    }

    public Constraint instance(final TypeInstantiator inst)
    {
        return this;
    }

    public Set<TypeVar> getVars()
    {
        return Collections.emptySet();
    }

    // Dumpable

    public String dump()
    {
        return enumType.dump();
    }
}
