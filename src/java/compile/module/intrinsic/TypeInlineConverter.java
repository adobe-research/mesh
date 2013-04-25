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
package compile.module.intrinsic;

import compile.term.TypeDef;
import compile.type.Type;
import compile.type.TypeParam;
import compile.type.TypeRef;
import compile.type.visit.TypeTransformerBase;

/**
 * Wraps inline type params and type defs in type refs.
 * Used as a convenience to process type expressions given by hand in Java.
 * TODO remove when no longer used by {@link compile.module.intrinsic.IntrinsicModule}.
 *
 * @author Basil Hosmer
 */
public final class TypeInlineConverter extends TypeTransformerBase
{
    private static TypeInlineConverter INSTANCE = new TypeInlineConverter();

    public static Type convert(final Type type)
    {
        return INSTANCE.transform(type);
    }

    // TypeTransformerBase

    /**
     * Here we simply transfer all params from original to copy.
     * This means as usual that this makes the original useless,
     * since now original.param.typeScope != original
     */
    @Override
    protected void fixupParams(final Type original, final Type result)
    {
        for (final TypeParam param : original.getParams().values())
            result.addParam(param);
    }


    // TypeVisitor

    /**
     * 
     */
    @Override
    public Type visit(final TypeDef def)
    {
        return new TypeRef(def.getLoc(), def);
    }
    
    /**
     * At this point in the pipeline, inline type params will appear
     * embedded directly into type terms. Here we simply each one in
     * its own type ref; {@link compile.type.visit.TypeBindingCollector} will map these
     * to merged entries in the enclosing type's parameter map.
     */
    @Override
    public Type visit(final TypeParam param)
    {
        return new TypeRef(param.getLoc(), param);
    }
}