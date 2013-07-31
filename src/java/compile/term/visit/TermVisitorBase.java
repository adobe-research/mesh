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
package compile.term.visit;

import compile.term.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Common base implementation for term visitors.
 * Performs complete traversal but takes no action.
 *
 * @author Basil Hosmer
 */
public class TermVisitorBase<T> implements TermVisitor<T>
{
    /**
     * helper - process individual term.
     * subclasses can override to do pre/post processing
     */
    protected T visitTerm(final Term term)
    {
        return term.accept(this);
    }

    /**
     * helper - visit a list of terms, return a list of results
     */
    protected final List<T> visitEach(final Iterable<Term> terms)
    {
        final List<T> visited = new ArrayList<T>();
        for (final Term term : terms)
            visited.add(visitTerm(term));
        return visited;
    }

    // TermVisitor

    public T visit(final RefTerm ref)
    {
        return null;
    }

    public T visit(final ParamValue paramValue)
    {
        return null;
    }

    public T visit(final BoolLiteral boolLiteral)
    {
        return null;
    }

    public T visit(final IntLiteral intLiteral)
    {
        return null;
    }

    public T visit(final LongLiteral longLiteral)
    {
        return null;
    }

    public T visit(final DoubleLiteral doubleLiteral)
    {
        return null;
    }

    public T visit(final StringLiteral stringLiteral)
    {
        return null;
    }

    public T visit(final SymbolLiteral symbolLiteral)
    {
        return null;
    }

    public T visit(final ListTerm list)
    {
        for (final Term item : list.getItems())
            visitTerm(item);
        return null;
    }

    public T visit(final MapTerm map)
    {
        for (final Map.Entry<Term, Term> entry : map.getItems().entrySet())
        {
            visitTerm(entry.getKey());
            visitTerm(entry.getValue());
        }
        return null;
    }

    public T visit(final TupleTerm tuple)
    {
        for (final Term item : tuple.getItems())
            visitTerm(item);
        return null;
    }

    public T visit(final RecordTerm record)
    {
        for (final Map.Entry<Term, Term> entry : record.getItems().entrySet())
        {
            visitTerm(entry.getKey());
            visitTerm(entry.getValue());
        }
        return null;
    }

    public T visit(final VariantTerm variant)
    {
        visitTerm(variant.getKey());
        visitTerm(variant.getValue());
        return null;
    }

    public T visit(final CondTerm cond)
    {
        visitTerm(cond.getSel());
        visitTerm(cond.getCases());
        return null;
    }

    public T visit(final LambdaTerm lambda)
    {
        return null;
    }

    public T visit(final ApplyTerm apply)
    {
        visitTerm(apply.getBase());
        visitTerm(apply.getArg());
        return null;
    }

    public T visit(final CoerceTerm coerce)
    {
        visitTerm(coerce.getTerm());
        return null;
    }
}
