package compile.type.constraint;

import compile.Loc;
import compile.Pair;
import compile.type.*;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeInstantiator;

import java.util.Set;

/**
 * Constrains a typevar to be a variant type with at least
 * the given set of cases.
 */
public final class VariantConstraint implements Constraint
{
    private final Type var;

    public VariantConstraint(final TypeMap fields)
    {
        this.var = Types.var(fields);
    }

    // Constraint

    public Pair<? extends Constraint, SubstMap> merge(
        final Constraint constraint, final TypeEnv env)
    {
        if (constraint == Constraint.ANY)
            return Pair.create(this, SubstMap.EMPTY);

        if (!(constraint instanceof VariantConstraint))
            return null;

        final VariantConstraint variantConstraint = (VariantConstraint)constraint;

        final TypeMap opts = (TypeMap)Types.varOpts(var);
        final TypeMap otherOpts = (TypeMap)Types.varOpts(variantConstraint.var);

        // NOTE: reverse order tends to accumulate constraints in code order,
        // given the polarity of unify() args in type checker. ugh
        final Pair<TypeMap, SubstMap> merged = otherOpts.merge(opts, env);

        if (merged == null)
            return null;

        return Pair.create(new VariantConstraint(merged.left), merged.right);
    }

    public SubstMap satisfy(final Loc loc, final Type type, final TypeEnv env)
    {
        if (!Types.isVar(type))
            return null;

        final TypeMap opts = (TypeMap)Types.varOpts(var);
        final TypeMap otherOpts = (TypeMap)Types.varOpts(type);

        return otherOpts.subsume(loc, opts, env);
    }

    public Constraint subst(final SubstMap substMap)
    {
        final Type opts = Types.varOpts(var);
        final Type subst = opts.subst(substMap);

        return opts == subst ? this :
            new VariantConstraint((TypeMap)subst);
    }

    public Constraint instance(final TypeInstantiator inst)
    {
        final Type instance = var.accept(inst);

        return instance == var ? this :
            new VariantConstraint((TypeMap)Types.varOpts(instance));
    }

    public Set<TypeVar> getVars()
    {
        return var.getVars();
    }

    // Dumpable

    public String dump()
    {
        return var.dump();
    }
}
