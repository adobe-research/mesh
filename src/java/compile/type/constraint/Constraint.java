package compile.type.constraint;

import compile.Dumpable;
import compile.Loc;
import compile.Pair;
import compile.type.Type;
import compile.type.TypeEnv;
import compile.type.TypeVar;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeInstantiator;

import java.util.Collections;
import java.util.Set;

/**
 *
 */
public interface Constraint extends Dumpable
{
    static Constraint ANY = new Constraint()
    {
        public SubstMap satisfy(final Loc loc, final Type type, final TypeEnv env)
        {
            return SubstMap.EMPTY;
        }

        public Pair<Constraint, SubstMap> merge(final Constraint constraint,
            final TypeEnv env)
        {
            return Pair.create(constraint, SubstMap.EMPTY);
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

        public String dump()
        {
            return "*";
        }
    };

    /**
     *
     */
    Pair<? extends Constraint, SubstMap> merge(Constraint constraint, TypeEnv env);

    /**
     *
     */
    SubstMap satisfy(Loc loc, Type type, TypeEnv env);

    /**
     *
     */
    Constraint subst(SubstMap subtMap);

    /**
     *
     */
    Constraint instance(TypeInstantiator inst);

    /**
     *
     */
    Set<TypeVar> getVars();
}
