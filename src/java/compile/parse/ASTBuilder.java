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
package compile.parse;

import compile.Loc;
import compile.Session;
import compile.StringUtils;
import compile.term.*;
import compile.type.*;
import compile.type.constraint.Constraint;
import compile.type.kind.ArrowKind;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.kind.TupleKind;
import compile.Pair;

import java.util.*;

import static compile.parse.ApplyFlavor.CollIndex;
import static compile.parse.ApplyFlavor.StructAddr;

/**
 * static methods for building Mesh AST nodes
 *
 * @author Basil Hosmer
 */
public class ASTBuilder
{
    /**
     * typedef statement
     */
    public static List<Statement> typeDefStmt(final Loc loc,
        final String name, final Type type)
    {
        if (type.hasParams())
        {
            Session.error(type.getLoc(),
                "nested polymorphic types not allowed in type defs: {0}",
                type.dump());

            return Collections.emptyList();
        }
        else
        {
            final TypeDef typeDef = new TypeDef(loc, name, type);

            return Collections.<Statement>singletonList(typeDef);
        }
    }

    /**
     * import statement.
     * syms == null indicates open import.
     * namespace == null indicates local (unqualified) namespace,
     * namespace == "" indicates module name as namespace.
     */
    public static List<Statement> importStmt(
        final Loc loc, final List<String> syms,
        final String module, final String namespace)
    {
        final ImportStatement stmt =
            syms == null ?
                (namespace == null ?
                    ImportStatement.openUnqualified(loc, module) :
                ImportStatement.openQualified(loc, module,
                    namespace.isEmpty() ? module : namespace)) :
            // syms != null
                (namespace == null ?
                    ImportStatement.enumUnqualified(loc, syms, module) :
                ImportStatement.enumQualified(loc, syms, module,
                    namespace.isEmpty() ? module : namespace));

        return Collections.<Statement>singletonList(stmt);
    }

    /**
     * export statement
     * syms == ["*"] indicates open export.
     */
    public static List<Statement> exportStmt(final Loc loc,
                                             final List<String> syms)
    {
        final ExportStatement stmt =
            syms.size() == 1 && syms.get(0).equals("*") ?
                ExportStatement.open(loc) :
                ExportStatement.enumerated(loc, syms);

        return Collections.<Statement>singletonList(stmt);
    }

    /**
     * type abstraction (parameterized type def) statement
     */
    public static List<Statement> typeAbsStmt(final Loc loc, final String name,
        final List<TypeParam> params, final Type body)
    {
        if (params.isEmpty())
        {
            return typeDefStmt(loc, name, body);
        }

        // type def can be declared with param list on LHS, or as part of RHS expr
        // ...but not both
        if (body.hasParams())
        {
            Session.error(body.getLoc(),
                "nested polymorphic types not allowed in type defs: {0}",
                body.dump());

            return Collections.emptyList();
        }

        // add params
        body.addParams(params);

        // adjust body kind
        final List<Kind> paramKinds = new ArrayList<Kind>();
        for (final TypeParam param : params)
            paramKinds.add(param.getKind());

        final Kind paramKind = paramKinds.size() == 1 ?
            paramKinds.get(0) : new TupleKind(loc, paramKinds);

        final ArrowKind absKind = new ArrowKind(loc, paramKind, Kinds.STAR);

        final TypeCons rhs = new TypeCons(loc, name, absKind, body);

        final TypeDef def = new TypeDef(loc, name, rhs);

        return Collections.<Statement>singletonList(def);
    }

    public static FunctionDeclaration funcDecl(
        final Loc loc,
        final String name,
        final List<TypeParam> typeParams,
        final List<ParamBinding> params,
        final Type returnType)
    {
        return new FunctionDeclaration(loc, name, typeParams, params, returnType);
    }

    /**
     * function declaration statement
     */
    public static List<Statement> funDecStmt(final Loc loc,
        final String name,
        final List<TypeParam> typeParams,
        final List<ParamBinding> params,
        final Type returnType,
        final List<Statement> body)
    {
        final LambdaTerm lambdaTerm = lambda(loc, typeParams, params, returnType, body);

        final LetBinding let = new LetBinding(loc, name, lambdaTerm);

        return Collections.<Statement>singletonList(let);
    }

