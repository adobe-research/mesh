package compile.type.constraint;

import compile.Loc;
import compile.Pair;
import compile.type.*;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeInstantiator;

import java.util.Set;

/**
 * Constrains a typevar to be a tuple type with at least
 * the given prefix set of slots.
 */
public final class TupleConstraint implements Constraint
{
    private final Type tup;

    public TupleConstraint(final TypeList members)
    {
        this.tup = Types.tup(members.getLoc(), members);
    }

    // Constraint

    public Pair<? extends Constraint, SubstMap> merge(
        final Constraint constraint, final TypeEnv env)
    {
        if (constraint == Constraint.ANY)
            return Pair.create(this, SubstMap.EMPTY);

        if (!(constraint instanceof TupleConstraint))
            return null;

        final TupleConstraint tupleConstraint = (TupleConstraint)constraint;

        final TypeList members = (TypeList)Types.tupMembers(tup);
        final TypeList otherMembers = (TypeList)Types.tupMembers(tupleConstraint.tup);

        final Pair<TypeList, SubstMap> merged = otherMembers.merge(members, env);

        if (merged == null)
            return null;

        return Pair.create(new TupleConstraint(merged.left), merged.right);
    }

    public SubstMap satisfy(final Loc loc, final Type type, final TypeEnv env)
    {
        if (!Types.isTup(type))
            return null;

        final TypeList members = (TypeList)Types.tupMembers(tup);
        final TypeList otherMembers = (TypeList)Types.tupMembers(type);

        return otherMembers.subsume(loc, members, env);
    }

    public Constraint subst(final SubstMap substMap)
    {
        final Type fields = Types.tupMembers(tup);
        final Type subst = fields.subst(substMap);

        return fields == subst ? this :
            new TupleConstraint((TypeList)subst);
    }

    public Constraint instance(final TypeInstantiator inst)
    {
        final Type instance = tup.accept(inst);

        return instance == tup ? this :
            new TupleConstraint((TypeList)Types.tupMembers(instance));
    }

    public Set<TypeVar> getVars()
    {
        return tup.getVars();
    }

    // Dumpable

    public String dump()
    {
        return tup.dump();
    }
}
