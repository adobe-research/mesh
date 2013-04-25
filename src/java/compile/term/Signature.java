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
package compile.term;

import com.google.common.collect.Lists;
import compile.*;
import compile.type.*;

import java.util.*;

/**
 * Contains the Type parts of a lambda (or intrinsic) declaration
 */
public final class Signature implements Located
{
    protected final Loc loc;

    /**
     * calculated type of the term, set after type inference/checking
     */
    private Type type;

    /**
     * type parameter declarations, as parsed
     */
    private final Map<String, TypeParam> typeParamDecls;

    /**
     * (value) parameter declarations, as parsed
     */
    private Map<String, ParamBinding> params;

    /**
     * true once parameter declarations have been committed.
     * Used to enforce exclusivity of declared and inline param syntax.
     */
    private boolean paramsCommitted;

    /**
     * aggregate declared parameter type, as parsed, with
     * {@link WildcardType} used for missing annotations.
     */
    private Type declaredParamType;

    /**
     * declared result type, as parsed, or null.
     */
    private Type declaredResultType;

    /**
     * type as declared, with {@link WildcardType} in positions
     * lacking annotations.
     */
    private Type signatureType;

    /**
     * Note that {@link #typeParamDecls} has explicitly declared type params;
     * inlines are added during resolution.
     */
    public Signature(
        final Loc loc,
        final List<TypeParam> typeParamDecls,
        final List<ParamBinding> params,
        final Type declaredResultType)
    {
        this.loc = loc;

        this.typeParamDecls = new LinkedHashMap<String, TypeParam>();

        this.params = new LinkedHashMap<String, ParamBinding>();

        this.paramsCommitted = false;

        this.declaredParamType = null;

        this.declaredResultType = declaredResultType;

        this.signatureType = null;

        for (final TypeParam typeParam : typeParamDecls)
            addTypeParamDecl(typeParam);

        for (final ParamBinding param : params)
            addParam(param);
    }

    public final Loc getLoc()
    {
        return loc;
    }

    /**
     *
     */
    public void addTypeParamDecl(final TypeParam typeParam)
    {
        final TypeParam exists = typeParamDecls.get(typeParam.getName());

        if (exists != null)
        {
            Session.error(typeParam.getLoc(),
                "type parameter {0} already declared at {1}",
                exists.getName(), exists.getLoc());

            return;
        }

        typeParamDecls.put(typeParam.getName(), typeParam);
    }

    /**
     * add non-inline param binding.
     */
    public void addParam(final ParamBinding param)
    {
        assert !getParamsCommitted() :
            "param bindings already committed";

        assert !hasInlineParams();

        final ParamBinding exists = params.get(param.getName());

        if (exists != null)
        {
            Session.error(param.getLoc(),
                "parameter {0} already declared at {1}",
                exists.getName(), exists.getLoc());
            return;
        }

        param.setIndex(params.size());
        params.put(param.getName(), param);
    }

    /**
     *
     */
    public void addInlineParam(final ParamBinding param)
    {
        assert !getParamsCommitted() :
            "param bindings already committed";

        if (params.isEmpty())
        {
            // replace empty creation-time map with sorted map
            params = new TreeMap<String, ParamBinding>();
        }
        else
        {
            // non-empty, better be inlining already
            assert hasInlineParams();
        }

        final ParamBinding exists = params.get(param.getName());

        if (exists != null)
        {
            Session.error(param.getLoc(),
                "parameter {0} already declared at {1}",
                exists.getName(), exists.getLoc());
            return;
        }

        params.put(param.getName(), param);
    }

    /**
     * A little bit of a hack, but reliable: our params map is
     * sorted (treemap) iff our params came from inline refs.
     */
    private boolean hasInlineParams()
    {
        return params instanceof TreeMap;
    }