    public static List<Statement> intrinsicTypeDef(final Loc loc, final String name)
    {
        final TypeDef typeDef = new TypeDef(loc, name);
        return Collections.<Statement>singletonList(typeDef);
    }

    public static List<Statement> intrinsicFuncStmt(final Loc loc,
         final String name,
         final List<TypeParam> typeParams,
         final List<ParamBinding> params,
         final Type returnType)
    {
        final Signature sig = new Signature(loc,
            typeParams != null ? typeParams : Collections.<TypeParam>emptyList(), 
            params != null ? params : Collections.<ParamBinding>emptyList(), 
            returnType);
        sig.commitParams(null);

        final LetBinding let = new LetBinding(loc, name, sig.getSignatureType());
        return Collections.<Statement>singletonList(let);
    }

    /**
     * let statement
     * decomposing lets desugar into multiple statements
     */
    public static List<Statement> letStmt(final Loc loc,
        final Term lval, final Type ltype, final Term rval)
    {
        if (lval instanceof RefTerm)
        {
            final LetBinding let =
                new LetBinding(loc, ((RefTerm)lval).getName(), ltype, rval);

            return Collections.<Statement>singletonList(let);
        }
        else if (lval instanceof ListTerm)
        {
            if (ltype != null)
                Session.error(loc, "typed decomposing assignments not yet supported");

            return listDecompAssigns(lval, rval);
        }
        else if (lval instanceof MapTerm)
        {
            if (ltype != null)
                Session.error(loc, "typed decomposing assignments not yet supported");

            return mapDecompAssigns(lval, rval);
        }
        else if (lval instanceof TupleTerm)
        {
            if (ltype != null)
                Session.error(loc, "typed decomposing assignments not yet supported");

            return tupleDecompAssigns(lval, rval);
        }
        else if (lval instanceof RecordTerm)
        {
            if (ltype != null)
                Session.error(loc, "typed decomposing assignments not yet supported");

            return recordDecompAssigns(lval, rval);
        }
        else
        {
            Session.error(loc, "illegal receiver ''{0}'' in assignment statement",
                lval.dump());

            return Collections.emptyList();
        }
    }

    /**
     * convenience wrapper takes location from lval
     */
    private static List<Statement> letStmt(
        final Term lval, final Type ltype, final Term rval)
    {
        return letStmt(lval.getLoc(), lval, ltype, rval);
    }

    /**
     * [x, y,...] = rhs, where x, y, ... can be nested structures
     */
    private static List<Statement> listDecompAssigns(final Term lhs, final Term rhs)
    {
        final Loc loc = lhs.getLoc();
        final List<Statement> results = new ArrayList<Statement>();

        if (rhs instanceof ListTerm)
        {
            final ListTerm llist = (ListTerm)lhs;
            final ListTerm rlist = (ListTerm)rhs;

            final List<Term> litems = llist.getItems();
            final List<Term> ritems = rlist.getItems();

            if (litems.size() != ritems.size())
                Session.error(loc, "arity mismatch in decomposing assignment");

            for (int i = 0; i < litems.size(); i++)
            {
                results.addAll(letStmt(litems.get(i), null, ritems.get(i)));
            }
        }
        else
        {
            final RefTerm rhsRef = ensureRef(rhs, loc, results);

            int i = 0;
            for (final Term litem : ((ListTerm)lhs).getItems())
            {
                final IntLiteral posArg = new IntLiteral(loc, i++);
                final Term selTerm = new ApplyTerm(loc, rhsRef, posArg, CollIndex);

                results.addAll(letStmt(litem, null, selTerm));
            }
        }

        return results;
    }

    /**
     * Ensure a reference to a given expr. If expr
     * is not already a reference, create a new temp
     * variable, add binding from temp to original expr
     * to results list, and return reference to it.
     */
    private static RefTerm ensureRef(final Term expr,
        final Loc loc, final List<Statement> results)
    {
        if (expr instanceof RefTerm)
            return (RefTerm)expr;

        // temp = expr
        final String tempName = "_" + loc.getLine() + "_" + loc.getColumn();
        results.add(new LetBinding(loc, tempName, null, expr));

        return new RefTerm(loc, tempName);
    }

