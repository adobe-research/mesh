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
import compile.Pair;
import compile.Session;
import compile.term.Term;
import compile.type.visit.SubstMap;

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

    @Override
    public Pair<? extends EnumType, SubstMap> merge(final EnumType otherEnum)
    {
        if (!(otherEnum instanceof ChoiceType))
            return null;

        final ChoiceType otherChoice = (ChoiceType)otherEnum;

        final Set<Term> mergedValues = new LinkedHashSet<Term>(values);
        mergedValues.addAll(otherChoice.values);

        final ChoiceType mergedChoice = new ChoiceType(loc, baseType, mergedValues);

        return Pair.create(mergedChoice, SubstMap.EMPTY);
    }

    @Override
    public SubstMap subsume(final Loc loc, final Type type, final TypeEnv env)
    {
        if (!(type instanceof ChoiceType))
            return null;

        final ChoiceType otherChoice = (ChoiceType)type;

        return otherChoice.getBaseType().equals(baseType) &&
            values.containsAll(otherChoice.values) ?
            SubstMap.EMPTY : null;
    }
}
