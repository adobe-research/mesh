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
package compile.type.visit;

import com.google.common.collect.Sets;
import compile.type.Type;
import compile.type.TypeParam;
import compile.type.TypeRef;

import java.util.Set;

/**
 * Collects all params referred to from a type term (local and
 * from enclosing type terms).
 *
 * @author Basil Hosmer
 */
public class TypeParamCollector extends TypeVisitorBase<Object>
{
    private static final ThreadLocal<TypeParamCollector> LOCAL =
        new ThreadLocal<TypeParamCollector>()
        {
            protected TypeParamCollector initialValue()
            {
                return new TypeParamCollector();
            }
        };

    /**
     * Collect type params mentioned in given type
     */
    public static Set<TypeParam> collect(final Type type)
    {
        return LOCAL.get().process(type);
    }

    //
    // instance
    //

    /**
     * set of collected params.
     * Note: must be an identity set.
     */
    private Set<TypeParam> params;

    private Set<TypeParam> process(final Type type)
    {
        params = Sets.newIdentityHashSet();
        visitType(type);
        return params;
    }

    /**
     *
     */
    @Override
    public Object visit(final TypeRef ref)
    {
        if (ref.isParamRef())
            params.add((TypeParam)ref.getBinding());

        return null;
    }
}