    /**
     * [k1: x, k2: y,...] = rhs, where x, y, ... can be nested structures
     */
    private static List<Statement> mapDecompAssigns(final Term lhs, final Term rhs)
    {
        final Loc loc = lhs.getLoc();
        final List<Statement> results = new ArrayList<Statement>();

        if (rhs instanceof MapTerm)
        {
            final MapTerm lmap = (MapTerm)lhs;
            final MapTerm rmap = (MapTerm)rhs;

            final Map<Term, Term> litems = lmap.getItems();
            final Map<Term, Term> ritems = rmap.getItems();

            if (!litems.keySet().equals(ritems.keySet()))
                Session.error(loc, "keyset mismatch in decomposing assignment");

            for (final Term key : litems.keySet())
            {
                results.addAll(letStmt(litems.get(key), null, ritems.get(key)));
            }
        }
        else
        {
            final RefTerm rhsRef = ensureRef(rhs, loc, results);

            for (final Map.Entry<Term, Term> lhsEntry :
                    ((MapTerm)lhs).getItems().entrySet())
            {
                final Term lhsKey = lhsEntry.getKey();
                final Term lhsValue = lhsEntry.getValue();
                final Term selTerm = new ApplyTerm(loc, rhsRef, lhsKey, CollIndex);

                results.addAll(letStmt(lhsValue, null, selTerm));
            }
        }

        return results;
    }

    /**
     * (x,y,...) = rhs, where x, y, ... can be nested structures
     */
    private static List<Statement> tupleDecompAssigns(final Term lhs, final Term rhs)
    {
        final Loc loc = lhs.getLoc();
        final List<Statement> results = new ArrayList<Statement>();

        if (rhs instanceof TupleTerm)
        {
            final TupleTerm ltup = (TupleTerm)lhs;
            final TupleTerm rtup = (TupleTerm)rhs;

            final List<Term> litems = ltup.getItems();
            final List<Term> ritems = rtup.getItems();

            if (litems.size() != ritems.size())
                Session.error(loc, "arity mismatch in decomposing assignment");

            for (int i = 0; i < litems.size(); i++)
            {
                results.addAll(letStmt(litems.get(i), null, ritems.get(i)));
            }
        }
        else
        {
            final RefTerm rhsRef = ensureRef(rhs, loc, results);

            int i = 0;
            for (final Term litem : ((TupleTerm)lhs).getItems())
            {
                final IntLiteral posArg = new IntLiteral(loc, i++);
                final Term selTerm = new ApplyTerm(loc, rhsRef, posArg, StructAddr);

                results.addAll(letStmt(litem, null, selTerm));
            }
        }

        return results;
    }

    /**
     * (k1: x, k2: y,...) = rhs, where x, y, ... can be nested structures
     */
    private static List<Statement> recordDecompAssigns(final Term lhs, final Term rhs)
    {
        final Loc loc = lhs.getLoc();
        final List<Statement> results = new ArrayList<Statement>();

        if (rhs instanceof RecordTerm)
        {
            final RecordTerm lrec = (RecordTerm)lhs;
            final RecordTerm rrec = (RecordTerm)rhs;

            final Map<Term, Term> litems = lrec.getItems();
            final Map<Term, Term> ritems = rrec.getItems();

            if (!litems.keySet().equals(ritems.keySet()))
                Session.error(loc, "keyset mismatch in decomposing assignment");

            for (final Term key : litems.keySet())
            {
                results.addAll(letStmt(litems.get(key), null, ritems.get(key)));
            }
        }
        else
        {
            final RefTerm rhsRef = ensureRef(rhs, loc, results);

            for (final Map.Entry<Term, Term> lhsEntry :
                ((RecordTerm)lhs).getItems().entrySet())
            {
                final Term lhsKey = lhsEntry.getKey();
                final Term lhsValue = lhsEntry.getValue();

                final Term selTerm = new ApplyTerm(loc, rhsRef, lhsKey, StructAddr);

                results.addAll(letStmt(lhsValue, null, selTerm));
            }
        }

        return results;
    }

    /**
     * unbound expression statement
     */
    public static List<Statement> unboundExprStmt(final Term expr)
    {
        final UnboundTerm term = new UnboundTerm(expr);

        return Collections.<Statement>singletonList(term);
    }

