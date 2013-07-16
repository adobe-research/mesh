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

import compile.StringUtils;
import compile.term.*;
import compile.term.visit.TermDumper;
import compile.term.visit.TermVisitor;
import compile.type.*;
import compile.type.constraint.Constraint;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;

import java.util.*;

/**
 * Dump a string representation of a type term. Provides static (thread local) methods.
 * TODO phase out static methods in favor of DumpUtils
 *
 * @author Basil Hosmer
 */
public final class TypeDumper extends StackedTypeVisitor<String>
{
    private static final ThreadLocal<TypeDumper> LOCAL = new ThreadLocal<TypeDumper>()
    {
        protected TypeDumper initialValue()
        {
            return new TypeDumper();
        }
    };

    /**
     * hack for dumping type fragments, e.g. in lambda sigs,
     * where we never visit the enclosing scope so it can be
     * retained in {@link #visitType(compile.type.Type)}
     */
    public static String dumpWithScope(final Type type, final ScopeType typeScope)
    {
        final TypeDumper typeDumper = LOCAL.get();

        typeDumper.pushTypeScope(typeScope);

        final String result = typeDumper.visitType(type);

        typeDumper.popTypeScope();

        return result;
    }
    
    /**
     *
     */
    public static String dump(final Type type)
    {
        if (type == null)
            return "NULL";

        return LOCAL.get().visitType(type);
    }

    /**
     * dump type expr body, but no param list
     */
    public static String dumpWithoutParams(final Type type)
    {
        if (type == null)
            return "NULL";

        return LOCAL.get().dumpBody(type);
    }

    /**
     * dump a type's param list, enclosed in < > if nonempty
     */
    public static String dumpTypeParams(final Type type)
    {
        if (type == null)
            return "";

        return LOCAL.get().dumpParamList(type);
    }

    /**
     *
     */
    public static List<String> dumpEach(final Iterable<? extends Type> types)
    {
        final List<String> dumps = new ArrayList<String>();

        for (final Type type : types)
            dumps.add(dump(type));

        return dumps;
    }

    /**
     *
     */
    public static String dumpList(final Iterable<? extends Type> types)
    {
        return StringUtils.join(dumpEach(types), ", ");
    }

    /**
     *
     */
    public static String dumpList(final Iterable<? extends Type> types, final String sep)
    {
        return StringUtils.join(dumpEach(types), sep);
    }

    /**
     * Helper - visit a map of values terms to type terms, return an array of
     * "(value): (type)" dump strings.
     */
    private List<String> visitEntrySet(final Set<Map.Entry<Term, Type>> entrySet)
    {
        return visitEntrySet(entrySet, ": ");
    }

    /**
     * Helper - visit a map of values terms to type terms, return an array of
     * "(value): (type)" dump strings.
     */
    private List<String> visitEntrySet(
        final Set<Map.Entry<Term, Type>> entrySet, final String sep)
    {
        final List<String> visitedList = new ArrayList<String>();

        for (final Map.Entry<Term, Type> entry : entrySet)
            visitedList.add(entry.getKey().dump() + sep + visitType(entry.getValue()));

        return visitedList;
    }

    //
    // instance
    //

    // TypeVisitor

    @Override
    protected String visitType(final Type type)
    {
        return dumpParamList(type) + dumpBody(type);
    }


    /**
     * dump type expr body, but no param list
     */
    private String dumpBody(final Type type)
    {
        return super.visitType(type);
    }

    /**
     * dump a type's param list, enclosed in < > if nonempty
     */
    private String dumpParamList(final Type type)
    {
        return (type.hasParams() ?
            "<" + visitList(type.getParams().values()) + "> " : "");
    }

    // like dump, but doesn't reset topLevel
    private String visitList(final Iterable<? extends Type> types)
    {
        final List<String> dumps = new ArrayList<String>();

        for (final Type type : types)
            dumps.add(visitType(type));

        return StringUtils.join(dumps, ", ");
    }

    public TermVisitor<String> getTermVisitor()
    {
        return TermDumper.getThreadLocal();
    }

    @Override
    public String visit(final WildcardType wildcard)
    {
        return "?";
    }
    
    @Override
    public String visit(final EnumType enumType)
    {
        // TODO enum syntax
        // return "Enum([" + TermDumper.dumpList(enumType.getValues()) + "])";
        final Type baseType = enumType.getBaseType();

        return
            baseType.dump() + ":{" + TermDumper.dumpList(enumType.getValues()) + "}";
    }

