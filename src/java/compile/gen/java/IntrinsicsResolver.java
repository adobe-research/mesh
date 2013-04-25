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

import runtime.rep.lambda.IntrinsicLambda;
import compile.Pair;
import compile.Session;
import compile.term.LetBinding;
import compile.type.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Find intrinsics in the classpath and verify that thier runtime signatures match 
 * the source-declared signatures.
 *
 * @author Keith McGuigan
 */
public final class IntrinsicsResolver
{
    /**
     * our type formatter
     */
    private final TypeMapper typeMapper;

    /**
     * Memoized list of bindings which have been verified
     */
    private static final Map<LetBinding,IntrinsicLambda> verifiedIntrinsics = 
        new HashMap<LetBinding,IntrinsicLambda>();
    
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

    public IntrinsicsResolver()
    {
        this.typeMapper = new TypeMapper();
    }


    public IntrinsicLambda resolve(final LetBinding let) 
    {
        IntrinsicLambda resolved = verifiedIntrinsics.get(let);

        if (resolved != null)
            return resolved;

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
                        final IntrinsicLambda lambda = (IntrinsicLambda)intrObj;
                        if (verifyIntrinsicType(let, lambda)) 
                        {
                            verifiedIntrinsics.put(let, lambda);
                            return lambda;
                        }
                    }
                }
            }
            catch (ClassNotFoundException cnfe) {}
            catch (NoSuchFieldException nsfe) {}
            catch (IllegalAccessException iae) {}
        }
        return null;
    }

    /**
     * Here we access special knowledge about how to go from an intrinsic
     * let binding in the current scope, to a compatible value in the
     * underlying Java environment
     */
    public String formatAsRHS(final LetBinding let)
    {
        final IntrinsicLambda intr = resolve(let);
        if (intr != null)
            return intr.getClass().getName() + "." + Constants.INSTANCE;
        else
        {
            Session.error("Cannot find implementation for intrinsic ''{0}'' with type ''{1}''", 
                    let.getName(), let.getType().dump());
            return null;
        }
    }

    private boolean verifyIntrinsicType(final LetBinding let, final IntrinsicLambda lambda)
    {
        if (Session.isDebug()) 
            Session.debug("Verifying proper form of intrinsic: ''{0}''", let.getName());

        final List<Class<?> > parameters = getParameterTypes(let.getType());
        final Class<?> returnType = getReturnType(let.getType());

        if (parameters == null || returnType == null)
        {
            Session.error("Invalid intrinsic implementation ''{0}''", lambda.getClass());
            return false;
        }

        final Class<?>[] paramspec = parameters.toArray(new Class<?>[0]);
        try 
        {
            final Method method = lambda.getClass().getMethod(Constants.INVOKE, paramspec);
            if (method.getReturnType() == returnType) 
                return true;
        }
        catch (NoSuchMethodException nsme) {}

        Session.error("intrinsic implementation ''{0}'' is incompatible with prototype ''{1}''", 
            lambda.getClass(), let.getType().dump());
        return false;
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
            return typeMapper.map(pair.right);

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
