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

import compile.Session;
import compile.term.TypeDef;
import compile.type.*;
import compile.type.kind.Kinds;
import compile.type.visit.TypeVisitorBase;
import runtime.rep.*;
import runtime.rep.Lambda;
import runtime.rep.list.ListValue;
import runtime.rep.map.MapValue;
import runtime.rep.Record;
import runtime.tran.Box;

/**
 * Map type expression to Java representation class
 *
 * @author Basil Hosmer
 */
public final class TypeMapper extends TypeVisitorBase<Class<?>>
{
    private TypeMapper()
    {
    }

    /**
     *
     */
    public static Class<?> map(final Type type)
    {
        final TypeMapper visitor = new TypeMapper();
        return visitor.visitType(type);
    }

    /**
     *
     */
    @Override
    public Class<?> visit(final WildcardType wildcard)
    {
        Session.error(wildcard.getLoc(), "internal error: undigested wildcard type");
        return Object.class;
    }

    /**
     * Use enum's base type.
     */
    @Override
    public Class<?> visit(final EnumType enumType)
    {
        return visitType(enumType.getBaseType());
    }

    /**
     *
     */
    @Override
    public Class<?> visit(final TypeVar var)
    {
        Session.error(var.getLoc(),
            "internal error: undigested type variable in CG: {0}",
            var.dump());

        return Object.class;
    }

    /**
     * Note.
     */
    @Override
    public Class<?> visit(final TypeParam param)
    {
        return Object.class;
    }

    /**
     *
     */
    @Override
    public Class<?> visit(final TypeRef ref)
    {
        if (ref.isResolved())
        {
            return visitType(ref.getBinding());
        }
        else
        {
            Session.error(ref.getLoc(),
                "internal error: unresolved type name \"{0}\"",
                ref.getName());

            return Object.class;
        }
    }

    /**
     *
     */
    @Override
    public Class<?> visit(final TypeDef def)
    {
        return visitType(def.getValue());
    }

    /**
     *
     */
    @Override
    public Class<?> visit(final TypeCons cons)
    {
        if (cons.getKind() != Kinds.STAR)
        {
            Session.error(cons.getLoc(),
                "internal error: undigested higher-order type constant in CG: {0}",
                cons.dump());

            return Object.class;
        }

        final Class<?> c =
            (cons == Types.BOOL) ? boolean.class :
            (cons == Types.INT) ? int.class :
            (cons == Types.LONG) ? long.class :
            (cons == Types.FLOAT) ? float.class :
            (cons == Types.DOUBLE) ? double.class :
            (cons == Types.STRING) ? String.class :
            (cons == Types.SYMBOL) ? Symbol.class :
            (cons == Types.OPAQUE) ? Object.class :
            null;

        assert c != null;

        return c;
    }

    /**
     *
     */
    @Override
    public Class<?> visit(final TypeApp app)
    {
        // TODO shouldn't this be an assert at the end of TC?
        if (app.hasVars())
            Session.error(app.getLoc(),
                "internal error: undigested type variables in CG: {0}", app.dump());

        // newtypes fall awway in CG, we just use the underlying representation
        if (Types.isNew(app))
            return visitType(Types.newRep(app));

        // simple mapping from type constructors to
        final Class<?> c =
            Types.isBox(app) ? Box.class :
            Types.isList(app) ? ListValue.class :
            Types.isMap(app) ? MapValue.class :
            Types.isFun(app) ? Lambda.class :
            Types.isTup(app) ? Tuple.class :
            Types.isRec(app) ? Record.class :
            Types.isVar(app) ? Variant.class :
            null;

        if (c != null)
            return c;

        // TODO systematize the lifetimes of type transforms:
        // What this code tests is the presence of unevaluated type
        // transforms at CG time. There's nothing fundamentally wrong
        // with finding them here--mostly we don't want to spend a lot
        // of time reevaluating them in ad-hoc ways.
        /*
        final Type base = app.getBase().deref();
        if (base instanceof TypeCons && ((TypeCons)base).getBody() != null)
            Session.error(app.getLoc(),
                "internal error: undigested type function in CG: {0}",
                app.dump());
                */

        return Object.class;
    }

    /**
     *
     */
    @Override
    public Class<?> visit(final TypeTuple tuple)
    {
        Session.error(tuple.getLoc(), "internal error: undigested type tuple {0}",
            tuple.dump());

        return Object.class;
    }

    /**
     *
     */
    @Override
    public Class<?> visit(final TypeList list)
    {
        Session.error(list.getLoc(), "internal error: undigested type list {0}",
            list.dump());

        return Object.class;
    }

    /**
     *
     */
    @Override
    public Class<?> visit(final TypeMap map)
    {
        Session.error(map.getLoc(), "internal error: undigested type map {0}",
            map.dump());

        return Object.class;
    }

}
