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
package compile.gen.java;

import compile.Loc;
import compile.Session;
import compile.term.*;
import compile.type.Type;
import compile.type.Types;
import runtime.rep.Lambda;
import compile.NameUtils;
import compile.StringUtils;

import java.util.*;

import static compile.parse.ApplyFlavor.StructAddr;

/**
 * Lambda ClassDef implementation generator {@link #generate}.
 *
 * @author Basil Hosmer
 */
public final class LambdaClassGenerator extends ClassGenerator
{
    /**
     * Given a LambdaTerm, creates a ClassDef which implements the lambda.
     */
    public static ClassDef generate(final LambdaTerm lambdaTerm,
        final String qualifiedClassName,
        final StatementFormatter formatter)
    {
        final ClassDef classDef = new ClassDef(qualifiedClassName);

        // use type formatter to get name of most specific interface we should declare
        classDef.addInterfaceName(formatter.formatType(lambdaTerm.getType()));

        if (lambdaTerm.hasCapturedLambdaBindings())
        {
            // add fields for captured environment
            for (final ValueBinding valueBinding : lambdaTerm.getCapturedLambdaBindings()
                .values())
                classDef.addFieldDef(
                    new FieldDef(formatFieldDecl(valueBinding, false, true, formatter),
                        ""));

            // add non-default constructor that takes captured environment values
            addConstructorDef(lambdaTerm, classDef, formatter);
        }
        else
        {
            // if no capture, add INSTANCE singleton
            classDef.addStaticFieldDef(buildInstanceField(classDef.getName()));
        }

        // add apply method
        classDef.addMethodDef(buildApplyMethodDef(lambdaTerm, formatter));

        // add invoke method
        classDef.addMethodDef(buildInvokeMethodDef(lambdaTerm, formatter));

        // add toString() method
        classDef.addMethodDef(buildToStringMethodDef(lambdaTerm));

        return classDef;
    }

    /**
     * Add constructor to classdef, if needed.
     */
    private static void addConstructorDef(final LambdaTerm lambdaTerm,
        final ClassDef classDef,
        final StatementFormatter formatter)
    {
        final Map<String, ValueBinding> capturedBindings =
            lambdaTerm.getCapturedLambdaBindings();

        if (!capturedBindings.isEmpty())
        {
            final List<String> bindingDecls =
                new ArrayList<String>(capturedBindings.size());

            for (final ValueBinding capturedBinding : capturedBindings.values())
                bindingDecls.add(
                    formatter.formatTypeName(capturedBinding) + " " +
                        StatementFormatter.formatName(capturedBinding.getName()));

            // Note unqualified name
            final String sig = "public " + NameUtils.unqualify(classDef.getName()) +
                "(" + StringUtils.join(bindingDecls, ", ") + ")";

            final ConstructorDef ctorDef = new ConstructorDef(sig);

            for (final String bindingName : capturedBindings.keySet())
                ctorDef.addStatement(new JavaStatement(
                    "this." + StatementFormatter.formatName(bindingName) +
                        " = " + StatementFormatter.formatName(bindingName)));

            classDef.setConstructorDef(ctorDef);
        }
    }

    /**
     * field def for static INSTANCE field holding class instance.
     * Used for lambdas with no environment capture.
     */
    private static FieldDef buildInstanceField(final String className)
    {
        final String decl = "public static " + className + " " + Constants.INSTANCE;
        final String init = "new " + className + "()";
        return new FieldDef(decl, init);
    }

    /**
     * Generate {@link Lambda#apply} implementation.
     * <p/>
     * Nontrivial work here is scattering parts of a composite argument to locals
     */
    private static MethodDef buildApplyMethodDef(final LambdaTerm lambdaTerm,
        final StatementFormatter formatter)
    {
        final Type lambdaType = lambdaTerm.getType();
        final Collection<ParamBinding> params = lambdaTerm.getParams().values();

        // set up method signature, param name.
        // single params are straightforward, multiples need a synthetic Java param
        final String paramName = params.size() == 1 ?
            params.iterator().next().getName() :
            ensureUniqueParamName(lambdaTerm, "arg");

        final String sig =
            "public final " + Constants.OBJECT + " " + Constants.APPLY + "(" +
                Constants.OBJECT + " " + StatementFormatter.formatName(paramName) +
                ")";


        // create method def
        final MethodDef methodDef = new MethodDef(sig);

        // build args to invoke()
        final ArrayList<String> invokeArgs = new ArrayList<String>(params.size());

        if (params.size() == 1)
        {
            final ParamBinding param = params.iterator().next();
/*
            invokeArgs.add(
                formatter.addDowncast(param.getType(),
                    StatementFormatter.formatName(param.getName())));
*/
            final String expr = formatter.fixup(
                param.getLoc(),
                formatter.formatNameRef(param),
                Object.class,
                param.getType());

            invokeArgs.add(expr);
        }
        else
        {
            // create tuple-typed local for param, then
            // build (downcast) member accesses for scatter terms
            if (params.size() > 1)
            {
                final Loc paramLoc = params.iterator().next().getLoc();
                
                final Type paramType = Types.funParam(lambdaType);

                final ParamBinding singleParam =
                    new ParamBinding(paramLoc, paramName, paramType)
                    {
                        @Override
                        public Type getType()
                        {
                            return paramType;
                        }

                        @Override
                        public Type getDeclaredType()
                        {
                            return paramType;
                        }
                    };

                final RefTerm paramRef = makeRefTerm(singleParam);

                final LetBinding argsVar =
                    new LetBinding(paramLoc, "args", paramType, paramRef);

                // e.g. "final Tuple _args = (Tuple)obj;"
                final String argsInit =
                    formatter.formatVarDeclLHS(argsVar) + " = " +
                        formatter.fixup(
                            paramLoc,
                            formatter.formatNameRef(singleParam),
                            Object.class,
                            paramType);

                methodDef.addStatement(new JavaStatement(argsVar, argsInit));

                // build the list of invoke params
                final RefTerm refTerm = makeRefTerm(argsVar);

                int i = 0;
                for (final ParamBinding param : params)
                {
                    final Loc loc = param.getLoc();
                    final Type type = param.getType();

                    final ApplyTerm accessTerm =
                        new ApplyTerm(loc, refTerm, new IntLiteral(loc, i), StructAddr);

                    accessTerm.setType(type);

                    invokeArgs.add(formatter.formatTermAs(accessTerm, type));

                    i++;
                }
            }
        }

        // body is just a delegation to invoke()
        final Type resultType = Types.funResult(lambdaType);

        final String ret = "return " + formatter.fixup(
            lambdaTerm.getLoc(),
            Constants.INVOKE + "(" + StringUtils.join(invokeArgs, ", ") + ")",
            resultType,
            Object.class);

        methodDef.addStatement(new JavaStatement(ret));

        return methodDef;
    }

