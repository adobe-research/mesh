package compile.type.constraint;

import compile.Loc;
import compile.Pair;
import compile.Session;
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

    // TODO what about non-typemap cases?
    public VariantConstraint(final TypeMap fields)
    {
        this.var = Types.var(fields.getLoc(), fields);
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
        if (Session.isDebug())
            Session.debug(loc, "({0}).satisfy({1})", dump(), type.dump());

        if (!Types.isVar(type))
            return null;

        final TypeMap opts = (TypeMap)Types.varOpts(var);

        final Type otherOpts = Types.varOpts(type);

        if (otherOpts instanceof TypeMap)
        {
            return otherOpts.subsume(loc, opts, env);
        }
        else if (otherOpts instanceof TypeApp)
        {
            Session.info("TypeApp otherOpts = {0}", otherOpts.dump());

            return otherOpts.subsume(loc, opts, env);

            /*
            final TypeApp otherOptsApp = (TypeApp)otherOpts;
            final Type base = otherOptsApp.getBase();

            if (!(base instanceof TypeCons))
            {
                if (Session.isDebug())
                    Session.debug(loc, "type app base {0} is not a type cons, fail",
                        base.dump());

                return null;
            }

            final TypeCons baseCons = (TypeCons)base;

            if (baseCons == Types.ASSOC)
            {
                final Type assocKey = Types.assocKey(otherOptsApp);
                final SubstMap keySubst =
                    assocKey.subsume(opts.getKeyType());

                if (keySubst == null)
                    return null;

                final Type assocVals = Types.assocVals(otherOptsApp).subst(keySubst);
                final SubstMap valsSubst =
                    assocVals.subsume(opts.getValueTypes().subst(keySubst));

                if (valsSubst == null)
                    return null;

                return keySubst.compose(loc, valsSubst);
            }

            if (Session.isDebug())
                Session.debug(loc, "type cons {0} is not handled, fail",
                    baseCons.dump());
            */
        }
        else
        {
            // Note that TypeVar is handled by caller--should probably be handled
            // in Constraint.satisfy() super-impl instead TODO

            Session.error(loc,
                "internal error in ({0}).satisfy({1}): : unhandled argument {2} to Var TC ",
                dump(), type.dump(), otherOpts.dump());

            return null;
        }
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
