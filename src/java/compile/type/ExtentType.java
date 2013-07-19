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
import compile.term.IntLiteral;
import compile.term.Term;
import compile.type.visit.SubstMap;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An enumeration of ints from 0 to the given extent - 1.
 *
 * @author Basil Hosmer
 */
public final class ExtentType extends EnumType
{
    private final int extent;

    public ExtentType(final Loc loc, final Type baseType, final int extent)
    {
        super(loc, baseType);
        this.extent = extent;
    }

    @Override
    public final boolean isExplicit()
    {
        return false;
    }

    @Override
    public final int getSize()
    {
        return extent;
    }

    @Override
    public final Iterable<Term> getValues()
    {
        return new Iterable<Term>()
        {
            public Iterator<Term> iterator()
            {
                return new Iterator<Term>()
                {
                    int i = 0;

                    public boolean hasNext()
                    {
                        return i < extent;
                    }

                    public Term next()
                    {
                        if (!hasNext())
                            throw new NoSuchElementException();

                        return new IntLiteral(loc, i++);
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    public Pair<? extends EnumType, SubstMap> merge(final EnumType otherEnum)
    {
        if (!(otherEnum instanceof ExtentType))
            return null;

        final ExtentType otherExtent = (ExtentType)otherEnum;

        final ExtentType mergedExtent =
            new ExtentType(loc, baseType, Math.max(extent, otherExtent.extent));

        return Pair.create(mergedExtent, SubstMap.EMPTY);
    }

    @Override
    public SubstMap subsume(final Loc loc, final Type type, final TypeEnv env)
    {
        if (!(type instanceof ExtentType))
            return null;

        final ExtentType otherExtent = (ExtentType)type;

        return otherExtent.getBaseType().equals(baseType) &&
            extent >= otherExtent.extent ?
            SubstMap.EMPTY : null;
    }

}
