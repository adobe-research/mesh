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
package runtime.intrinsic;

import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;

/**
 * while combinator.
 * run a block repeatedly while guard predicate returns true.
 *
 * @author Basil Hosmer
 */
public final class While extends IntrinsicLambda
{
    public static final String NAME = "while";

    private static final Type T = new TypeParam("T");
    private static final Type PRED = Types.fun(Types.unit(), Types.BOOL);
    private static final Type BODY = Types.fun(Types.unit(), T);
    public static final Type TYPE =
        Types.fun(Types.tup(PRED, BODY), Types.unit());

    public String getName()
    {
        return NAME;
    }

    public Type getType()
    {
        return TYPE;
    }

    public Object apply(final Object arg)
    {
        final Tuple args = (Tuple)arg;
        return invoke((Lambda)args.get(0), (Lambda)args.get(1));
    }

    public static Tuple invoke(final Lambda pred, final Lambda f)
    {
        final Tuple unit = Tuple.UNIT;

        while ((Boolean)pred.apply(unit))
            f.apply(unit);

        return unit;
    }
}