    /**
     * Called once all params have been added.
     * 1. Lock param binding map.
     * 2. Fill gaps if inline params have been used.
     * 3. Build our declared type.
     * NOTE: declared type is built regardless of whether any annotations
     * were supplied, so {@link #hasDeclaredType()} is nontrivial.
     */
    public void commitParams(final LambdaTerm scope)
    {
        assert !paramsCommitted : "params already committed";

        if (hasInlineParams())
        {
            assert scope != null : "Intrinsic can't have inline params";
            // fill gaps
            {
                final ArrayList<ParamBinding> adds = Lists.newArrayList();

                int i = 0;
                for (final String name : params.keySet())
                {
                    // DANGER: assumes format "$n" for inline param names
                    final int j = Integer.parseInt(name.substring(1, name.indexOf('_')));
                    for (; i < j; i++)
                    {
                        final ParamBinding b = new ParamBinding(loc, "$" + i, null, true);
                        adds.add(b);
                    }
                    i++;
                }

                for (final ParamBinding add : adds)
                    params.put(add.getName(), add);
            }

            // reindex
            {
                int i = 0;
                for (final ParamBinding param : params.values())
                {
                    param.setIndex(i);
                    i++;
                }
            }
        }

        // derive lambda types

        final List<Type> members = new ArrayList<Type>();

        for (final ParamBinding param : params.values())
        {
            if (scope != null) 
            {
                param.setScope(scope);
            }
            members.add(param.hasDeclaredType() ?
                param.getDeclaredType() :
                new WildcardType(param.getLoc()));
        }

        declaredParamType =
            members.size() == 1 ? members.get(0) : Types.tup(loc, members);

        // declared result type or null
        final Type resultType =
            declaredResultType != null ? declaredResultType : new WildcardType(loc);

        signatureType = Types.fun(loc, declaredParamType, resultType);

        if (!typeParamDecls.isEmpty())
            signatureType.addParams(typeParamDecls.values());

        signatureType.collectInlineParams();

        // record any collected inline params as declared
        final Map<String, TypeParam> declParams = signatureType.getParams();
        for (final String name : declParams.keySet())
            if (!typeParamDecls.containsKey(name))
                typeParamDecls.put(name, declParams.get(name));

        paramsCommitted = true;
    }

    public boolean getParamsCommitted()
    {
        return paramsCommitted;
    }

    public Type getSignatureType()
    {
        return signatureType;
    }

    /**
     * Return the given param type from our declared signature.
     */
    public Type getSignatureParamType(final int index)
    {
        if (params.size() == 1)
            return declaredParamType;

        final List<Type> paramTypes =
            ((TypeList)Types.tupMembers(declaredParamType).deref()).getItems();

        return paramTypes.get(index);
    }

    /**
     * Return the type of param at given position, from our composite type.
     */
    public Type getParamType(final int index)
    {
        assert Types.isFun(type);
        final Type paramTypeTerm = Types.funParam(type);

        if (params.size() == 1)
            return paramTypeTerm;

        final List<Type> paramTypeTerms =
            ((TypeList)Types.tupMembers(paramTypeTerm).deref()).getItems();

        return paramTypeTerms.get(index);

    }

    public Type getDeclaredResultType()
    {
        return declaredResultType;
    }

    public Map<String, TypeParam> getTypeParamDecls()
    {
        return typeParamDecls;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(final Type type)
    {
        if (!Types.isFun(type))
            throw new IllegalArgumentException("LambdaTerm type must be function type");

        this.type = type;
    }

    /**
     * True if any part of our signature carries a declared type.
     */
    public boolean hasDeclaredType()
    {
        if (declaredResultType != null)
            return true;

        for (final ParamBinding param : params.values())
            if (param.hasDeclaredType())
                return true;

        return false;
    }

    public Type getDeclaredType()
    {
        return hasDeclaredType() ? signatureType : null;
    }

    public void setDeclaredType(final Type type)
    {
        assert hasDeclaredType() :
            "Signature.setDeclaredType() w/o declaredType in original";

        signatureType = type;
    }

    public Map<String, ParamBinding> getParams()
    {
        assert params != null : "null params in signature?";
        return params;
    }
}