    /**
     * declared parameter. type may be null
     */
    public static ParamBinding param(final Loc loc, final String name, final Type type)
    {
        return new ParamBinding(loc, name, type);
    }

    /**
     * build binary expr term from (a, [(op1, b), (op2, c), ...])
     * based on precedence/associativity info from {@link Ops#BINOP_INFO}.
     * ops may be strings containing infix operator symbols, or full terms.
     */
    public static Term binaryExpr(final Term head, final List<Pair<Object, Term>> tail)
    {
        return TermBinExprBuilder.build(head, tail);
    }

    /**
     * constructs a Verb structure, which wraps an infix operator (string or Term),
     * plus left and right dimensionality ranks
     */
    public static Object verb(final int lefts, final Object op, final int rights)
    {
        return lefts + rights == 0 ? op : new Verb(lefts, op, rights);
    }

    /**
     * unary expression
     */
    public static Term unaryExpr(final Loc loc, final String op, final Term term)
    {
        assert Ops.UNOP_INFO.containsKey(op);

        if (op.equals(Ops.MINUS_SYM))
        {
            // deliver negative numerics as constants
            if (term instanceof IntLiteral)
                return new IntLiteral(loc, -((IntLiteral)term).getValue());
            else if (term instanceof DoubleLiteral)
                return new DoubleLiteral(loc, -((DoubleLiteral)term).getValue());
        }

        final String name = Ops.UNOP_INFO.get(op);

        if (name == null)
            Session.error(loc, "unary op ''{0}'' not yet supported", op);

        return new ApplyTerm(loc, new RefTerm(loc, name), term);
    }

    /**
     * build chain of application terms from term list
     */
    public static Term applyChain(final Term base,
        final List<Pair<Term, ApplyFlavor>> chain)
    {
        final Loc loc = base.getLoc();

        Term result = base;

        for (final Pair<Term, ApplyFlavor> arg : chain)
        {
            result = new ApplyTerm(loc, result, arg.left, arg.right);
        }

        return result;
    }

    /**
     * id reference
     */
    public static RefTerm idRef(final Loc loc, final String id)
    {
        return new RefTerm(loc, id);
    }

    /**
     * inline parameter reference
     */
    public static RefTerm inlineParam(final Loc loc,
        final String prefix, final String strpos)
    {
        final String name = prefix + strpos;

        final RefTerm paramRefTerm = new RefTerm(loc, name);

        paramRefTerm.setBinding(new ParamBinding(loc, name, null, true));

        return paramRefTerm;
    }

    /**
     * enum literal
     */
    public static VariantTerm enumLit(final Term lit)
    {
        return new VariantTerm(lit.getLoc(), lit, TupleTerm.UNIT);
    }

    /**
     * boolean literal
     */
    public static BoolLiteral boolLiteral(final Loc loc, final String text)
    {
        return new BoolLiteral(loc, Boolean.parseBoolean(text));
    }

    /**
     * natural (nonnegative base 10 whole number) literal
     */
    public static SimpleLiteralTerm natLiteral(final Loc loc, final String text)
    {
        return numLiteral(loc, text, 10);
    }

    /**
     * hex number literal
     */
    public static SimpleLiteralTerm hexLiteral(final Loc loc, final String text)
    {
        return numLiteral(loc, text, 0x10);
    }

    /**
     * helper - parse numeric literal with a given radix.
     * Trailing 'L' as forces long; also tries long on int parse error,
     * so literals too big for int will produce long without trailing 'L'.
     */
    private static SimpleLiteralTerm numLiteral(final Loc loc, String text, final int radix)
    {
        final boolean explicitLong = text.endsWith("L") || text.endsWith("l");

        if (explicitLong)
        {
            text = text.substring(0, text.length() - 1);
        }

        if (!explicitLong)
        {
            try
            {
                return new IntLiteral(loc, Integer.parseInt(text, radix));
            }
            catch (NumberFormatException e)
            {
                // NOTE: fall through to long parse
            }
        }

        // try parsing as long
        try
        {
            return new LongLiteral(loc, Long.parseLong(text, radix));
        }
        catch (NumberFormatException e)
        {
            Session.error(loc,
                "error parsing {0} decimal literal \"{1}\"",
                explicitLong ? "long" : "", text);

            return explicitLong ?
                new LongLiteral(loc, 0) :
                new IntLiteral(loc, 0);
        }
    }

