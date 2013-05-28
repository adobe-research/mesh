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

import compile.*;
import compile.term.LetBinding;
import compile.type.*;
import runtime.rep.lambda.IntrinsicLambda;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Find intrinsics in the classpath and verify that thier runtime signatures match
 * the source-declared signatures.
 *
 * @author Keith McGuigan
 */
public final class JavaIntrinsicsResolver extends IntrinsicsResolver
{

    public static IntrinsicsResolver.Factory factory =
        new IntrinsicsResolver.Factory()
        {
            public IntrinsicsResolver create()
            {
                return new JavaIntrinsicsResolver();
            }
        };

    /**
     * Memoized list of bindings which have been verified
     */
    private final Map<LetBinding,IntrinsicLambda> verifiedIntrinsics;
    private String lastErrorMessage;

    // TODO: use module name as key into package configuration mapping
    private static final String[] intrinsicPackages = new String[] {
        "runtime.intrinsic",
        "runtime.intrinsic.tran",
        "runtime.intrinsic.demo",
        "runtime.intrinsic.demo.processing",
        "runtime.intrinsic.demo.socket",
        "runtime.intrinsic.log",
        "runtime.intrinsic.test"
    };

    public JavaIntrinsicsResolver()
    {
        this.verifiedIntrinsics = new HashMap<LetBinding,IntrinsicLambda>();
        this.lastErrorMessage = null;
    }


    private IntrinsicLambda resolved(
            final LetBinding let, final IntrinsicLambda lambda)
    {
        lastErrorMessage = null;
        verifiedIntrinsics.put(let, lambda);
        return lambda;
    }

    private IntrinsicLambda cached(final IntrinsicLambda lambda)
    {
        lastErrorMessage = null;
        return lambda;
    }

    private IntrinsicLambda error(final String msg)
    {
        lastErrorMessage = msg;
        return null;
    }

    public String getErrorMessage()
    {
        return lastErrorMessage;
    }

    public IntrinsicLambda resolve(final LetBinding let)
    {
        final IntrinsicLambda lambda = verifiedIntrinsics.get(let);
        if (lambda != null)
            return cached(lambda);

        final String name = let.getName();

        for (final String pkg : intrinsicPackages)
        {
            try
            {
                final String clsName = pkg + "._" + name;
                final Class<?> intrClass = Class.forName(clsName);
                if (intrClass != null)
                {
                    final Field field = intrClass.getField(Constants.INSTANCE);
                    final Object intrObj = field.get(null);
                    if (intrObj != null && intrObj instanceof IntrinsicLambda)
                    {
                        final IntrinsicLambda instance = (IntrinsicLambda)intrObj;
                        final String msg = verifyIntrinsicType(let, instance);
                        if (msg == null)
                            return resolved(let, instance);
                        else
                            return error(msg);
                    }
                }
            }
            catch (ClassNotFoundException ignored) {}
            catch (NoSuchFieldException ignored) {}
            catch (IllegalAccessException ignored) {}
        }

        return error("Cannot find implementation for intrinsic " +
            let.getName() + " with type " + let.getType().dump());
    }

    private String verifyIntrinsicType(final LetBinding let, final IntrinsicLambda lambda)
    {
        if (Session.isDebug())
            Session.debug("Verifying proper form of intrinsic: ''{0}''", let.getName());

        final List<Class<?> > parameters = getParameterTypes(let.getType());
        final Class<?> returnType = getReturnType(let.getType());

        if (parameters == null || returnType == null)
        {
            return "Invalid intrinsic implementation " + lambda.getClass();
        }

        final Class<?>[] paramspec = parameters.toArray(new Class<?>[parameters.size()]);
        try
        {
            final Method method = lambda.getClass().getMethod(Constants.INVOKE, paramspec);
            if (method.getReturnType() == returnType)
                return null; // success
        }
        catch (NoSuchMethodException ignored) {}

        return "intrinsic implementation " +
            lambda.getClass() + " is incompatible with prototype " +
            let.getType().dump();
    }

    /**
     * Returns the function type as a (Return,Parameters) pair.  If the input type is not
     * a function type (or is not in the form we expect for functions), then this will
     * return null.
     */
    private Pair<Type,Type> getFunctionTypes(final Type function)
    {
        if (Types.isAppOf(function, Types.FUN))
        {
            final Type arg = Types.appArg(function);
            if (arg instanceof TypeTuple)
            {
                final List<Type> tuple = ((TypeTuple)arg).getMembers();
                if (tuple.size() == 2)
                    return new Pair<Type,Type>(tuple.get(0), tuple.get(1));
            }
        }
        return null;
    }

    /**
     * Grab the return type out of function type, and convert to Java type
     */
    private Class<?> getReturnType(final Type function)
    {
        final Pair<Type,Type> pair = getFunctionTypes(function);
        if (pair != null)
            return (new TypeMapper()).map(pair.right);

        return null;
    }

    /**
     * Checks parameter type to see if it is a tuple (multiple parameters)
     */
    private static TypeList asTypeList(final Type type)
    {
        if (Types.isTup(type))
        {
            final Type members = Types.tupMembers(type);
            if (members instanceof TypeList)
                return (TypeList)members;
        }

        return null;
    }

    /**
     * Grab the parameters type out of function type, and convert to
     * list of Java types.
     */
    private List<Class<?> > getParameterTypes(final Type function)
    {
        final TypeMapper typeMapper = new TypeMapper();
        final Pair<Type,Type> pair = getFunctionTypes(function);
        if (pair != null)
        {
            final List<Class<?> > params = new ArrayList<Class<?> >();
            final TypeList list = asTypeList(pair.left);

            if (list != null)
            {
                for (final Type item : list.getItems())
                    params.add(typeMapper.map(item));
            }
            else
            {
                // singleton argument
                params.add(typeMapper.map(pair.left));
            }
            return params;
        }

        return null;
    }
}
