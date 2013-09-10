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
package compile.gen.java.inline;

import compile.Session;
import compile.gen.java.InvokeInfo;
import compile.gen.java.StatementFormatter;
import compile.term.ApplyTerm;
import compile.term.Term;
import compile.term.TupleTerm;
import compile.type.Types;
import runtime.sys.ConfigUtils;
import runtime.intrinsic.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Inlining utility for apply terms. {@link #tryInlining} takes an
 * {@link ApplyTerm}, some {@link InvokeInfo resolution information} about
 * the application target, and a {@link StatementFormatter}, and attempts
 * to generate inlined code for the term.
 * <p/>
 * We're only inlining a handful of the usual suspects currently.
 * TODO the rest, ideally after the TC functionality has settled.
 *
 * @author Basil Hosmer
 */
public class TermInliner
{
    /**
     * system property enables/disables CG inlining
     */
    private static final boolean ENABLED =
        ConfigUtils.parseBoolProp(TermInliner.class.getName() + ".ENABLED", true);

    /**
     * Inliners mapped by intrinsic function implementation class name.
     * This is the name supplied by {@link InvokeInfo} when a direct call to an
     * intrinsic function has been resolved.
     */
    private static final Map<String, Inliner> INTRINSICS = new HashMap<String, Inliner>();

    static
    {
        // control flow
        INTRINSICS.put(_for.class.getName(), new ForInliner());
        INTRINSICS.put(_guard.class.getName(), new GuardInliner());
        INTRINSICS.put(_if.class.getName(), new IfInliner());
        INTRINSICS.put(_map.class.getName(), new MapInliner());
        INTRINSICS.put(_reduce.class.getName(), new ReduceInliner());
        INTRINSICS.put(_when.class.getName(), new WhenInliner());
        INTRINSICS.put(_while.class.getName(), new WhileInliner());

        // lists
        INTRINSICS.put(_size.class.getName(), new SizeInliner());
        INTRINSICS.put(_zip.class.getName(), new ZipInliner());

        // logic
        INTRINSICS.put(_not.class.getName(), new NotInliner());
        INTRINSICS.put(_and.class.getName(), new AndInliner());
        INTRINSICS.put(_or.class.getName(), new OrInliner());

        // poly
        INTRINSICS.put(_eq.class.getName(), new EQInliner());
        INTRINSICS.put(_ne.class.getName(), new NEInliner());
        INTRINSICS.put(_plus.class.getName(), new PlusInliner());

        // int arith
        INTRINSICS.put(_minus.class.getName(), new MinusInliner());
        INTRINSICS.put(_times.class.getName(), new TimesInliner());
        INTRINSICS.put(_div.class.getName(), new DivInliner());
        INTRINSICS.put(_max.class.getName(), new MaxInliner());
        INTRINSICS.put(_min.class.getName(), new MinInliner());
        INTRINSICS.put(_mod.class.getName(), new ModInliner());
        INTRINSICS.put(_neg.class.getName(), new NegInliner());
        INTRINSICS.put(_pow.class.getName(), new PowInliner());

        // int bitops
        INTRINSICS.put(_band.class.getName(), new BandInliner());
        INTRINSICS.put(_bor.class.getName(), new BorInliner());
        INTRINSICS.put(_bxor.class.getName(), new BxorInliner());
        INTRINSICS.put(_shiftr.class.getName(), new ShiftrInliner());
        INTRINSICS.put(_ushiftr.class.getName(), new UshiftrInliner());
        INTRINSICS.put(_shiftl.class.getName(), new ShiftlInliner());

        // int relops
        INTRINSICS.put(_gt.class.getName(), new GTInliner());
        INTRINSICS.put(_ge.class.getName(), new GEInliner());
        INTRINSICS.put(_le.class.getName(), new LEInliner());
        INTRINSICS.put(_lt.class.getName(), new LTInliner());

        // double arith
        INTRINSICS.put(_fminus.class.getName(), new FMinusInliner());
        INTRINSICS.put(_ftimes.class.getName(), new FTimesInliner());
        INTRINSICS.put(_fdiv.class.getName(), new FDivInliner());
        INTRINSICS.put(_fmod.class.getName(), new FModInliner());
        INTRINSICS.put(_fneg.class.getName(), new FNegInliner());
        INTRINSICS.put(_fpow.class.getName(), new FPowInliner());
        INTRINSICS.put(_ln.class.getName(), new LnInliner());
        INTRINSICS.put(_sqrt.class.getName(), new SqrtInliner());

        // trig
        INTRINSICS.put(_atan2.class.getName(), new ATan2Inliner());
        INTRINSICS.put(_cos.class.getName(), new CosInliner());
        INTRINSICS.put(_sin.class.getName(), new SinInliner());
        INTRINSICS.put(_tan.class.getName(), new TanInliner());

        // double relops
        INTRINSICS.put(_fgt.class.getName(), new FGTInliner());
        INTRINSICS.put(_fge.class.getName(), new FGEInliner());
        INTRINSICS.put(_fle.class.getName(), new FLEInliner());
        INTRINSICS.put(_flt.class.getName(), new FLTInliner());

        // converters
        INTRINSICS.put(_i2f.class.getName(), new I2FInliner());
        INTRINSICS.put(_f2i.class.getName(), new F2IInliner());
    }

    /**
     * Try to inline an apply term. Return inlined code or null.
     */
    public static String tryInlining(final ApplyTerm apply,
        final InvokeInfo info, final StatementFormatter fmt)
    {
        if (!ENABLED)
            return null;

        // if arg type is a tuple, arg term must be a tuple literal.
        // (we only try inlining multi-arg call sites if their args
        // are scattered)
        final Term arg = apply.getArg();
        if (Types.isTup(arg.getType()) && !(arg instanceof TupleTerm))
            return null;

        if (info.lambda == null)
        {
            // intrinsic
            final Inliner inliner = INTRINSICS.get(info.className);

            final String inlined = inliner != null ?
                inliner.tryInlining(apply, fmt, fmt.statementsOkay(apply)) :
                null;

            if (Session.isDebug())
                if (inlined != null)
                    Session.debug(apply.getLoc(), "inlined: {0} => {1}",
                        apply.dump(), inlined);

            return inlined;
        }
        else
        {
            return tryNonIntrinsic(apply, info, fmt);
        }
    }

    /**
     * Non-intrinsic inlining entry point.
     * Normally non-intrinsics are inlined prior to CG, but
     * this entry point might turn out to be useful for something.
     */
    @SuppressWarnings("UnusedParameters")
    private static String tryNonIntrinsic(final ApplyTerm apply,
        final InvokeInfo info, final StatementFormatter fmt)
    {
        return null;
    }
}