    /**
     * floating point literal
     * TODO trailing 'f' means float, 'd' forces double
     */
    public static DoubleLiteral floatLiteral(final Loc loc, final String text)
    {
        return new DoubleLiteral(loc, Double.parseDouble(text));
    }

    /**
     * string literal
     */
    public static StringLiteral strLiteral(final Loc loc, final String text)
    {
        return new StringLiteral(loc,
            StringUtils.unescapeJava(text.substring(1, text.length() - 1)));
    }

    /**
     * symbol literal
     */
    public static SymbolLiteral symLiteral(final Loc loc, final String text)
    {
        return new SymbolLiteral(loc, text);
    }

    /**
     * list literal
     */
    public static ListTerm listLiteral(final Loc loc, final List<Term> items)
    {
        return new ListTerm(loc,
            items != null ? items : Collections.<Term>emptyList());
    }

    /**
     * map literal
     */
    public static MapTerm mapLiteral(final Loc loc, final List<Pair<Term, Term>> pairs)
    {
        return new MapTerm(loc, mapFromPairs(pairs != null ? pairs :
            Collections.<Pair<Term, Term>>emptyList()));
    }

    /**
     * tuple literal
     */
    public static TupleTerm tupLiteral(final Loc loc, final Term head, final List<Term> tail)
    {
        return new TupleTerm(loc,
            head != null ? cons(head, tail) : Collections.<Term>emptyList());
    }

    /**
     * record literal
     */
    public static RecordTerm recLiteral(final Loc loc, final List<Pair<Term, Term>> pairs)
    {
        return new RecordTerm(loc, mapFromPairs(pairs != null ? pairs :
            Collections.<Pair<Term, Term>>emptyList()));
    }

    /**
     * lambda
     */
    public static LambdaTerm lambda(final Loc loc, final List<TypeParam> typeParams,
        final List<ParamBinding> params, final Type returnType,
        final List<Statement> body)
    {
        return new LambdaTerm(loc,
            typeParams != null ? typeParams : Collections.<TypeParam>emptyList(),
            params != null ? params : Collections.<ParamBinding>emptyList(),
            returnType,
            body);
    }

    /**
     * operator reference, e.g. (+)
     */
    public static Term opRef(final Loc loc, final String opsym)
    {
        final BinopInfo binopInfo = Ops.BINOP_INFO.get(opsym);

        final String name = binopInfo != null ?
            binopInfo.func : Ops.UNOP_INFO.get(opsym);

        if (name == null)
            Session.error(loc, "operator {0} not yet supported", opsym);

        return new RefTerm(loc, name);
    }

    //
    // types
    //

    /**
     *
     */
    public static Type quantifiedType(final Loc loc,
        final List<TypeParam> params, final Type type)
    {
        type.setLoc(loc);
        type.addParams(params);
        return type;
    }

    /**
     * binary type expression
     */
    public static Type binaryTypeExpr(final Type left, final List<Pair<Object, Type>> rights)
    {
        return TypeBinExprBuilder.build(left, rights);
    }

    /**
     * desugar unary type op to application of type constructor or mapping
     */
    public static Type unaryTypeExpr(final Loc loc, final String op, final Type arg)
    {
        assert Ops.TYPE_UNOP_INFO.containsKey(op);

        final TypeRef base = new TypeRef(loc, Ops.TYPE_UNOP_INFO.get(op));

        return Types.app(loc, base, arg);
    }

    /**
     * build chain of application terms from type list
     */
    public static Type typeApplyChain(final Loc loc,
        final Type base, final List<Type> chain)
    {
        Type result = base;

        for (final Type arg : chain)
        {
            //final Type arg = args.size() == 1 ? args.get(0) : new TypeTuple(loc, args);

            result = new TypeApp(loc, result, arg);
        }

        return result;
    }

    /**
     * build finished arg expr out of arglist
     */
    public static Type typeArgExpr(final Loc loc, final List<Type> args)
    {
        return args.size() == 1 ? args.get(0) : new TypeTuple(loc, args);
    }

