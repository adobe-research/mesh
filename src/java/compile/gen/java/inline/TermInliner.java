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
import runtime.ConfigUtils;

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
        INTRINSICS.put(runtime.intrinsic.For.class.getName(), new ForInliner());
        INTRINSICS.put(runtime.intrinsic.Guard.class.getName(), new GuardInliner());
        INTRINSICS.put(runtime.intrinsic.If.class.getName(), new IfInliner());
        INTRINSICS.put(runtime.intrinsic.When.class.getName(), new WhenInliner());
        INTRINSICS.put(runtime.intrinsic.While.class.getName(), new WhileInliner());

        // logic
        INTRINSICS.put(runtime.intrinsic.Not.class.getName(), new NotInliner());
        INTRINSICS.put(runtime.intrinsic.And.class.getName(), new AndInliner());
        INTRINSICS.put(runtime.intrinsic.Or.class.getName(), new OrInliner());

        // poly
        INTRINSICS.put(runtime.intrinsic.EQ.class.getName(), new EQInliner());
        INTRINSICS.put(runtime.intrinsic.NE.class.getName(), new NEInliner());
        INTRINSICS.put(runtime.intrinsic.Plus.class.getName(), new PlusInliner());

        // int arith
        INTRINSICS.put(runtime.intrinsic.Minus.class.getName(), new MinusInliner());
        INTRINSICS.put(runtime.intrinsic.Times.class.getName(), new TimesInliner());
        INTRINSICS.put(runtime.intrinsic.Div.class.getName(), new DivInliner());
        INTRINSICS.put(runtime.intrinsic.Max.class.getName(), new MaxInliner());
        INTRINSICS.put(runtime.intrinsic.Min.class.getName(), new MinInliner());
        INTRINSICS.put(runtime.intrinsic.Mod.class.getName(), new ModInliner());
        INTRINSICS.put(runtime.intrinsic.Neg.class.getName(), new NegInliner());
        INTRINSICS.put(runtime.intrinsic.Pow.class.getName(), new PowInliner());
        INTRINSICS.put(runtime.intrinsic.Sign.class.getName(), new SignInliner());

        // int relops
        INTRINSICS.put(runtime.intrinsic.GT.class.getName(), new GTInliner());
        INTRINSICS.put(runtime.intrinsic.GE.class.getName(), new GEInliner());
        INTRINSICS.put(runtime.intrinsic.LE.class.getName(), new LEInliner());
        INTRINSICS.put(runtime.intrinsic.LT.class.getName(), new LTInliner());

        // double arith
        INTRINSICS.put(runtime.intrinsic.FMinus.class.getName(), new FMinusInliner());
        INTRINSICS.put(runtime.intrinsic.FTimes.class.getName(), new FTimesInliner());
        INTRINSICS.put(runtime.intrinsic.FDiv.class.getName(), new FDivInliner());
        INTRINSICS.put(runtime.intrinsic.FMod.class.getName(), new FModInliner());
        INTRINSICS.put(runtime.intrinsic.FNeg.class.getName(), new FNegInliner());
        INTRINSICS.put(runtime.intrinsic.FPow.class.getName(), new FPowInliner());
        INTRINSICS.put(runtime.intrinsic.Ln.class.getName(), new LnInliner());
        INTRINSICS.put(runtime.intrinsic.Sqrt.class.getName(), new SqrtInliner());

        // trig
        INTRINSICS.put(runtime.intrinsic.ATan2.class.getName(), new ATan2Inliner());
        INTRINSICS.put(runtime.intrinsic.Cos.class.getName(), new CosInliner());
        INTRINSICS.put(runtime.intrinsic.Sin.class.getName(), new SinInliner());
        INTRINSICS.put(runtime.intrinsic.Tan.class.getName(), new TanInliner());

        // double relops
        INTRINSICS.put(runtime.intrinsic.FGT.class.getName(), new FGTInliner());
        INTRINSICS.put(runtime.intrinsic.FGE.class.getName(), new FGEInliner());
        INTRINSICS.put(runtime.intrinsic.FLE.class.getName(), new FLEInliner());
        INTRINSICS.put(runtime.intrinsic.FLT.class.getName(), new FLTInliner());

        // converters
        INTRINSICS.put(runtime.intrinsic.I2F.class.getName(), new I2FInliner());
        INTRINSICS.put(runtime.intrinsic.F2I.class.getName(), new F2IInliner());
    }

    /**
     * Try to inline an apply term. Return inlined code or null.
     */
    public static String tryInlining(final ApplyTerm apply,
        final InvokeInfo info, final StatementFormatter fmt)
    {
        if (!ENABLED)
            return null;

        // if arg type is a tuple, arg term must be a tuple literal
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
