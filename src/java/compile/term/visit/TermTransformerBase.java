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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Traverses a term and returns a new one if any part
 * of the traversal produces a new subterm. Unchanged
 * subterms are shared between original and new terms.
 * Base implementation only implements traversal, with
 * identity transformations at the leaves.
 *
 * Note: lambda bodies are not traversed, since they
 * contain a list of {@link Statement}s rather than
 * {@link Term}s. Clients of this class or its subs
 * are probably {@link compile.analyze.ModuleVisitor}s.
 *
 * @author Basil Hosmer
 */
public abstract class TermTransformerBase extends TermVisitorBase<Term>
{
    @Override
    public Term visit(final RefTerm ref)
    {
        return ref;
    }

    @Override
    public Term visit(final ParamValue paramValue)
    {
        return paramValue;
    }

    @Override
    public Term visit(final BoolLiteral boolLiteral)
    {
        return boolLiteral;
    }

    @Override
    public Term visit(final IntLiteral intLiteral)
    {
        return intLiteral;
    }

    @Override
    public Term visit(final LongLiteral longLiteral)
    {
        return longLiteral;
    }

    @Override
    public Term visit(final DoubleLiteral doubleLiteral)
    {
        return doubleLiteral;
    }

    @Override
    public Term visit(final StringLiteral stringLiteral)
    {
        return stringLiteral;
    }

    @Override
    public Term visit(final SymbolLiteral symbolLiteral)
    {
        return symbolLiteral;
    }

    @Override
    public Term visit(final ListTerm list)
    {
        final List<Term> items = list.getItems();
        final ArrayList<Term> newItems = transformTermList(items);

        if (newItems == null)
            return list;

        final ListTerm newList = new ListTerm(list.getLoc(), newItems);
        newList.setType(list.getType());

        return newList;
    }

    /**
     * return new term list if any terms were transformed, otherwise null.
     */
    private ArrayList<Term> transformTermList(final List<Term> items)
    {
        ArrayList<Term> newItems = null;

        final int size = items.size();
        for (int i = 0; i < size; i++)
        {
            final Term item = items.get(i);
            final Term newItem = visitTerm(item);

            if (item != newItem)
            {
                if (newItems == null)
                    newItems = new ArrayList<Term>(items);

                newItems.set(i, newItem);
            }
        }

        return newItems;
    }

    @Override
    public Term visit(final MapTerm map)
    {
        final LinkedHashMap<Term, Term> items = map.getItems();
        final LinkedHashMap<Term, Term> newItems = transformTermMap(items);

        if (newItems == null)
            return map;

        final MapTerm newMap = new MapTerm(map.getLoc(), newItems);
        newMap.setType(map.getType());

        return newMap;
    }

    /**
     * return new term map if any terms were transformed, otherwise null.
     * Note that we must preserve original entry order.
     */
    private LinkedHashMap<Term, Term> transformTermMap(final LinkedHashMap<Term, Term> items)
    {
        LinkedHashMap<Term, Term> newItems = null;

        for (final Map.Entry<Term, Term> entry : items.entrySet())
        {
            final Term key = entry.getKey();
            final Term newKey = visitTerm(key);

            final Term value = entry.getValue();
            final Term newValue = visitTerm(value);

            if (newKey != key || newValue != value)
            {
                if (newItems == null)
                {
                    newItems = new LinkedHashMap<Term, Term>();

                    for (final Map.Entry<Term, Term> oldEntry : items.entrySet())
                    {
                        if (oldEntry == entry)
                            break;

                        newItems.put(oldEntry.getKey(), oldEntry.getValue());
                    }
                }

                newItems.put(newKey, newValue);
            }
            else if (newItems != null)
            {
                newItems.put(key, value);
            }
        }

        return newItems;
    }

    @Override
    public Term visit(final TupleTerm tuple)
    {
        final List<Term> items = tuple.getItems();
        final ArrayList<Term> newItems = transformTermList(items);

        if (newItems == null)
            return tuple;

        final TupleTerm newTuple = new TupleTerm(tuple.getLoc(), newItems);
        newTuple.setType(tuple.getType());

        return newTuple;
    }

    @Override
    public Term visit(final RecordTerm record)
    {
        final LinkedHashMap<Term, Term> items = record.getItems();
        final LinkedHashMap<Term, Term> newItems = transformTermMap(items);

        if (newItems == null)
            return record;

        final RecordTerm newRecord = new RecordTerm(record.getLoc(), newItems);
        newRecord.setType(record.getType());

        return newRecord;
    }

    @Override
    public Term visit(final VariantTerm variant)
    {
        final Term key = variant.getKey();
        final Term newKey = visitTerm(key);

        final Term value = variant.getValue();
        final Term newValue = visitTerm(value);

        if (newKey == key && newValue == value)
            return variant;

        final VariantTerm newVariant =
            new VariantTerm(variant.getLoc(), newKey, newValue);

        newVariant.setType(variant.getType());

        return newVariant;
    }

    @Override
    public Term visit(final CondTerm cond)
    {
        final Term sel = cond.getSel();
        final Term newSel = visitTerm(sel);

        final Term cases = cond.getCases();
        final Term newCases = visitTerm(cases);

        if (newSel == sel && newCases == cases)
            return cond;

        final CondTerm newCond =
            new CondTerm(cond.getLoc(), newSel, newCases);

        newCond.setType(cond.getType());

        return newCond;
    }

    @Override
    public Term visit(final LambdaTerm lambda)
    {
        return lambda;
    }

    @Override
    public Term visit(final ApplyTerm apply)
    {
        final Term base = apply.getBase();
        final Term newBase = visitTerm(base);

        final Term arg = apply.getArg();
        final Term newArg = visitTerm(arg);

        if (base == newBase && arg == newArg)
            return apply;

        final ApplyTerm newApply =
            new ApplyTerm(apply.getLoc(), newBase, newArg, apply.getFlav());

        newApply.setType(apply.getType());

        return newApply;
    }

    @Override
    public Term visit(final CoerceTerm coerce)
    {
        final Term term = coerce.getTerm();
        final Term newTerm = visitTerm(term);

        if (newTerm == term)
            return coerce;

        return new CoerceTerm(coerce.getLoc(), newTerm, coerce.getType());
    }
}