    /**
     *
     */
    private static RefTerm makeRefTerm(final ValueBinding binding)
    {
        final RefTerm refTerm =
            new RefTerm(null, StatementFormatter.formatName(binding.getName()));

        refTerm.setBinding(binding);

        return refTerm;
    }

    /**
     * Generate function-specific invoke implementation.
     * This is called by {@link Lambda#apply} and
     * directly in generated code where possible.
     */
    private static MethodDef buildInvokeMethodDef(final LambdaTerm lambdaTerm,
        final StatementFormatter formatter)
    {
        final Type lambdaType = lambdaTerm.getType();
        final Collection<ParamBinding> params = lambdaTerm.getParams().values();

        // Note: invoke() is part of some Lambda subinterface sigs (e.g. Lambda2)
        // so cannot be static, even if function has no captured environment.
        final StringBuilder sig = new StringBuilder().
            append("public ").
            append(lambdaTerm.hasCapturedLambdaBindings() ? "final" : "static").
            append(" ").
            append(formatter.formatType(Types.funResult(lambdaType))).
            append(" ").append(Constants.INVOKE).
            append("(");

        final ArrayList<String> paramDecls = new ArrayList<String>(params.size());
        for (final ParamBinding param : params)
            paramDecls.add(formatter.formatInLambdaStatement(param));

        sig.append(StringUtils.join(paramDecls, ", ")).append(")");

        final MethodDef methodDef = new MethodDef(sig.toString());
        generateBodyStatements(lambdaTerm, formatter, methodDef);

        return methodDef;
    }

    /**
     * generate body statements
     */
    private static void generateBodyStatements(final LambdaTerm lambdaTerm,
        final StatementFormatter formatter,
        final MethodDef methodDef)
    {
        final Type resultType = Types.funResult(lambdaTerm.getType());

        // if lambda returns unit, add a final unit-returning statement.
        // this allows CG inliner to generate imperative form of original
        // final statement
        if (resultType.equals(Types.unit()))
        {
            final UnboundTerm result = lambdaTerm.getResultStatement();

            if (!result.getValue().equals(TupleTerm.UNIT))
            {
                if (Session.isDebug())
                    Session.debug(result.getLoc(), "adding dummy unit after {0}",
                        result.dump());

                lambdaTerm.getBody().add(new UnboundTerm(TupleTerm.UNIT));
            }
        }

        for (final Statement statement : lambdaTerm.getNonResultStatements())
            methodDef.addStatement(new JavaStatement(statement,
                formatter.formatInLambdaStatement(statement)));

        final UnboundTerm resultStatement = lambdaTerm.getResultStatement();
        final Term resultTerm = resultStatement.getValue();

        methodDef.addStatement(
            new JavaStatement(resultStatement,
                "return " + formatter.formatTermAs(resultTerm, resultType)));
    }

    /**
     * Generate a synthetic param name that stays out of the way of actual param names
     */
    private static String ensureUniqueParamName(final LambdaTerm lambdaTerm,
        String paramName)
    {
        final Set<String> paramNames = lambdaTerm.getParams().keySet();

        while (paramNames.contains(paramName))
        {
            paramName = "_" + paramName;
        }

        return paramName;
    }

    /**
     * Lambda.toString() dumps source representation
     */
    private static MethodDef buildToStringMethodDef(final LambdaTerm lambdaTerm)
    {
        final MethodDef methodDef =
            new MethodDef("public " + String.class.getName() + " toString()");

        methodDef.addStatement(
            new JavaStatement(
                "return \"" + StringUtils.escapeJava(lambdaTerm.dump()) + "\""));

        return methodDef;
    }
}
