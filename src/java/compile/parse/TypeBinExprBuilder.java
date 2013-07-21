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
package compile.parse;

import compile.Loc;
import compile.type.Type;
import compile.type.TypeRef;
import compile.type.TypeTuple;
import compile.type.Types;
import compile.Pair;

import java.util.List;

/**
 * Binary expr builder for type terms.
 *
 * @author Basil Hosmer
 */
public class TypeBinExprBuilder extends BinExprBuilder<Type>
{
    private static TypeBinExprBuilder INSTANCE = new TypeBinExprBuilder();

    /**
     * static entry point
     */
    public static Type build(final Type head, final List<Pair<Object, Type>> tail)
    {
        return INSTANCE.buildBinExpr(head, tail);
    }

    @Override
    protected Type buildApply(final Object op, final Type lhs, final Type rhs)
    {
        final Loc loc = lhs.getLoc();

        final Type base = new TypeRef(loc, getOpInfo(op).func);

        final Type arg = new TypeTuple(loc, lhs, rhs);

        return Types.app(loc, base, arg);
    }

    @Override
    protected BinopInfo getOpInfo(final Object op)
    {
        return op instanceof String ?
            Ops.TYPE_BINOP_INFO.get(op) : Ops.DEFAULT_BINOP_INFO;
    }
}
