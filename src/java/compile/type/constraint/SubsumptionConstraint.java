package compile.type.constraint;

import compile.Loc;
import compile.Pair;
import compile.Session;
import compile.type.*;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeInstantiator;

import java.util.Set;

/**
 * Constrains a typevar to range over the set of types
 * which subsume the type given by the constraint.
 */
public final class SubsumptionConstraint implements Constraint
{
    private final Type type;

    public SubsumptionConstraint(final Type type)
    {
        this.type = type;
    }

    // Constraint

    public Pair<? extends Constraint, SubstMap> merge(
        final Constraint constraint, final TypeEnv env)
    {
        if (constraint == Constraint.ANY)
            return Pair.create(this, SubstMap.EMPTY);

        if (!(constraint instanceof SubsumptionConstraint))
            return null;

        final Type otherType = ((SubsumptionConstraint)constraint).type;

        if (Types.isVar(type) && Types.varOpts(type) instanceof TypeMap &&
            Types.isVar(otherType) && Types.varOpts(otherType) instanceof TypeMap)
        {
            final TypeMap opts = (TypeMap)Types.varOpts(type);
            final TypeMap otherOpts = (TypeMap)Types.varOpts(otherType);

            // NOTE: reverse order tends to accumulate constraints in code order,
            // given the polarity of unify() args in type checker. ugh
            final Pair<TypeMap, SubstMap> merged = otherOpts.merge(opts, env);

            if (merged == null)
                return null;

            return Pair.create(new SubsumptionConstraint(merged.left), merged.right);
        }
        else if (Types.isRec(type) && Types.recFields(type) instanceof TypeMap &&
            Types.isRec(otherType) && Types.recFields(otherType) instanceof TypeMap)
        {
            final TypeMap fields = (TypeMap)Types.recFields(type);
            final TypeMap otherFields = (TypeMap)Types.recFields(otherType);

            // NOTE: reverse order tends to accumulate constraints in code order,
            // given the polarity of unify() args in type checker. ugh
            final Pair<TypeMap, SubstMap> merged = otherFields.merge(fields, env);

            if (merged == null)
                return null;

            return Pair.create(new SubsumptionConstraint(merged.left), merged.right);
        }
        else if (type instanceof TypeMap && otherType instanceof TypeMap)
        {
            final TypeMap map = (TypeMap)type;
            final TypeMap otherMap = (TypeMap)otherType;

            // NOTE: reverse order tends to accumulate constraints in code order,
            // given the polarity of unify() args in type checker. ugh
            final Pair<TypeMap, SubstMap> merged = otherMap.merge(map, env);

            if (merged == null)
                return null;

            return Pair.create(new SubsumptionConstraint(merged.left), merged.right);
        }
        else if (type instanceof TypeList && otherType instanceof TypeList)
        {
            final TypeList list = (TypeList)type;
            final TypeList otherList = (TypeList)otherType;

            // NOTE: reverse order tends to accumulate constraints in code order,
            // given the polarity of unify() args in type checker. ugh
            final Pair<TypeList, SubstMap> merged = otherList.merge(list, env);

            if (merged == null)
                return null;

            return Pair.create(new SubsumptionConstraint(merged.left), merged.right);
        }
        else if (type instanceof EnumType && otherType instanceof EnumType)
        {
            final EnumType enumType = (EnumType)type;
            final EnumType otherEnum = (EnumType)otherType;

            // NOTE: reverse order tends to accumulate constraints in code order,
            // given the polarity of unify() args in type checker. ugh
            final Pair<EnumType, SubstMap> merged = otherEnum.merge(enumType, env);

            if (merged == null)
                return null;

            return Pair.create(new SubsumptionConstraint(merged.left), merged.right);
        }
        else
        {
            Session.error(
                "SubsumptionConstraint: {0} merge {1}: Type.merge not implemented",
                dump(), constraint.dump());

            return null;

            /*
            // NOTE: reverse order tends to accumulate constraints in code order,
            // given the polarity of unify() args in type checker. ugh
            final Pair<? extends Type, SubstMap> merged = other.type.merge(type, env);

            if (merged == null)
                return null;

            return Pair.create(new SubsumptionConstraint(merged.left), merged.right);
            */
        }
    }

    public SubstMap satisfy(final Loc loc, final Type other, final TypeEnv env)
    {
        if (Session.isDebug())
            Session.debug(loc, "({0}).satisfy({1})", dump(), other.dump());

        return other.subsume(loc, type, env);
    }

    public Constraint subst(final SubstMap substMap)
    {
        final Type subst = type.subst(substMap);
        return type == subst ? this : new SubsumptionConstraint(subst);
    }

    public Constraint instance(final TypeInstantiator inst)
    {
        final Type instance = type.accept(inst);
        return instance == type ? this : new SubsumptionConstraint(instance);
    }

    public Set<TypeVar> getVars()
    {
        return type.getVars();
    }

    // Dumpable

    public String dump()
    {
        return type.dump();
    }
}
