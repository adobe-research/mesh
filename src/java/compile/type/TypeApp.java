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
import compile.Session;
import compile.type.kind.ArrowKind;
import compile.type.kind.Kind;
import compile.type.visit.EquivState;
import compile.type.visit.SubstMap;
import compile.type.visit.TypeApplier;
import compile.type.visit.TypeVisitor;

import java.util.Collection;
import java.util.Iterator;

/**
 * Type application term.
 *
 * @author Basil Hosmer
 */
public final class TypeApp extends ScopeType
{
    private final Type base;
    private Type arg;
    private Kind kind;

    // eval stuff--to be cleaned up
    private Type reduced;   // cached result of application
    private boolean inEval;

    public TypeApp(final Loc loc, final Type base, final Type arg, final Kind kind)
    {
        super(loc);
        this.base = base;
        this.arg = arg;
        this.kind = kind;
    }

    public TypeApp(final Loc loc, final Type base, final Type arg)
    {
        this(loc, base, arg, null);

        // NOTE: optimistically set kind to base kind's result kind,
        // if available
        final Kind baseKind = base.getKind();
        if (baseKind != null && baseKind instanceof ArrowKind)
        {
            setKind(((ArrowKind)baseKind).getResultKind());
        }
    }

    public Type getBase()
    {
        return base;
    }

    public Type getArg()
    {
        return arg;
    }

    /**
     * NOTE: not really public, called in
     * {@link compile.analyze.KindChecker#visit(TypeApp)} to shim in coercions
     */
    public void setArg(final Type arg)
    {
        this.arg = arg;
    }

    /**
     * Note: may be set to {@link compile.type.kind.Kinds#STAR}
     * in error situations, so no checking for consistency here.
     */
    public void setKind(final Kind kind)
    {
        this.kind = kind;
    }

    /**
     * true if our base term is a type abstraction,
     * rather than a constructor
     */
    public boolean isAbsApply()
    {
        final Type baseDeref = base.deref();

        return baseDeref instanceof TypeCons &&
            ((TypeCons)baseDeref).getBody() != null;
    }

    /**
     *
     */
    public Type eval()
    {
        if (reduced != null)
            return reduced;

        if (inEval)
            return this;

        final Type baseEval = base.deref().eval();
        final Type argEval = arg.deref().eval();

        if (!(baseEval instanceof TypeCons))
            // error has been raised
            return this;

        final TypeCons cons = (TypeCons)baseEval;

        final Type body = cons.getBody();
        if (body == null)
            return reduced(baseEval, argEval);

        // check base abs param kind against arg kind
        if (!checkKindAgreement(cons, argEval))
            // error has been raised
            return this;

        // evaluate by building param->arg subst map and
        // applying it to body term

        final SubstMap argMap = new SubstMap();

        final Collection<TypeParam> params = body.getParams().values();

        if (params.size() == 1)
        {
            final TypeParam param = params.iterator().next();
            assert param.getTypeScope() == body : "nope";

            argMap.put(param, argEval);
        }
        else
        {
            if (!(argEval instanceof TypeTuple))
                assert false;

            final Iterator<Type> argList =
                ((TypeTuple)argEval).getMembers().iterator();

            for (final TypeParam param : params)
            {
                assert param.getTypeScope() == body : "nope";
                argMap.put(param, argList.next());
            }
        }

        final Type bodySubst = new TypeApplier(body, argMap).apply();

        // evaluate body with args substituted for params
        inEval = true;
        reduced = bodySubst.eval();
        inEval = false;

        if (Session.isDebug())
            Session.debug(body.getLoc(), "eval {0}({1}) => {2}",
                body.dump(), argMap.dump(), reduced.dump());

        return reduced;
    }

    /**
     * helper--return self-reference if given base and arg are
     * the same as ours, otherwise new TypeApp
     */
    private Type reduced(final Type base, final Type arg)
    {
        return base == this.base && arg == this.arg ? this :
            new TypeApp(loc, base, arg, kind);
    }

    /**
     * internal reality checks on param/arg kinds
     */
    private boolean checkKindAgreement(final Type abs, final Type argDeref)
    {
        final Kind absKind = abs.getKind();

        if (!(absKind instanceof ArrowKind))
        {
            Session.error(loc,
                "internal error: abs {0} has non-lambda kind {1} in type app {2}",
                abs.dump(), absKind.dump(), dump());

            return false;
        }

        final Kind paramKind = ((ArrowKind)absKind).getParamKind();
        final Kind argKind = argDeref.getKind();

        if (!paramKind.equals(argKind))
        {
            Session.error(loc,
                "internal error: param kind {0} incompatible with arg kind {1} in type app {2}",
                paramKind.dump(), argKind.dump(), dump());

            return false;
        }

        return true;
    }

    // Type

    public Type deref()
    {
        return this;
    }

    public Kind getKind()
    {
        return kind;
    }

    public SubstMap unify(final Loc loc, final Type other, final TypeEnv env)
    {
        if (env.checkVisited(this, other))
            return SubstMap.EMPTY;

        if (other instanceof TypeVar)
            return SubstMap.bindVar(loc, (TypeVar)other, this, env);

        // if our application expression can be evaluated, unify against that.
        if (isAbsApply())
            return eval().unify(loc, other, env);

        final Type otherEval = other.deref().eval();

        if (otherEval instanceof TypeApp)
        {
            final TypeApp otherApp = (TypeApp)otherEval;
            final SubstMap baseSubst = base.unify(loc, otherApp.base, env);

            if (baseSubst != null)
            {
                final SubstMap argSubst =
                    arg.subst(baseSubst).unify(loc, otherApp.arg.subst(baseSubst), env);

                if (argSubst != null)
                    return baseSubst.compose(loc, argSubst);
            }
        }
        else if (kind.equals(otherEval.getKind()))
        {
            // other is not an application expression, but kinds match.
            // other type classes do opportunistic matching of type apps
            // implementations of unify. This approach is really ad-hoc,
            // needs to be rationalized.

            return otherEval.unify(loc, this, env);
        }

        return null;
    }

    public boolean equiv(final Type other, final EquivState state)
    {
        if (state.checkVisited(this, other))
            return true;

        if (isAbsApply())
        {
            return eval().equiv(other, state);
        }

        final Type otherEval = other.deref().eval();

        if (otherEval instanceof TypeApp)
        {
            final TypeApp otherApp = (TypeApp)otherEval;
            return base.equiv(otherApp.base, state) && arg.equiv(otherApp.arg, state);
        }

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
        if (!super.equals(o)) return false;

        final TypeApp typeApp = (TypeApp)o;

        if (!base.equals(typeApp.base)) return false;
        if (!arg.equals(typeApp.arg)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + base.hashCode();
        result = 31 * result + arg.hashCode();
        return result;
    }
}
