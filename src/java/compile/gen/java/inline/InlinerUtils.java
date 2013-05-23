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
package compile.gen.java.inline;

import compile.Loc;
import compile.gen.java.InvokeInfo;
import compile.gen.java.StatementFormatter;
import compile.term.*;
import compile.type.Type;
import compile.type.Types;
import compile.StringUtils;
import runtime.rep.lambda.IntrinsicLambda;

import java.util.ArrayList;

/**
 * Situation-aware formatting utilities used by {@link Inliner}
 * implementations.
 *
 * @author Basil Hosmer
 */
public class InlinerUtils
{
    /**
     * Convenience wrapper for {@link #formatBlockStmts(compile.gen.java.StatementFormatter, compile.term.Term, boolean)},
     * passes true for final param (fallbackToCall).
     */
    static String formatBlockStmts(final StatementFormatter fmt, final Term block)
    {
        return formatBlockStmts(fmt, block, true);
    }

    /**
     * Try to inline the body of a block (a ()->T lambda), for use as a block body
     * in target code. If successful, a string containing the generated code is
     * returned, otherwise null.
     *
     * Inlining succeeds if the term {@link #derefToLambda dereferences to a lambda},
     * or failing that if the fallbackToCall param is true, in which case a single
     * statement containing a call to the lambda is returned.
     *
     * So given a block <pre><code>
     *     f = { a(1); b(2); c(3) }
     * </code></pre>
     * then in the former case the returned string will amount to <pre><code>
     *     "a(1); b(2); c(3)"
     * </code></pre>
     * and in the latter case <pre><code>
     *     "f()"
     * </code></pre>
     *
     * The fallback allows us to inline control flow even when arguments can't be
     * dereferenced to lambda literals, eg <pre><code>
     *     if(c, t, f) => if (c) { t(); } else { f(); }
     * </code></pre>
     * ...which empirically seems worth it.
     *
     * Note that as implied by the "Stmts" in the name, we are implicitly generating
     * code to be used in a non-expression context. This means that we don't need to
     * return the block's result value, only execute the expression that produces it.
     */
    static String formatBlockStmts(final StatementFormatter fmt,
        final Term block, final boolean fallbackToCall)
    {
        final Loc loc = block.getLoc();
        final Type type = block.getType();
        final Type resultType = Types.funResult(type);

        // traverse body term to ensure codegen, and generate a
        // fallback call to block if desired
        final String call;
        {
            if (fallbackToCall)
            {
                final Term body = new ApplyTerm(loc, block, TupleTerm.UNIT);
                body.setType(resultType);
                call = fmt.formatTermAs(body, resultType);
            }
            else
            {
                // note: still visit body to ensure codegen
                fmt.formatTermAs(block, block.getType());
                call = null;
            }
        }

        // try to deref to a lambda literal, or fall back
        final LambdaTerm blockDeref = derefToLambda(block);
        if (blockDeref == null)
            return call;

        // we have our deref, generate body code
        final ArrayList<String> bodyStmts = new ArrayList<String>();

        // non-result statements
        for (final Statement statement : blockDeref.getNonResultStatements())
            bodyStmts.add(fmt.formatInLambdaStatement(statement));

        // result statement. note
        // (a) don't generate a return,
        // (b) omitted if we detect "()" (the unit tuple).
        // TODO if we had purity, we could omit any pure statement in this position
        final UnboundTerm result = blockDeref.getResultStatement();
        if (!result.getValue().equals(TupleTerm.UNIT))
            bodyStmts.add(fmt.formatInLambdaStatement(result));

        return StringUtils.join(bodyStmts, ";\n\t");
    }

    /**
     * Convenience wrapper for {@link #formatBlockStmts(compile.gen.java.StatementFormatter, compile.term.Term, boolean)},
     * passes true for fallbackToCall.
     */
    static String formatBlockExpr(final StatementFormatter fmt, final Term block)
    {
        return formatBlockExpr(fmt, block, true);
    }

    /**
     * Try to inline the body of a block, in an expression context.
     * If successful, a string containing the generated code is
     * returned, otherwise null.
     *
     * See {@link #formatBlockStmts(compile.gen.java.StatementFormatter, compile.term.Term, boolean)}
     * for general details. An important difference here is that the
     * expression (rather than statement) context prevents us from
     * generating code containing multiple statements.
     */
    static String formatBlockExpr(final StatementFormatter fmt,
        final Term block, final boolean fallbackToCall)
    {
        final Loc loc = block.getLoc();
        final Type type = block.getType();
        final Type resultType = Types.funResult(type);

        // create a synthetic call to block
        final Term body = new ApplyTerm(loc, block, TupleTerm.UNIT);
        body.setType(resultType);

        final String call;
        if (fallbackToCall)
        {
            call = fmt.formatTermAs(body, resultType);
        }
        else
        {
            // note: still visit body to ensure codegen
            fmt.formatTermAs(body, resultType);
            call = null;
        }

        // try to deref to a lambda literal, otherwise fall back
        final LambdaTerm blockDeref = derefToLambda(block);
        if (blockDeref == null)
            return call;

        // If dereferenced block consists of a single statement,
        // generate body code, otherwise fall back to call (or null).
        if (blockDeref.getNonResultStatements().isEmpty())
        {
            // generate an expr from a single-statement lambda.
            // formatter might not be in an expression context, so force it
            // TODO add API to do this more cleanly
            final boolean save = fmt.getInExpr();
            fmt.setInExpr(true);

            final Term resultTerm = blockDeref.getResultStatement().getValue();

            final String bodyFmt = fmt.formatTermAs(resultTerm, resultType);

            fmt.setInExpr(save);

            return bodyFmt;
        }

        return call;
    }

    /**
     * find lambda literals at the end of very simple reference chains.
     * note: this duplicates a subset of the logic of
     * {@link compile.gen.java.StatementFormatter#getInvokeInfo}, should factor.
     * And we could certainly do more, e.g. [{...}, {...}] # const
     */
    static LambdaTerm derefToLambda(final Term term)
    {
        if (term instanceof LambdaTerm)
            return (LambdaTerm)term;

        if (term instanceof RefTerm)
        {
            final ValueBinding binding = ((RefTerm)term).getBinding();
            if (binding.isLet() && !((LetBinding) binding).isIntrinsic())
                return derefToLambda(binding.getValue());
        }

        return null;
    }

    /**
     * if the passed term is an application of the given intrinsic, return the
     * application's argument, otherwise null
     */
    static Term derefToIntrinsicApply(final Term term, 
        final IntrinsicLambda intrinsic, final StatementFormatter fmt)
    {
        if (term instanceof ApplyTerm)
        {
            final ApplyTerm apply = (ApplyTerm)term;
            final Term base = apply.getBase();

            return derefToIntrinsic(base, intrinsic, fmt) != null ?
                apply.getArg() : null;
        }

        return null;
    }

    /**
     * final refs to intrinsic lets at the end of simple reference chains
     */
    static LetBinding derefToIntrinsic(final Term term, 
       final IntrinsicLambda intrinsic, final StatementFormatter fmt)
    {
        if (term instanceof RefTerm)
        {
            final Binding binding = ((RefTerm)term).getBinding();
            if (binding.isLet())
            {
                final LetBinding let = (LetBinding)binding;
                if (let.isIntrinsic())
                {
                    final InvokeInfo info = fmt.getInvokeInfo(term);
                    final String intrName = intrinsic.getClass().getName();
                    return info.className.equals(intrName) ? let : null;
                }

                return derefToIntrinsic(let.getValue(), intrinsic, fmt);
            }
        }

        return null;
    }
}