    @Override
    public String visit(final TypeVar var)
    {
        final Kind kind = var.getKind();
        final Constraint constraint = var.getConstraint();

        if (kind != Kinds.STAR)
        {
            if (constraint != Constraint.ANY)
                return var.getName() + ":" + kind.dump() + "|" + constraint.dump();

            return var.getName() + ":" + kind.dump();
        }

        if (constraint != Constraint.ANY)
            return var.getName() + ":" + constraint.dump();

        return var.getName();
    }

    public String visit(final TypeParam param)
    {
        final Kind kind = param.getKind();
        final Constraint constraint = param.getConstraint();

        if (kind != Kinds.STAR)
        {
            if (constraint != null && constraint != Constraint.ANY)
                return param.getName() + ":" + kind.dump() + "|" + constraint.dump();

            return param.getName() + ":" + kind.dump();
        }

        if (constraint != null && constraint != Constraint.ANY)
            return param.getName() + ":" + constraint.dump();

        return param.getName();
    }

    @Override
    public String visit(final TypeRef ref)
    {
        final Type type = ref.getBinding();

        if (type == null)
        {
            // unresolved
            return "@(\"" + ref.getName() + "\")";
        }
        else
        {
            // TODO sometimes we'll want suffix on params, will get complicated
            return ref.getName();
        }
    }
    
    @Override
    public String visit(final TypeDef def)
    {
        //return "type " + def.getName() + " = " + visitType(def.getValue());
        return def.getName();
    }

    @Override
    public String visit(final TypeCons cons)
    {
        return cons.isAbs() ? visitType(cons.getBody()) : cons.getName();
    }

    /**
     * We trap applications of some built-in constructors
     * and substitute sugared syntax.
     */
    @Override
    public String visit(final TypeApp app)
    {
        final Type base = app.getBase().deref();
        final Type arg = app.getArg();

        if (base == Types.LIST)
        {
            return "[" + visitType(arg) + "]";
        }
        else if (base == Types.BOX)
        {
            return "*" + visitType(arg);
        }
        else if (base == Types.MAP)
        {
            if (arg instanceof TypeTuple)
                return "[" + visitType(Types.mapKey(app)) + " : " +
                    visitType(Types.mapValue(app)) + "]";
        }
        else if (base == Types.FUN)
        {
            if (arg instanceof TypeTuple)
            {
                final Type paramType = Types.funParam(app);
                final String paramDump = Types.isFun(paramType) ?
                    "(" + visitType(paramType) + ")" : visitType(paramType);

                final Type resultType = Types.funResult(app);
                final String resultDump = Types.isFun(resultType) ?
                    "(" + visitType(resultType) + ")" : visitType(resultType);

                return paramDump + " -> " + resultDump;
            }
        }
        else if (base == Types.TUP)
        {
            if (arg instanceof TypeList)
            {
                final List<Type> members = ((TypeList)arg).getItems();

                return "(" +
                    StringUtils.join(visitEach(members), ", ") +
                    (members.size() == 1 ? "," : "") +
                    ")";
            }
        }
        else if (base == Types.REC)
        {
            if (arg instanceof TypeMap)
            {
                final Map<Term, Type> fields = ((TypeMap)arg).getMembers();

                return fields.isEmpty() ?
                    "(:)" :
                    "(" + StringUtils.join(visitEntrySet(fields.entrySet()), ", ") + ")";
            }
        }
        else if (base == Types.SUM)
        {
            if (arg instanceof TypeMap)
            {
                final Map<Term, Type> fields = ((TypeMap)arg).getMembers();

                return "(" +
                        StringUtils.join(visitEntrySet(fields.entrySet(), " ! "), ", ") +
                    ")";
            }
        }
        else if (base == Types.TMAP)
        {
            if (arg instanceof TypeTuple)
            {
                return visitType(Types.appArg(app, 0)) + " | " +
                    visitType(Types.appArg(app, 1));
            }
        }

        // fall-through
        return visitType(app.getBase()) +
            (arg instanceof TypeTuple ? visitType(arg) : ("(" + visitType(arg) + ")"));
    }

    @Override
    public String visit(final TypeTuple tuple)
    {
        final List<Type> members = tuple.getMembers();

        return "(" + StringUtils.join(visitEach(members), ", ") +
            (members.size() == 1 ? "," : "") +
            ")";
    }

    @Override
    public String visit(final TypeList list)
    {
        final List<Type> items = list.getItems();
        return "[" + StringUtils.join(visitEach(items), ", ") + "]";
    }
    
    @Override
    public String visit(final TypeMap map)
    {
        final Map<Term, Type> members = map.getMembers();
        return "[" + StringUtils.join(visitEntrySet(members.entrySet()), ", ") + "]";
    }
}
