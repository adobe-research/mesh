package compile.type.constraint;

import compile.Loc;
import compile.Pair;
import compile.type.*;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeInstantiator;

import java.util.Set;

/**
 * Constrains a typevar to be a record type with at least
 * the given set of fields.
 */
public final class RecordConstraint implements Constraint
{
    private final Type rec;

    public RecordConstraint(final TypeMap fields)
    {
        this.rec = Types.rec(fields);
    }

    // Constraint

    public Pair<? extends Constraint, SubstMap> merge(
        final Constraint constraint, final TypeEnv env)
    {
        if (constraint == Constraint.ANY)
            return Pair.create(this, SubstMap.EMPTY);

        if (!(constraint instanceof RecordConstraint))
            return null;

        final RecordConstraint recordConstraint = (RecordConstraint)constraint;

        final TypeMap fields = (TypeMap)Types.recFields(rec);
        final TypeMap otherFields = (TypeMap)Types.recFields(recordConstraint.rec);

        // NOTE: reverse order tends to accumulate constraints in code order,
        // given the polarity of unify() args in type checker. ugh
        final Pair<TypeMap, SubstMap> merged = otherFields.merge(fields, env);

        if (merged == null)
            return null;

        return Pair.create(new RecordConstraint(merged.left), merged.right);
    }

    public SubstMap satisfy(final Loc loc, final Type type, final TypeEnv env)
    {
        if (!Types.isRec(type))
            return null;

        final TypeMap fields = (TypeMap)Types.recFields(rec);
        final TypeMap otherFields = (TypeMap)Types.recFields(type);

        return otherFields.subsume(loc, fields, env);
    }

    public Constraint subst(final SubstMap substMap)
    {
        final Type fields = Types.recFields(rec);
        final Type subst = fields.subst(substMap);

        return fields == subst ? this :
            new RecordConstraint((TypeMap)subst);
    }

    public Constraint instance(final TypeInstantiator inst)
    {
        final Type instance = rec.accept(inst);

        return instance == rec ? this :
            new RecordConstraint((TypeMap)Types.recFields(instance));
    }

    public Set<TypeVar> getVars()
    {
        return rec.getVars();
    }

    // Dumpable

    public String dump()
    {
        return rec.dump();
    }
}
