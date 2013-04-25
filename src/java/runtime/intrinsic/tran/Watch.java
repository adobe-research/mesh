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
package runtime.intrinsic.tran;

import compile.type.Type;
import compile.type.TypeParam;
import compile.type.Types;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.lambda.Lambda;
import runtime.rep.Tuple;
import runtime.tran.Box;

/**
 * Add a watcher to a box, return watcher.
 *
 * @author Basil Hosmer
 */
public final class Watch extends IntrinsicLambda
{
    public static final String NAME = "watch";

    private static final TypeParam T = new TypeParam("T");
    private static final TypeParam X = new TypeParam("X");
    private static final Type BOX_T = Types.box(T);
    private static final Type PAIR_T = Types.tup(T, T);
    private static final Type WATCH_FUNC = Types.fun(PAIR_T, X);
    private static final Type PARAM_TYPE = Types.tup(BOX_T, WATCH_FUNC);
    public static final Type TYPE = Types.fun(PARAM_TYPE, WATCH_FUNC);

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
        return invoke((Box)args.get(0), (Lambda)args.get(1));
    }

    public static Lambda invoke(final Box box, final Lambda watcher)
    {
        box.addWatcher(watcher);
        return watcher;
    }
}