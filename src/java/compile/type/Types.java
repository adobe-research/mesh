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
package compile.type;

import compile.Loc;
import compile.gen.java.Constants;
import compile.term.*;
import compile.type.constraint.RecordConstraint;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Predefined types.
 *
 * @author Basil Hosmer
 */
public final class Types
{
    // TODO: consider getting rid of these statics and using the IntrinsicType
    // instances directily.
    //
    // built-in ground types
    //
    public static final TypeCons BOOL = findIntrinsicCons("Bool"); 
    public static final TypeCons INT = findIntrinsicCons("Int");
    public static final TypeCons LONG = findIntrinsicCons("Long");
    public static final TypeCons FLOAT = findIntrinsicCons("Float");
    public static final TypeCons DOUBLE = findIntrinsicCons("Double");
    public static final TypeCons STRING = findIntrinsicCons("String");
    public static final TypeCons SYMBOL = findIntrinsicCons("Symbol");

    /**
     * Opaque is effectively Top. Used only as the structural payload
     * for nominal types produced and consumed by foreign functions.
     */
    public static final TypeCons OPAQUE = findIntrinsicCons("Opaque");

    //
    // built-in type constructors
    //
    public static final TypeCons BOX = findIntrinsicCons("Box");
    public static final TypeCons NEW = findIntrinsicCons("New");
    public static final TypeCons LIST = findIntrinsicCons("List");

    public static final TypeCons MAP = findIntrinsicCons("Map");
    public static final TypeCons FUN = findIntrinsicCons("Fun");

    public static final TypeCons TUP = findIntrinsicCons("Tup");
    public static final TypeCons REC = findIntrinsicCons("Rec");

    public static final TypeCons SUM = findIntrinsicCons("Sum");

    //
    // built-in type functions
    //
    public static final TypeCons TMAP = findIntrinsicCons("TMap");

    public static final TypeCons INDEX = findIntrinsicCons("Index");

    public static final TypeCons ASSOC = findIntrinsicCons("Assoc");

    /**
     * Unit is the 0-tuple, an application of the Tup type constructor
     */
    public static final Type UNIT = findIntrinsic("Unit");

    /**
     * build TypeDef over given TypeCons
     * used to add builtin type constructors to intrinsics module
     */
    public static TypeDef def(final TypeCons cons)
    {
        return new TypeDef(Loc.INTRINSIC, cons.getName(), cons);
    }

    /**
     * build TypeDef with given name, rvalue
     * used to add builtin types to intrinsics module
     */
    public static TypeDef def(final String name, final Type type)
    {
        return new TypeDef(Loc.INTRINSIC, name, type);
    }

    //
    // helpers - convenience wrappers to build and query
    // common type terms
    //

    //
    // type application terms
    //

    public static TypeApp app(final Loc loc, final Type base, final Type arg)
    {
        return new TypeApp(loc, base, arg);
    }

    public static TypeApp app(final Type base, final Type arg)
    {
        return new TypeApp(Loc.INTRINSIC, base, arg);
    }

    public static boolean isApp(final Type type)
    {
        return type.deref() instanceof TypeApp;
    }

    public static boolean isAppOf(final Type type, final TypeCons cons)
    {
        return isApp(type) && appBase(type) == cons;
    }

    public static Type appBase(Type type)
    {
        type = type.deref();

        if (!(type instanceof TypeApp))
            throw new IllegalArgumentException();

        final TypeApp app = (TypeApp)type;

        return app.getBase().deref();
    }

    public static Type appArg(Type type)
    {
        type = type.deref();

        if (!(type instanceof TypeApp))
            throw new IllegalArgumentException();

        final TypeApp app = (TypeApp)type;

        return app.getArg().deref();
    }

