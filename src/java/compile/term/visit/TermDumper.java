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

import compile.StringUtils;
import compile.term.*;
import compile.type.Type;
import compile.type.Types;
import compile.type.visit.TypeDumper;

import java.util.*;

/**
 * Utility for pretty printing terms and term lists.
 * TODO phase out static methods in favor of DumpUtils
 *
 * @author Basil Hosmer
 */
public final class TermDumper extends TermVisitorBase<String>
{
    public static final ThreadLocal<TermDumper> LOCAL =
        new ThreadLocal<TermDumper>()
        {
            protected TermDumper initialValue()
            {
                return new TermDumper();
            }
        };

    public static TermDumper getThreadLocal()
    {
        return LOCAL.get();
    }

    public static String dump(final Term term)
    {
        return term == null ? "NULL" : term.accept(getThreadLocal());
    }

    public static List<String> dumpEach(final Iterable<? extends Term> terms)
    {
        final List<String> dumps = new ArrayList<String>();
        for (final Term term : terms)
            dumps.add(dump(term));
        return dumps;
    }

    public static String dumpList(final Iterable<? extends Term> terms)
    {
        return StringUtils.join(dumpEach(terms), ", ");
    }

    public static String dumpList(final Iterable<? extends Term> terms, final String sep)
    {
        return StringUtils.join(dumpEach(terms), sep);
    }

    // TermVisitorString

    @Override
    public String visit(final RefTerm ref)
    {
        if (ref.getQualifier() == null) 
            return ref.getName();
        else 
            return ref.getQualifier() + ":" + ref.getName();
    }

    @Override
    public String visit(final ParamValue paramValue)
    {
        return "<param value: " + paramValue.getParam().dump() + ">";
    }

    @Override
    public String visit(final BoolLiteral boolLiteral)
    {
        return "" + boolLiteral.getValue();
    }

    @Override
    public String visit(final IntLiteral intLiteral)
    {
        return "" + intLiteral.getValue();
    }

    @Override
    public String visit(final LongLiteral longLiteral)
    {
        return "" + longLiteral.getValue();
    }

    @Override
    public String visit(final DoubleLiteral doubleLiteral)
    {
        return "" + doubleLiteral.getValue();
    }

    @Override
    public String visit(final StringLiteral stringLiteral)
    {
        return "\"" + StringUtils.escapeJava(stringLiteral.getValue()) + "\"";
    }

    @Override
    public String visit(final SymbolLiteral symbolLiteral)
    {
        return "#" + symbolLiteral.getValue();
    }

    @Override
    public String visit(final ListTerm list)
    {
        return "[" + StringUtils.join(visitEach(list.getItems()), ", ") + "]";
    }

    @Override
    public String visit(final MapTerm map)
    {
        return map.getItems().isEmpty() ?
            "[:]" :
            "[" +
                StringUtils.join(
                    visitEntrySet(map.getItems().entrySet(), ": "), ", ") +
            "]";
    }

    @Override
    public String visit(final TupleTerm tuple)
    {
        final List<Term> members = tuple.getItems();

        return "(" + StringUtils.join(visitEach(members), ", ") +
            (members.size() == 1 ? "," : "") +
            ")";
    }

    @Override
    public String visit(final RecordTerm record)
    {
        return record.getItems().isEmpty() ?
            "(:)" :
            "(" +
                StringUtils.join(
                    visitEntrySet(record.getItems().entrySet(), ": "), ", ") +
                ")";
    }

    @Override
    public String visit(final LambdaTerm lambda)
    {
        final StringBuilder buf = new StringBuilder();

        final String tpsig = TypeDumper.dumpList(lambda.getTypeParamDecls().values());

        if (tpsig.length() > 0)
            buf.append("<").append(tpsig).append("> ");

        buf.append("{ ");

        final String sig = dumpLambdaSig(lambda);

        if (sig.length() > 0)
            buf.append(sig).append(" => ");

        final List<String> dumps = new ArrayList<String>();

        for (final Statement statement : lambda.getBody())
            dumps.add(statement.dump());

        buf.append(StringUtils.join(dumps, "; "));

        buf.append(" }");

        return buf.toString();
    }

    /**
     * dump lambda signature, i.e. the sig in { sig => body }
     */
    private String dumpLambdaSig(final LambdaTerm lambda)
    {
        final StringBuilder sig = new StringBuilder();

        final Map<String, ParamBinding> paramBindings = lambda.getParams();

        final List<String> paramDumps = new ArrayList<String>();

        for (final ParamBinding param : paramBindings.values())
            paramDumps.add(param.dump());

        final Type declaredResultType = lambda.getDeclaredResultType();

        if (declaredResultType != null)
        {
            sig.append("(");
            sig.append(StringUtils.join(paramDumps, ", "));
            sig.append(") -> ").append(declaredResultType.dump());
        }
        else
        {
            sig.append(StringUtils.join(paramDumps, ", "));
        }

        return sig.toString();
    }

    /**
     * helper - visit terms in map entry set, return list of
     * pairwise-concatenated string results
     */
    private List<String> visitEntrySet(final Set<Map.Entry<Term, Term>> entrySet,
        final String sep)
    {
        final List<String> visitedList = new ArrayList<String>();

        for (final Map.Entry<Term, Term> pair : entrySet)
            visitedList.add(visitTerm(pair.getKey()) + sep + visitTerm(pair.getValue()));

        return visitedList;
    }

    @Override
    public String visit(final ApplyTerm apply)
    {
        final Term arg = apply.getArg();

        switch (apply.getFlav())
        {
            case FuncApp:
                return visitTerm(apply.getBase()) +
                    (arg instanceof TupleTerm ? visitTerm(arg) : "(" + visitTerm(arg) + ")");

            case CollIndex:
                return visitTerm(apply.getBase()) +
                    (arg instanceof TupleTerm ? visitTerm(arg) : "[" + visitTerm(arg) + "]");

            case StructAddr:
            {
                final String fmtArg = visitTerm(arg);
                return visitTerm(apply.getBase()) + "." +
                    (arg.getType() == Types.SYMBOL ? fmtArg.substring(1) : fmtArg);
            }

            default:
                assert false;
                return "";
        }
    }

    @Override
    public String visit(final CoerceTerm coerce)
    {
        return "(" + coerce.getTerm().dump() + " as " + coerce.getType().dump() + ")";
    }
}
