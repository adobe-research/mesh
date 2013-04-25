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

import compile.term.TypeDef;
import compile.type.*;

/**
 * Interface for type term visitors. Implementations split two groups: pure
 * visitors, which are subclasses of {@link TypeVisitorBase}, and transformers,
 * which either update or produce transformed copies of type terms. These are
 * subclasses of {@link TypeTransformerBase}.
 * <p/>
 * @param <T> type of value returned by visitation
 *
 * @author Basil Hosmer
 */
public interface TypeVisitor<T>
{
    T visit(WildcardType wildcard);

    T visit(EnumType enumType);

    T visit(TypeVar var);

    T visit(TypeRef ref);

    T visit(TypeDef def);
    
    T visit(TypeParam param);

    T visit(TypeCons cons);
    
    T visit(TypeApp app);
    
    T visit(TypeTuple tuple);
    
    T visit(TypeList list);
    
    T visit(TypeMap map);
}