    public static Type appArg(Type type, final int i)
    {
        type = type.deref();

        if (!(type instanceof TypeApp))
            throw new IllegalArgumentException();

        final TypeApp app = (TypeApp)type;

        final Type arg = app.getArg().deref();
        assert arg instanceof TypeTuple;

        return ((TypeTuple)arg).getMembers().get(i);
    }

    //
    // box types are app(BOX, <value type>)
    //

    public static TypeApp box(final Loc loc, final Type arg)
    {
        return app(loc, BOX, arg);
    }

    public static TypeApp box(final Type arg)
    {
        return box(Loc.INTRINSIC, arg);
    }

    public static boolean isBox(final Type type)
    {
        return type instanceof TypeApp && ((TypeApp)type).getBase().deref() == BOX;
    }

    public static Type boxValue(Type type)
    {
        type = type.deref();
        if (!Types.isBox(type))
            throw new IllegalArgumentException();

        return ((TypeApp)type).getArg();
    }

    //
    // new (nominal) types are app(NEW, <value type>)
    //

    public static TypeApp newType(final Loc loc, final Type arg)
    {
        return app(loc, NEW, arg);
    }

    public static TypeApp newType(final Type arg)
    {
        return newType(Loc.INTRINSIC, arg);
    }

    public static boolean isNew(final Type type)
    {
        return type instanceof TypeApp && ((TypeApp)type).getBase().deref() == NEW;
    }

    public static Type newRep(Type type)
    {
        type = type.deref();
        if (!Types.isNew(type))
            throw new IllegalArgumentException();

        return ((TypeApp)type).getArg();
    }

    //
    // list types are app(LIST, <elem type>)
    //

    public static TypeApp list(final Loc loc, final Type elem)
    {
        return app(loc, LIST, elem);
    }

    public static TypeApp list(final Type elem)
    {
        return list(Loc.INTRINSIC, elem);
    }

    public static boolean isList(Type type)
    {
        type = type.deref();
        return type instanceof TypeApp && ((TypeApp)type).getBase().deref() == LIST;
    }

    public static Type listItem(Type type)
    {
        type = type.deref();
        if (!Types.isList(type))
            throw new IllegalArgumentException();

        return ((TypeApp)type).getArg();
    }

    //
    // map types are app(MAP, (<key type>, <value type>))
    // builders take scattered arg pair
    //

    public static TypeApp map(final Loc loc, final Type key, final Type val)
    {
        return binapp(loc, MAP, key, val);
    }

    public static TypeApp map(final Type key, final Type val)
    {
        return map(Loc.INTRINSIC, key, val);
    }

    public static boolean isMap(Type type)
    {
        type = type.deref();
        return type instanceof TypeApp && ((TypeApp)type).getBase().deref() == MAP;
    }

    public static Type mapKey(Type type)
    {
        type = type.deref();
        if (!Types.isMap(type))
            throw new IllegalArgumentException();

        return appArg(type, 0);
    }

    public static Type mapValue(Type type)
    {
        type = type.deref();
        if (!Types.isMap(type))
            throw new IllegalArgumentException();

        return appArg(type, 1);
    }

    //
    // function types are app(FUN, (<param type>, <result type))
    // builders take scattered arg pair
    //

    public static TypeApp fun(final Loc loc, final Type param, final Type result)
    {
        return binapp(loc, FUN, param, result);
    }

    public static TypeApp fun(final Type key, final Type val)
    {
        return fun(Loc.INTRINSIC, key, val);
    }

    public static boolean isFun(Type type)
    {
        type = type.deref();
        return type instanceof TypeApp && ((TypeApp)type).getBase().deref() == FUN;
    }

    public static Type funParam(Type type)
    {
        type = type.deref();
        if (!Types.isFun(type))
            throw new IllegalArgumentException();

        return appArg(type, 0);
    }

    public static Type funResult(Type type)
    {
        type = type.deref();
        if (!Types.isFun(type))
            throw new IllegalArgumentException();

        return appArg(type, 1);
    }