    /**
     *
     */
    public static Type typeIdRef(final Loc loc, final String name)
    {
        return new TypeRef(loc, name);
    }

    /**
     *
     */
    public static Type wildcardType(final Loc loc)
    {
        return new WildcardType(loc);
    }

    /**
     *
     */
    public static Type listType(final Loc loc, final Type itemType)
    {
        return Types.list(loc, itemType);
    }

    /**
     *
     */
    public static Type mapType(final Loc loc, final Type keyType, final Type valueType)
    {
        return Types.map(loc, keyType, valueType);
    }

    /**
     *
     */
    public static Type tupType(final Loc loc, final Type head, final List<Type> tail)
    {
        return Types.tup(loc, head != null ?
            cons(head, tail) : Collections.<Type>emptyList());
    }

    /**
     *
     */
    public static Type recType(final Loc loc, final List<Pair<Term, Type>> pairs)
    {
        return Types.rec(loc, pairs != null ?
            mapFromPairs(pairs) : Collections.<Term, Type>emptyMap());
    }

    /**
     *
     */
    public static Type enumType(final Loc loc, final List<Term> items)
    {
        final LinkedHashSet<Term> values = new LinkedHashSet<Term>(items);
        return new EnumType(loc, new WildcardType(loc), values);
    }

    /**
     *
     */
    public static TypeParam typeParam(final Loc loc, final String name, final Kind kind)
    {
        return new TypeParam(loc, name,
            kind == null ? Kinds.STAR : kind, Constraint.ANY);
    }

    //
    // kinds
    //

    /**
     *
     */
    public static Kind arrowKind(final Loc loc, final Kind head, final List<Kind> tail)
    {
        return tail.isEmpty() ? head :
            new ArrowKind(loc, head,
                arrowKind(loc, tail.get(0), tail.subList(1, tail.size())));
    }

    /**
     *
     */
    public static Kind tupleKind(final Loc loc, final Kind head, final List<Kind> tail)
    {
        return tail.isEmpty() ? head :
            new TupleKind(loc, cons(head, tail));
    }

    //
    // action helpers
    //

    /**
     * Process a flattened list of (Term, ?) pairs.
     * We check keys for duplication, but since we allow
     * non-constant terms this is not exhaustive.
     * Note: record literals *do* require constant keys,
     * but since a PEG parser may get here speculatively,
     * we must defer some semantic checks until later.
     */
    private static <T> LinkedHashMap<Term, T> mapFromPairs(final List<Pair<Term, T>> pairs)
    {
        final LinkedHashMap<Term, T> accum = new LinkedHashMap<Term, T>();

        for (final Pair<Term, T> pair : pairs)
        {
            if (accum.containsKey(pair.left))
            {
                Session.error(pair.left.getLoc(), "duplicate key: {0}",
                    pair.left.dump());
            }
            else
            {
                accum.put(pair.left, pair.right);
            }
        }

        return accum;
    }

    //
    // static actions/helpers
    //

    /**
     *
     */
    public static <T> List<T> flatten(final List<T> h, final List<List<T>> tail)
    {
        final List<T> result = new ArrayList<T>();
        result.addAll(h);
        for (final List<T> list : tail)
            result.addAll(list);
        return result;
    }

    /**
     *
     */
    public static <T> List<T> empty()
    {
        return Collections.emptyList();
    }

    /**
     *
     */
    public static <K, V> Pair<K, V> assoc(final K k, final V v)
    {
        return Pair.create(k, v);
    }

    /**
     * cons a list (tail may be null)
     */
    public static <T> List<T> cons(final T head, final List<T> tail)
    {
        final List<T> list = new ArrayList<T>();
        list.add(0, head);
        if (tail != null)
            list.addAll(tail);
        return list;
    }

    public static <T> List<T> concat(final List<T> head, final List<T> tail)
    {
        final List<T> list = new ArrayList<T>(head.size() + tail.size());
        list.addAll(head);
        list.addAll(tail);
        return list;
    }

    public static List<String> distribute(final String prefix, final List<String> rest)
    {
        final List<String> list = new ArrayList<String>(rest.size());
        for (final String s : rest) list.add(prefix + s);
        return list;
    }
}
