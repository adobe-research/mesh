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

import compile.Session;
import compile.analyze.KindChecker;
import compile.analyze.TypeChecker;
import compile.term.Term;
import compile.term.visit.TermVisitor;
import compile.type.EnumType;
import compile.type.Type;
import compile.type.TypeRef;
import compile.type.WildcardType;
import compile.type.kind.Kinds;

/**
 * Prepare declared type exprs for type checking.
 * Note that due to shared subnodes, types may come through here more than once.
 * So for safety, all operations here need to be idempotent.
 * TODO move this into TC proper
 *
 * @author Basil Hosmer
 */
public final class TypeExprPreprocessor extends TypeVisitorBase<Object>
{
    private final TypeChecker typeChecker;

    /**
     * We take the type checker to which we pass value terms attached to types.
     * Note argument to super: we update the passed type terms, rather than producing
     * transformed copies.
     *
     * @param typeChecker
     */
    public TypeExprPreprocessor(final TypeChecker typeChecker)
    {
        this.typeChecker = typeChecker;
    }

    public void preprocess(final Type type)
    {
        visitType(type);
        KindChecker.check(type);
    }

    @Override
    public TermVisitor<?> getTermVisitor()
    {
        return typeChecker;
    }

    /**
     * Calculate common base type for enum values, if one exists.
     */
    @Override
    public Object visit(final EnumType enumType)
    {
        final Type origBaseType = enumType.getBaseType();

        final Type baseType = !(origBaseType instanceof WildcardType) ?
            origBaseType.instance(typeChecker, true) :
            typeChecker.freshVar(enumType.getLoc(), Kinds.STAR);

        for (final Term value : enumType.getValues())
        {
            final Type valueType = typeChecker.visitTermInType(value);

            if (!typeChecker.unify(value.getLoc(), baseType, valueType))
                Session.error(value.getLoc(),
                    "value type {0} is incompatible with established enum base type {1}",
                    typeChecker.errorFormat(valueType).dump(),
                    typeChecker.errorFormat(baseType).dump());
        }

        if (origBaseType != baseType)
            enumType.setBaseType(baseType);

        return null;
    }

    /**
     * don't traverse referenced typed
     */
    @Override
    public Object visit(final TypeRef ref)
    {
        return null;
    }
}