    //
    // tuple types are app(TUP, [<type>, ...])
    // some builders take raw/scattered arg list, other takes cooked arg term.
    // arg kind is checked for compatibility later
    //

    public static TypeApp tup(final Loc loc, final List<Type> items)
    {
        return app(loc, TUP, new TypeList(loc, items));
    }

    public static TypeApp tup(final Type... items)
    {
        return tup(Loc.INTRINSIC, Arrays.asList(items));
    }

    public static TypeApp tup(final Loc loc, final Type list)
    {
        return app(loc, TUP, list);
    }

    public static boolean isTup(Type type)
    {
        if (type == null)
            assert false;

        type = type.deref();
        return type instanceof TypeApp && ((TypeApp)type).getBase().deref() == TUP;
    }

    public static Type tupMembers(Type type)
    {
        type = type.deref();
        if (!Types.isTup(type))
            throw new IllegalArgumentException();

        return ((TypeApp)type).getArg();
    }

    //
    // record types are app(REC, [<term> : <type>, ...])
    // one builder takes raw type map, others take cooked arg term.
    // arg kind is checked for compatibility later
    //

    public static TypeApp rec(final Loc loc, final Map<Term, Type> items)
    {
        return rec(loc, new TypeMap(loc, items));
    }

    public static TypeApp rec(final Type fields)
    {
        return rec(Loc.INTRINSIC, fields);
    }

    public static TypeApp rec(final Loc loc, final Type fields)
    {
        return app(loc, REC, fields);
    }

    public static boolean isRec(Type type)
    {
        type = type.deref();
        return type instanceof TypeApp && ((TypeApp)type).getBase().deref() == REC;
    }

    public static boolean isPolyRec(Type type)
    {
        type = type.deref();
        return type instanceof TypeParam &&
            ((TypeParam)type).getConstraint() instanceof RecordConstraint;
    }

    public static Type recFields(Type type)
    {
        type = type.deref();

        if (!Types.isRec(type))
            throw new IllegalArgumentException();

        return ((TypeApp)type).getArg();
    }

    /**
     *
     */
    public static List<SimpleLiteralTerm> recKeyList(final Type recordType)
    {
        final Type fieldTypes = recFields(recordType);

        if (!(fieldTypes instanceof TypeMap))
            return null;

        final Type keyType = ((TypeMap)fieldTypes).getKeyType();

        if (!(keyType instanceof EnumType))
            return null;

        final EnumType keyEnum = (EnumType)keyType;

        final List<SimpleLiteralTerm> list = new ArrayList<SimpleLiteralTerm>();

        for (final Term value : keyEnum.getValues())
            list.add((SimpleLiteralTerm)value);

        return list;
    }

    //
    // sum types are app(SUM, [<term> : <type>, ...])
    // one builder takes raw type map, others take cooked arg term.
    // arg kind is checked for compatibility later
    //

    public static TypeApp sum(final Loc loc, final Map<Term, Type> items)
    {
        return sum(loc, new TypeMap(loc, items));
    }

    public static TypeApp sum(final Loc loc, final Type opts)
    {
        return app(loc, SUM, opts);
    }

    public static TypeApp sum(final Type opts)
    {
        return sum(Loc.INTRINSIC, opts);
    }

    public static boolean isSum(Type type)
    {
        type = type.deref();
        return type instanceof TypeApp && ((TypeApp)type).getBase().deref() == SUM;
    }

    public static Type sumOpts(Type type)
    {
        type = type.deref();
        if (!Types.isSum(type))
            throw new IllegalArgumentException();

        return ((TypeApp)type).getArg();
    }

    //
    // tmap exprs are app(TMAP, (<type list>, <type constructor>))
    // builders take scattered arg pair
    //

    public static TypeApp tmap(final Loc loc, final Type lhs, final Type rhs)
    {
        return binapp(loc, TMAP, lhs, rhs);
    }

