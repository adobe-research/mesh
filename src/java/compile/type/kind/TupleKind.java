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
package compile.type.kind;

import compile.DumpUtils;
import compile.Loc;

import java.util.Arrays;
import java.util.List;

/**
 * The kind of type tuples (not tuple types).
 *
 * @author Basil Hosmer
 */
public final class TupleKind extends Kind
{
    final List<Kind> members;

    public TupleKind(final Loc loc, final List<Kind> members)
    {
        super(loc);
        this.members = members;
    }

    public TupleKind(final List<Kind> members)
    {
        this(Loc.INTRINSIC, members);
    }

    public TupleKind(final Kind... members)
    {
        this(Arrays.asList(members));
    }

    public List<Kind> getMembers()
    {
        return members;
    }

    // Dumpable

    public String dump()
    {
        return "(" + DumpUtils.dumpList(members) +
            (members.size() == 1 ? "," : "") +
            ")";
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TupleKind tupleKind = (TupleKind)o;

        if (!members.equals(tupleKind.members)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return members.hashCode();
    }
}
