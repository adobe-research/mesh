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
import compile.term.Term;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An enum made from values explicitly chosen from a base type.
 *
 * @author Basil Hosmer
 */
public final class ChoiceType extends EnumType
{
    private final LinkedHashSet<Term> values;

    public ChoiceType(final Loc loc, final Type baseType, final Set<Term> values)
    {
        super(loc, baseType);

        this.values = (values instanceof LinkedHashSet) ?
            (LinkedHashSet<Term>)values :
            new LinkedHashSet<Term>(values);
    }

    /**
     * singleton choice set
     */
    public ChoiceType(final Loc loc, final Type baseType, final Term value)
    {
        this(loc, baseType, Collections.singleton(value));
    }

    /**
     * escape hatch--used by visitors who clone us. TODO should tighten up?
     */
    public LinkedHashSet<Term> getValueSet()
    {
        return values;
    }

    // EnumType

    @Override
    public boolean isExplicit()
    {
        return true;
    }

    @Override
    public int getSize()
    {
        return values.size();
    }

    @Override
    public LinkedHashSet<Term> getValues()
    {
        return values;
    }
}