    public static TypeApp tmap(final Type lhs, final Type rhs)
    {
        return tmap(Loc.INTRINSIC, lhs, rhs);
    }

    public static boolean isTMap(Type type)
    {
        type = type.deref();
        return type instanceof TypeApp && ((TypeApp)type).getBase().deref() == TMAP;
    }

    public static Type tmapList(Type type)
    {
        type = type.deref();
        if (!Types.isTMap(type))
            throw new IllegalArgumentException();

        return appArg(type, 0);
    }

    public static Type tmapCons(Type type)
    {
        type = type.deref();
        if (!Types.isTMap(type))
            throw new IllegalArgumentException();

        return appArg(type, 1);
    }

    //
    // index exprs are app(INDEX, <type list>)
    //

    public static TypeApp index(final Loc loc, final Type list)
    {
        return app(loc, INDEX, list);
    }

    public static TypeApp index(final Type list)
    {
        return index(Loc.INTRINSIC, list);
    }

    public static boolean isIndex(final Type type)
    {
        return isAppOf(type, INDEX);
    }

    public static Type indexList(final Type type)
    {
        assert Types.isIndex(type);
        return appArg(type);
    }

    //
    // assoc exprs are app(ASSOC, (<key type>, <value types>))
    //

    public static TypeApp assoc(final Loc loc, final Type lhs, final Type rhs)
    {
        return binapp(loc, ASSOC, lhs, rhs);
    }

    public static TypeApp assoc(final Type lhs, final Type rhs)
    {
        return assoc(Loc.INTRINSIC, lhs, rhs);
    }

    public static boolean isAssoc(final Type type)
    {
        return isAppOf(type, ASSOC);
    }

    public static Type assocKey(final Type type)
    {
        assert Types.isAssoc(type);
        return appArg(type, 0);
    }

    public static Type assocVals(final Type type)
    {
        assert Types.isAssoc(type);
        return appArg(type, 1);
    }

    //
    // binary applications are app(<TCON>, (<arg 0>, <arg 1))
    // builders take scattered arg pair
    //

    public static TypeApp binapp(final Loc loc, final TypeCons tcon,
        final Type left, final Type right)
    {
        return app(loc, tcon, new TypeTuple(left.getLoc(), left, right));
    }

    //
    // coercions
    //

    /**
     *
     */
    public static Type applyArgCoercions(final TypeCons cons, final Type arg)
    {
        if (cons == SUM)
            return applySumArgCoercions(arg);

        return arg;
    }

    /**
     *
     */
    private static Type applySumArgCoercions(final Type arg)
    {
        if (isRec(arg))
        {
            return recFields(arg);
        }
        else if (isTup(arg))
        {
            final Type members = tupMembers(arg);
            return assoc(index(members), members);
        }
        else if (arg instanceof TypeList)
        {
            return assoc(index(arg), arg);
        }

        return arg;
    }


    public static Type findIntrinsic(final String name)
    {
        // TODO: use a configurable search path instead
        final String[] intrinsicSearchPath = new String[] { 
            "compile.type.intrinsic", 
            "runtime.intrinsic.demo"
        };

        for (final String packageName : intrinsicSearchPath) 
        {
            final String clsName = packageName + "." + name;

            try
            {
                final Class<?> cls = Class.forName(clsName);
                final Field instanceField = cls.getField(Constants.INSTANCE);
                final Type type = (Type)instanceField.get(null);

                assert type != null : "Type canot be null";
                return type;
            }
            catch (ClassNotFoundException e)
            {
                /* try the next package */
            }
            catch (NoSuchFieldException e)
            {
                /* try the next package */
            }
            catch (IllegalAccessException e)
            {
                /* try the next package */
            }
        }
        return null;
    }

    public static TypeCons findIntrinsicCons(final String name)
    {
        final Type type = findIntrinsic(name);
        if (type != null && type instanceof TypeCons)
            return (TypeCons)type;
        else
            return null;
    }
}
