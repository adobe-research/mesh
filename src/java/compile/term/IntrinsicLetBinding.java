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
package compile.term;

import compile.Loc;
import compile.module.Scope;
import compile.type.Type;

/**
 * Instances correspond directly to simple (non-decomposing) lets
 * in the surface language. Decomposing lets are currently desugared
 * into simple lets at parse time.
 *
 * @author Basil Hosmer
 */
public final class IntrinsicLetBinding extends LetBinding
{
    /**
     * arbitrary, fixed-identity dummy RHS for intrinsics
     */
    private static final Term INTRINSIC_VALUE = new TupleTerm(Loc.INTRINSIC);

    //
    // instance
    //

    private Scope scope;
    private Term value;

    /**
     * after type checking, this will be the same as value.getType()
     * when value is non-null.
     */
    private Type type;

    /**
     *
     */
    public IntrinsicLetBinding(final Loc loc, final String name,
        final Type declaredType, final Term value)
    {
        super(loc, name, declaredType);
        this.value = value;

        if (value == INTRINSIC_VALUE)
        {
            assert declaredType != null;
            this.type = declaredType;
        }
        else
        {
            // most of the time rhs type is null at this point,
            // but not always.
            this.type = value.getType();
        }
    }

    /**
     * implicitly typed
     */
    public IntrinsicLetBinding(final Loc loc, final String name, final Term value)
    {
        this(loc, name, null, value);
    }

    /**
     * intrinsics
     */
    public IntrinsicLetBinding(final Loc loc, final String name, final Type declaredType)
    {
        this(loc, name, declaredType, INTRINSIC_VALUE);
    }

    public boolean isIntrinsic()
    {
        return true;
    }
}
