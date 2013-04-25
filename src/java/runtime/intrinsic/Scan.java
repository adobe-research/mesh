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
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.Tuple;

/**
 * Like reduce, but accumulates intermediate results into list.
 *
 * @author Basil Hosmer
 */
public final class Scan extends IntrinsicLambda
{
    public static final String NAME = "scan";

    private static final Type A = new TypeParam("A");
    private static final Type B = new TypeParam("B");
    private static final Type LIST_B = Types.list(B);
    private static final Type FUNC_AB_A = Types.fun(Types.tup(A, B), A);
    private static final Type LIST_A = Types.list(A);

    public static final Type TYPE = Types.fun(Types.tup(FUNC_AB_A, A, LIST_B), LIST_A);

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
        return invoke((Lambda)args.get(0), args.get(1), (ListValue)args.get(2));
    }

    public static ListValue invoke(final Lambda f, final Object init,
        final ListValue list)
    {
        final PersistentList result = PersistentList.alloc(list.size());

        int i = 0;
        Object fret = init;

        for (final Object item : list)
        {
            fret = f.apply(Tuple.from(fret, item));
            result.updateUnsafe(i++, fret);
        }

        return result;
    }
}