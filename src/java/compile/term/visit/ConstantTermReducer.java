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
package compile.term.visit;

import compile.Session;
import compile.analyze.ConstantReducer;
import compile.term.*;
import compile.term.reduce.*;
import runtime.intrinsic.Intrinsics;

import java.util.HashMap;

/**
 * Simple constant folding and propagation within terms.
 * {@link ConstantReducer} calls us to do in-term reduction
 * in dependency order over an entire module.
 * We recurse back out to process the bodies of lambda
 * literals encountered within expressions.
 *
 * As with inlining, only implemented a few of the usual
 * suspects here. TODO the rest.
 *
 * @author Basil Hosmer
 */
public class ConstantTermReducer extends TermTransformerBase
{
    private final static HashMap<String, ApplyReducer> APPLY_REDUCERS =
        new HashMap<String, ApplyReducer>();

    static
    {
        // int arith
        APPLY_REDUCERS.put(Intrinsics._plus.getName(), new IntPlusReducer());
        APPLY_REDUCERS.put(Intrinsics._div.getName(), new DivReducer());
        APPLY_REDUCERS.put(Intrinsics._max.getName(), new MaxReducer());
        APPLY_REDUCERS.put(Intrinsics._min.getName(), new MinReducer());
        APPLY_REDUCERS.put(Intrinsics._minus.getName(), new MinusReducer());
        APPLY_REDUCERS.put(Intrinsics._mod.getName(), new ModReducer());
        APPLY_REDUCERS.put(Intrinsics._sign.getName(), new SignReducer());
        APPLY_REDUCERS.put(Intrinsics._times.getName(), new TimesReducer());

        // double arith
        APPLY_REDUCERS.put(Intrinsics._fdiv.getName(), new FDivReducer());
        APPLY_REDUCERS.put(Intrinsics._fminus.getName(), new FMinusReducer());
        APPLY_REDUCERS.put(Intrinsics._ftimes.getName(), new FTimesReducer());
        APPLY_REDUCERS.put(Intrinsics._sqrt.getName(), new SqrtReducer());

        // converters
        APPLY_REDUCERS.put(Intrinsics._f2i.getName(), new F2IReducer());
        APPLY_REDUCERS.put(Intrinsics._i2f.getName(), new I2FReducer());

        // logic
        APPLY_REDUCERS.put(Intrinsics._and.getName(), new AndReducer());
        APPLY_REDUCERS.put(Intrinsics._if.getName(), new IfReducer());
        APPLY_REDUCERS.put(Intrinsics._or.getName(), new OrReducer());

        // misc
        APPLY_REDUCERS.put(Intrinsics._size.getName(), new SizeReducer());
    }

    // instance

    private final ConstantReducer scopeReducer;

    public ConstantTermReducer(final ConstantReducer scopeReducer)
    {
        this.scopeReducer = scopeReducer;
    }

    /**
     * Return a term with constants folded and propagated,
     * or original.
     */
    public final Term reduce(final Term term)
    {
        return visitTerm(term);
    }

    /**
     * Read through refs to inlineable constants.
     * Reachable lets have already been simplified.
     * See {@link compile.analyze.ConstantReducer}.
     */
    @Override
    public Term visit(final RefTerm ref)
    {
        final Binding binding = ref.getBinding();

        if (!binding.isLet())
            return ref;

        final LetBinding let = (LetBinding)binding;

        // for intrinsics, we have nothing to dereference until CG time.
        if (let.isIntrinsic())
            return ref;

        final Term rhs = let.getValue();

        if (rhs.isConstant() &&
            (rhs instanceof BoolLiteral ||
                rhs instanceof IntLiteral ||
                rhs instanceof LongLiteral ||
                // rhs instanceof FloatLiteral ||
                rhs instanceof DoubleLiteral))
        {
            if (Session.isDebug())
                Session.debug("const reduction {0} -> {1}", let.getName(), rhs.dump());

            return rhs;
        }

        return ref;
    }

    /**
     * Run lambda literals through our scope reducer.
     *
     */
    @Override
    public Term visit(final LambdaTerm lambda)
    {
        scopeReducer.visit(lambda);
        return lambda;
    }

    /**
     * Reduce applications.
     */
    @Override
    public Term visit(final ApplyTerm apply)
    {
        final Term base = apply.getBase();
        final Term arg = apply.getArg();

        final Term newBase = visitTerm(base);
        final Term newArg = visitTerm(arg);

        if (newBase instanceof RefTerm)
        {
            final RefTerm ref = (RefTerm)newBase;
            if (ref.getBinding().isLet())
            {
                final LetBinding let = (LetBinding)ref.getBinding();
                if (let.isIntrinsic())
                {
                    final ApplyReducer reducer = APPLY_REDUCERS.get(let.getName());
                    if (reducer != null)
                    {
                        final Term reduced = reducer.reduce(newArg);

                        if (reduced != null)
                        {
                            //  convenience, but should maybe insist that reducers do it
                            if (reduced.getType() == null)
                                reduced.setType(apply.getType());

                            if (Session.isDebug())
                                Session.debug(apply.getLoc(),
                                    "const reduction {0} -> {1}",
                                    apply.dump(), reduced.dump());

                            return reduced;
                        }
                    }
                }
            }
        }

        if (base == newBase && arg == newArg)
            return apply;

        final ApplyTerm newApply =
            new ApplyTerm(apply.getLoc(), newBase, newArg, apply.getFlav());

        newApply.setType(apply.getType());

        if (Session.isDebug())
            Session.debug(apply.getLoc(), "const reduction {0} -> {1}",
                apply.dump(), newApply.dump());

        return newApply;
    }
}
