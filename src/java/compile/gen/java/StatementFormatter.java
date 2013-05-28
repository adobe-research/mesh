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

import compile.*;
import compile.gen.java.inline.TermInliner;
import compile.module.Module;
import compile.module.Scope;
import compile.term.*;
import compile.term.visit.BindingVisitorBase;
import compile.type.*;
import runtime.IntrinsicTypeRecorder;
import runtime.rep.Record;
import runtime.rep.Tuple;
import runtime.rep.lambda.Lambda;
import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.list.ListValue;
import runtime.rep.list.PersistentList;
import runtime.rep.map.MapValue;
import runtime.rep.map.PersistentMap;

import java.util.*;

import static compile.parse.ApplyFlavor.StructAddr;

/**
 * Formats terms into Java source expressions. Used for Javassist codegen.
 * Note: what we're generating isn't quite legal Java--
 * Javassist lets a few things past that javac doesn't like.
 * (E.g. casts on top-level statements provoke "not a statement", and
 * forward slashes are escaped in string literals.)
 * So compiling source dumps almost works without edits but not quite.
 * In the shell, $u dumps source to shell, $w dumps .java and .class files.
 *
 * @author Basil Hosmer
 */
public final class StatementFormatter extends BindingVisitorBase<String>
{
    // instance

    /**
     * our compilation unit
     */
    private final Unit unit;

    /**
     * our type formatter
     */
    private final TypeMapper typeMapper;

    /**
     * our current scope--our module if we're generating top-level code,
     * or the current lambda. (we generate local functions out of line,
     * so this is not a stack)
     */
    private Scope currentScope;

    /**
     * 0 if we're generating top-level module code, 1 if we're in a lambda
     * (see above comment)
     */
    private int lambdaDepth;

    /**
     * current statement being generated
     */
    private Statement currentStatement;

    /**
     * if true, we're currently generating expr code, even if
     * {@link #currentStatement} is a non-result unbound term.
     * Used by inliner to avoid generating  mid-expr statement lists.
     */
    private boolean inExpr;

    /**
     * map from symbol constants to generated field names
     */
    private Map<SymbolLiteral, String> symbolConstants;

    /**
     * map from record types to generated keyset field names
     */
    private Map<List<SimpleLiteralTerm>, String> keyListConstants;

    /**
     *
     */
    private ArrayDeque<Class<?>> lvalueClassStack;

    /**
     *
     */
    public StatementFormatter(final Unit unit)
    {
        this.unit = unit;

        this.typeMapper = new TypeMapper();

        this.currentScope = unit.getModule();

        this.lambdaDepth = 0;

        this.currentStatement = null;

        this.inExpr = false;

        this.symbolConstants = new HashMap<SymbolLiteral, String>();

        this.keyListConstants = new HashMap<List<SimpleLiteralTerm>, String>();

        this.lvalueClassStack = new ArrayDeque<Class<?>>();
    }

    /**
     * After generating constant bindings, {@link ModuleClassGenerator} installs
     * them here. We don't generate these as bona fide bindings (and ref to them)
     * earlier in the pipeline so that we're free to pick which constants to treat
     * this way. E.g. when generating Java source we don't bother collecting
     * number or string constants because we can just dump them into generated
     * code.
     */
    public void addSymbolConstant(final SymbolLiteral term, final String name)
    {
        symbolConstants.put(term, name);
    }

    public void addKeyListConstant(
        final List<SimpleLiteralTerm> keySet, final String name)
    {
        keyListConstants.put(keySet, name);
    }

    public boolean getInExpr()
    {
        return inExpr;
    }

    public Class<?> mapType(final Type type)
    {
        return typeMapper.map(type);
    }

    public void setInExpr(final boolean inExpr)
    {
        this.inExpr = inExpr;
    }

    public Class<?> getLValueClass()
    {
        return lvalueClassStack.peek();
    }

    public void pushLValueClass(final Class<?> c)
    {
        lvalueClassStack.push(c);
    }

    public void popLValueClass()
    {
        lvalueClassStack.pop();
    }

    /**
     * true if the current statement is an unbound value (i.e.,
     * expr as statement) that is not providing the result of
     * a lambda, and the given term is its value.
     */
    public boolean isNonResultUnboundValue(final Term term)
    {
        if (!(currentStatement instanceof UnboundTerm))
            return false;

        if (term != ((UnboundTerm)currentStatement).getValue())
            return false;

        return currentScope instanceof Module ||
            currentStatement != ((LambdaTerm)currentScope).getResultStatement();
    }

    /**
     *
     */
    public boolean statementsOkay(final ApplyTerm apply)
    {
        return !getInExpr() && isNonResultUnboundValue(apply);
    }

    // convenience

    public String formatType(final Type type)
    {
        final Class<?> c = typeMapper.map(type);
        return formatClass(c);
    }

    private String formatClass(final Class<?> c)
    {
        final String name = c == Object[].class ? "Object[]" : c.getName();
        return name.startsWith("java.lang.") ? c.getSimpleName() : name;
    }

    /**
     * Generate Java code for statement.
     * NOTE: assumes we're generating code for a lambda body, or some fragment that contains no bindings.
     * Not suitable for top-level code containing bindings. For that use {@link #formatTopLevelStatement}.
     */
    public String formatInLambdaStatement(final Statement statement)
    {
        final Statement save = currentStatement;
        currentStatement = statement;

        lambdaDepth++;

        final String result = statement.isBinding() ?
            visitBinding((Binding)statement) :
            formatTermAs(((UnboundTerm)statement).getValue(), Object.class);

        lambdaDepth--;

        currentStatement = save;

        return result;
    }

    /**
     * Generate Java code for top-level term. Difference between top-level and lambda code is
     * that value bindings are not declared inline, but are assumed to have been generated elsewhere.
     * I.e. for var binding x = 5, it's the difference between lambda code
     * <pre>
     * class C { void f() { int x = 5; ... } }
     * </pre>
     * and top-level code
     * <pre>
     * class C { int x; C() { x = 5; } }
     * </pre>
     * For lambda code use {@link #formatInLambdaStatement}.
     */
    public String formatTopLevelStatement(final Statement statement)
    {
        lambdaDepth = 0;
        currentStatement = statement;

        if (statement.isBinding())
        {
            return visitBinding((Binding)statement);
        }
        else if (statement instanceof UnboundTerm)
        {
            return formatTermAs(((UnboundTerm)statement).getValue(), Object.class);
        }
        else if (statement instanceof ImportStatement)
        {
            final ImportStatement importStatement = (ImportStatement)statement;
            final Unit imported = unit.getImportedUnit(importStatement.getFrom());
            assert imported != null : "Imported unit not created";
            final ClassDef def = imported.getModuleClassDef();

            return def.getName() + "." + Constants.INSTANCE + ".run()";
        }
        return "";
    }

    /**
     *
     */
    public String fixup(final Loc loc, final String expr, final Type rtype)
    {
        return fixup(loc, expr, typeMapper.map(rtype), getLValueClass());
    }

    /**
     * reconcile incoming rvalue's type with lvalue context.
     */
    public String fixup(final Loc loc, final String expr, final Type rtype,
        final Class<?> lclass)
    {
        return fixup(loc, expr, typeMapper.map(rtype), lclass);
    }

    /**
     *
     */
    public String fixup(final Loc loc, final String expr, final Class<?> rclass)
    {
        return fixup(loc, expr, rclass, getLValueClass());
    }

    /**
     *
     */
    public String fixup(final Loc loc, final String expr,
        final Class<?> rclass, final Type ltype)
    {
        return fixup(loc, expr, rclass, typeMapper.map(ltype));
    }

    /**
     * reconcile incoming rvalue's rep class with lvalue context
     */
    public String fixup(final Loc loc, final String expr,
        final Class<?> rclass, final Class<?> lclass)
    {
        if (lclass.isAssignableFrom(rclass))
        {
            // no cast needed
            return expr;
        }

        final String result;

        final String lclassName = formatClass(lclass);
        final String rclassName = formatClass(rclass);

        if (lclass.isPrimitive())
        {
            if (rclass.isPrimitive())
            {
                if (ClassUtils.canCastFromPrimToPrim(lclass, rclass))
                {
                    result = "((" + formatClass(lclass) + ")" + expr + ")";
                }
                else
                {
                    // any cross-primitive casting means typing went wrong upstream
                    Session.error(loc, "internal error: incompatible reps {0} := {1}",
                        lclassName, rclassName);

                    result = expr;
                }
            }
            else
            {
                // try unboxing rvalue to lclass
                final Class<?> lbclass = ClassUtils.boxed(lclass);

                if (lbclass.isAssignableFrom(rclass))
                {
                    result = ClassUtils.unbox(expr, rclass);
                }
                else if (rclass.isAssignableFrom(lbclass))
                {
                    final String castExpr =
                        "((" + formatClass(lbclass) + ")" + expr + ")";
                    result = ClassUtils.unbox(castExpr, lbclass);
                }
                else
                {
                    Session.error(loc, "internal error: incompatible reps in {0} := {1}",
                        lclassName, rclassName);

                    result = expr;
                }
            }
        }
        else if (rclass.isPrimitive())
        {
            // rtype must box to a subclass of ltype
            if (!lclass.isAssignableFrom(ClassUtils.boxed(rclass)))
            {
                Session.error(loc, "internal error: incompatible reps {0} := {1}",
                    lclassName, rclassName);

                result = expr;
            }
            else
            {
                result = ClassUtils.box(expr, rclass);
            }
        }
        else
        {
            // downcast if compatible
            if (rclass.isAssignableFrom(lclass))
            {
                result = "((" + lclassName + ")" + expr + ")";
            }
            else
            {
                Session.error(loc, "internal error: incompatible reps {0} := {1}",
                    lclassName, rclassName);

                result = expr;
            }
        }

//        if (Session.isDebug())
//            Session.debug(loc,
//                "fixup({0}, {1}) lclass = {2}, result = {3}",
//                expr, rclassName, lclassName, result);

        return result;
    }

    // TermVisitor

    /**
     *
     */
    @Override
    public String visitTerm(final Term term)
    {
        assert false;
        return null;
    }

    /**
     * format term, adding boxing and casting as necessary to make it
     * compatible with the given representation class
     */
    public String formatTermAs(final Term term, final Class<?> lclass)
    {
        pushLValueClass(lclass);
        final String result = super.visitTerm(term);
        popLValueClass();

        return result;
    }

    /**
     * format term, adding boxing and casting as necessary to make it
     * compatible with the representation class for the given type.
     */
    public String formatTermAs(final Term term, final Type ltype)
    {
        return formatTermAs(term, typeMapper.map(ltype));
    }

    // BindingVisitor

    /**
     * ValueBindings produce different code depending on binding attributes
     * and context.
     * <ul>
     * <li/>in lambda body code, all value bindings are handled inline.
     * <li/>in top-level module code (which winds up in the constructor
     * for the class that hosts the module state), all bindings have been
     * declared as class fields, and constant bindings have been initialized.
     * So in that case here we only generate updates to non-constant bindings.
     * Note that this means (a) we may generate an empty statement, (b) final
     * but non-constant top-level bindings can't be final fields in Java.
     * </ul>
     */
    @Override
    public String visit(final LetBinding let)
    {
        final String lhs = lambdaDepth == 0 ?  formatNameRef(let) : formatVarDeclLHS(let);
        final String rhs;
        if (let.isIntrinsic())
        {
            // Record a dump of the intrinsic type for use when printing it.
            rhs = IntrinsicTypeRecorder.class.getName() +
                ".record(" + formatIntrinsicAsRHS(let) +
                ", \"" + let.getType().dump() + "\")";
        }
        else
        {
            rhs = formatTermAs(let.getValue(), typeMapper.map(let.getType()));
        }
        return lhs + " = " + rhs;
    }

    /**
     * factored for external use
     */
    public String formatVarDeclLHS(final LetBinding let)
    {
        return "final " + formatTypeName(let) + " " + formatNameRef(let);
    }

    /**
     * when we have a lambda term available, we can use the lambda's java class
     * as a type in generate generated code. Otherwise we go with generic type name.
     */
    public String formatTypeName(final ValueBinding binding)
    {
        if (binding.isLet())
        {
            final Term rhs = binding.getValue();

            if (rhs instanceof LambdaTerm)
                return unit.ensureLambdaClassName((LambdaTerm)rhs);
        }

        return formatType(binding.getType());
    }

    /**
     *
     */
    @Override
    public String visit(final ParamBinding param)
    {
        return formatType(param.getType()) + " " + formatName(param.getName());
    }

    // TermVisitor

    /**
     * Format a name reference
     */
    @Override
    public String visit(final RefTerm ref)
    {
        // now we have a chance to constant-propagate refs to intrinsics
        final ValueBinding binding = ref.getBinding();
        if (binding.isLet())
        {
            final LetBinding let = (LetBinding)binding;
            if (let.isIntrinsic())
                return formatIntrinsicAsRHS(let);
        }

        return fixup(ref.getLoc(), formatNameRef(binding), ref.getType());
    }

    /**
     * Helper - formats a binding name as a reference expression -
     * qualifies globals defined outside the scope from which the
     * reference is being made. (We only need to qualify globals--
     * any out-of-scope lambdas will have been captured
     * in a closure.)
     * Note: package local, used by class generators.
     */
    String formatNameRef(final ValueBinding binding)
    {
        final Scope bindingScope = binding.getScope();

        if (bindingScope instanceof Module && bindingScope != currentScope)
        {
            // qualify top-level (i.e., module not lambda) bindings
            // from outside refScope
            final Module bindingModule = (Module)bindingScope;

            // find the binding's unit.
            // TODO should use a bidi map or something instead of lookup
            final Unit bindingUnit = (bindingModule == unit.getModule()) ? unit :
                unit.getImportedUnit(bindingModule.getName());

            // Note: we have to actually use the class name if it's there,
            // as it might be nonstandard for intrinsics.
            final ClassDef moduleClassDef = bindingUnit.getModuleClassDef();

            // But it might not be there, e.g. if we're generating a lambda
            // class before its defining module class.
            final String moduleNameRef =
                (moduleClassDef != null ?
                    bindingUnit.getModuleClassDef().getName() :
                    ModuleClassGenerator.qualifiedModuleClassName(bindingModule))
                + "." + Constants.INSTANCE;

            return moduleNameRef + "." + formatName(binding.getName());
        }
        else
        {
            // catch self-reference in lambda
            if (binding.isLet())
            {
                final LetBinding let = (LetBinding)binding;
                if (!let.isIntrinsic() && let.getValue() == currentScope)
                {
                    // if we have no captured lambda bindings, we're in
                    // a static method and must use INSTANCE object.
                    // otherwise can just pass ourselves
                    return ((LambdaTerm)currentScope).hasCapturedLambdaBindings()
                        ? "this" : Constants.INSTANCE;
                }
            }

            return formatName(binding.getName());
        }
    }

    /**
     * Here we access special knowledge about how to go from an intrinsic
     * let binding in the current scope, to a compatible value in the
     * underlying Java environment
     */
    public String formatIntrinsicAsRHS(final LetBinding let)
    {
        final IntrinsicsResolver resolver = IntrinsicsResolver.getThreadLocal();
        try
        {
            final IntrinsicLambda intrinsic = resolver.resolve(let);
            return intrinsic.getClass().getName() + "."+ Constants.INSTANCE;
        }
        catch (IntrinsicsResolver.ResolutionError e)
        {
            Session.error(let.getLoc(), e.getMessage());
            assert false : "Intrinsic should have been previously resolved";
            return "";
        }
    }

    /**
     * Because we're generating Java source, we have to stay away
     * from Java keywords. A simple, fast but still relatively
     * readable way to do this is just to tack on an underscore
     * to every name. (Of course doing BC gen we won't need to do this,
     * but then again we won't be able to read the generated code either.)
     */
    public static String formatName(final String name)
    {
        return "_" + name;
    }

    /**
     *
     */
    public String visit(final ParamValue paramValue)
    {
        assert false : "undigested param value in codegen";
        return null;
    }

    /**
     * Generate Java source expression denoting a boolean literal.
     */
    @Override
    public String visit(final BoolLiteral boolLiteral)
    {
        final String expr = boolLiteral.getValue() ? "true" : "false";
        return fixup(boolLiteral.getLoc(), expr, Types.BOOL);
    }

    /**
     * Generate Java source expression denoting a natural number literal.
     */
    @Override
    public String visit(final IntLiteral intLiteral)
    {
        final String expr = "" + intLiteral.getValue();
        return fixup(intLiteral.getLoc(), expr, Types.INT);
    }

    /**
     * Generate Java source expression denoting a long number literal.
     */
    public String visit(final LongLiteral longLiteral)
    {
        final String expr = "" + longLiteral.getValue() + "L";
        return fixup(longLiteral.getLoc(), expr, Types.LONG);
    }

    /**
     * Generate Java source expression denoting a floating point number literal.
     */
    @Override
    public String visit(final DoubleLiteral doubleLiteral)
    {
        final double value = doubleLiteral.getValue();
        final String expr = Double.isNaN(value) ? "Double.NaN" : ("" + value);
        return fixup(doubleLiteral.getLoc(), expr, Types.DOUBLE);
    }

    /**
     * Generate Java source expression denoting a string literal.
     */
    @Override
    public String visit(final StringLiteral stringLiteral)
    {
        final String expr =
            "\"" + StringUtils.escapeJava(stringLiteral.getValue()) + "\"";

        return fixup(stringLiteral.getLoc(), expr, Types.STRING);
    }

    /**
     * Generate Java source expression denoting a Symbol literal.
     * We use pregenerated constants stashed in fields.
     */
    @Override
    public String visit(final SymbolLiteral symbolLiteral)
    {
        final String constant = symbolConstants.get(symbolLiteral);

        final Loc loc = symbolLiteral.getLoc();

        if (constant != null)
            return fixup(loc, constant, Types.SYMBOL);

        final String expr = formatType(Types.SYMBOL) +
            ".get(\"" + symbolLiteral.getValue() + "\")";

        return fixup(loc, expr, Types.SYMBOL);

    }

    /**
     * Generate Java source expression to produce a List as specified by a literal term.
     */
    @Override
    public String visit(final ListTerm list)
    {
        final String listClassName = PersistentList.class.getName();

        // format

        final String expr;

        final List<Term> items = list.getItems();

        if (items.isEmpty())
        {
            expr = listClassName + ".EMPTY";
        }
        else
        {
            final StringBuilder buf = new StringBuilder(listClassName).
                append(".alloc(").append(items.size()).append(")");

            int i = 0;
            for (final Term item : items)
            {
                final String itemExpr = formatTermAs(item, Object.class);

                buf.append(".updateUnsafe").append("(").
                    append(i).append(", ").
                    append(itemExpr).append(")");

                i++;
            }

            expr = buf.toString();
        }

        return fixup(list.getLoc(), expr, list.getType());
    }

    /**
     * Generate Java source expression to produce a Map as specified by a literal term.
     * Note that we produce an unversioned map.
     */
    @Override
    public String visit(final MapTerm map)
    {
        final StringBuilder buf;

        final Iterator<Map.Entry<Term, Term>> iter =
            map.getItems().entrySet().iterator();

        if (!iter.hasNext())
        {
            buf = new StringBuilder(PersistentMap.class.getName()).append(".EMPTY");
        }
        else
        {
            Map.Entry<Term, Term> entry = iter.next();

            final String fkey = formatTermAs(entry.getKey(), Object.class);
            final String fval = formatTermAs(entry.getValue(), Object.class);

            buf = new StringBuilder(PersistentMap.class.getName()).
                append(".single(").append(fkey).append(", ").append(fval).append(")");

            while (iter.hasNext())
            {
                entry = iter.next();

                final String nkey = formatTermAs(entry.getKey(), Object.class);
                final String nval = formatTermAs(entry.getValue(), Object.class);

                buf.append(".assocUnsafe(").
                    append(nkey).append(", ").append(nval).append(")");
            }
        }

        return fixup(map.getLoc(), buf.toString(), map.getType());
    }

    /**
     * Generate Java source expression denoting a Tuple as specified by a literal term
     */
    @Override
    public String visit(final TupleTerm tuple)
    {
        final List<String> formatted = new ArrayList<String>();

        final List<Term> items = tuple.getItems();

        final String expr;

        if (items.size() == 0)
        {
            expr = Tuple.class.getName() + ".UNIT";
        }
        else
        {
            for (final Term term : items)
                formatted.add(formatTermAs(term, Object.class));

            expr = Tuple.class.getName() + ".from(" +
                formatObjectArrayLiteral(formatted) + ")";
        }

        return fixup(tuple.getLoc(), expr, Tuple.class);
    }

    /**
     * Generate Java source expression denoting a Record as specified by a literal term
     */
    @Override
    public String visit(final RecordTerm record)
    {
        final Type type = record.getType().deref();
        final List<SimpleLiteralTerm> keyList = Types.recKeyList(type);

        // get keyset field name
        final String keyListField = keyListConstants.get(keyList);
        if (keyListField == null)
            assert false;

        // generate value exprs
        final List<String> valueExprs = new ArrayList<String>();
        {
            final Type fieldTypes = Types.recFields(type).deref();
            assert fieldTypes instanceof TypeMap;

            final Map<Term, Term> valueMap = record.getItems();

            // important: since record types are permutation groups,
            // must guarantee that terms are in symbol order.
            for (final Term key : ((TypeMap)fieldTypes).getMembers().keySet())
                valueExprs.add(formatTermAs(valueMap.get(key), Object.class));
        }

        final String expr = Record.class.getName() + ".from(" +
            keyListField + ", " +
            formatObjectArrayLiteral(valueExprs) + ")";

        return fixup(record.getLoc(), expr, record.getType());
    }

    /**
     * Helper - generate Java source for an Object array constructor call.
     * Complicated by a Javassist bug that rejects Object[]{}
     */
    public String formatObjectArrayLiteral(final Collection<String> formattedItems)
    {
        final StringBuilder buf = new StringBuilder().
            append("new ").
            append(Constants.OBJECT);

        if (formattedItems.size() > 0)
        {
            buf.append("[]{").
                append(StringUtils.join(formattedItems, ", ")).
                append("}");
        }
        else
        {
            buf.append("[0]");
        }

        return buf.toString();
    }

    /**
     * Generate a Java expression constructing a lambda term.
     */
    @Override
    public String visit(final LambdaTerm lambda)
    {
        // we may be generating lambda class def here for the first time - push scope
        final Scope prevScope = currentScope;
        currentScope = lambda;

        final ClassDef classDef = unit.ensureLambdaClassDef(lambda, this);

        currentScope = prevScope;

        // generate constructor call for lambda class -
        // may need to pass captured variable initializers
        final String expr;

        if (lambda.hasCapturedLambdaBindings())
        {
            final Collection<ValueBinding> capturedBindings =
                lambda.getCapturedLambdaBindings().values();

            final List<String> ctorParams =
                new ArrayList<String>(capturedBindings.size());

            for (final ValueBinding binding : capturedBindings)
                ctorParams.add(formatNameRef(binding));

            expr = "new " + classDef.getName() + "(" +
                StringUtils.join(ctorParams, ", ") + ")";
        }
        else
        {
            expr = classDef.getName() + "." + Constants.INSTANCE;
        }

        return fixup(lambda.getLoc(), expr, lambda.getType());
    }

    /**
     * ApplyTerms include function invocations, collection lookups and structure member accesses.
     */
    @Override
    public String visit(final ApplyTerm apply)
    {
        final Term baseTerm = apply.getBase();
        final Term argTerm = apply.getArg();

        final Type baseType = baseTerm.getType().deref();

        switch (apply.getFlav())
        {
            case FuncApp:
            {
                // calls to nominal type constructors are no-ops
                final String instance = tryNominalCoerces(baseTerm, argTerm);
                if (instance != null)
                    return instance;

                // try to optimize based on a lambda dereference
                final String invokeExpr = tryLambdaDeref(apply);
                if (invokeExpr != null)
                    return invokeExpr;

                return formatLambdaApplyTerm(baseTerm, argTerm);
            }

            case CollIndex:
            {
                if (Types.isList(baseType))
                    return formatListApplyTerm(apply);

                if (Types.isMap(baseType))
                    return formatMapApplyTerm(apply);

                break;
            }

            case StructAddr:
            {
                if (Types.isTup(baseType))
                    return formatTupleApplyTerm(apply);

                if (Types.isRec(baseType))
                    return formatRecordApplyTerm(apply);

                break;
            }

            default:
                break;
        }

        Session.error(apply.getLoc(),
            "internal error: invalid base term {0} : {1} in application {2}",
            baseTerm.dump(), baseType.dump(), apply.dump());

        return null;
    }

    /**
     * application on a list is positional lookup.
     */
    private String formatListApplyTerm(final ApplyTerm apply)
    {
        final Term base = apply.getBase();
        final Term arg = apply.getArg();

        final String expr =
            formatTermAs(base, ListValue.class) +
                ".get(" + formatTermAs(arg, int.class) + ")";

        return fixup(apply.getLoc(), expr, Object.class);
    }

    /**
     * application on a map is key lookup.
     */
    private String formatMapApplyTerm(final ApplyTerm apply)
    {
        final Term base = apply.getBase();
        final Term arg = apply.getArg();

        final String expr =
            formatTermAs(base, MapValue.class) +
                ".get(" + formatTermAs(arg, Object.class) + ")";

        return fixup(apply.getLoc(), expr, Object.class);
    }

    /**
     * application on a tuple is positional member access.
     */
    public String formatTupleApplyTerm(final ApplyTerm apply)
    {
        final Term arg = apply.getArg();
        final Term base = apply.getBase();
        final Type applyType = apply.getType();

        if (Types.isSum(applyType))
            Session.error(apply.getLoc(),
                "internal error: dynamic tuple access not currently supported");

        assert arg.getType() == Types.INT;

        {
            final Term argDeref = arg instanceof RefTerm ? ((RefTerm)arg).deref() : arg;
            assert argDeref.isConstant();
        }

        final String expr = formatTermAs(base, Tuple.class) + ".get(" +
            formatTermAs(arg, int.class) + ")";

        return fixup(apply.getLoc(), expr, Object.class);
    }

    /**
     * application on a record is keyed member access.
     * TODO when poly recs/vars are in, can do more efficient lookup
     */
    private String formatRecordApplyTerm(final ApplyTerm apply)
    {
        final Term base = apply.getBase();
        final Term arg = apply.getArg();

        //final Type baseType = base.getType();
        final Type applyType = apply.getType();

        if (Types.isSum(applyType))
            Session.error(apply.getLoc(),
                "internal error: dynamic record access not currently supported");

        {
            final Term argDeref = arg instanceof RefTerm ? ((RefTerm)arg).deref() : arg;
            assert argDeref.isConstant();
        }

        // name lookup for now, see comment
        final String expr = formatTermAs(base, Record.class) +
            ".get(" + formatTermAs(arg, Object.class) + ")";

        return fixup(apply.getLoc(), expr, Object.class);
    }

    /**
     * generate an application (tupled params), available for any lambda
     */
    private String formatLambdaApplyTerm(final Term base, final Term arg)
    {
        final String expr = formatTermAs(base, Lambda.class) + "." + Constants.APPLY +
            "(" + formatTermAs(arg, Object.class) + ")";

        return fixup(base.getLoc(), expr, Object.class);
    }

    /**
     * Type constructors, destructors are no-ops. If we're applying
     * one, just drill through to the underlying argument.
     * TODO something less spidery for detecting these?
     */
    private String tryNominalCoerces(final Term base, final Term arg)
    {
        if (!(base instanceof RefTerm))
            return null;

        final RefTerm baseRef = (RefTerm)base;

        if (!baseRef.getBinding().isLet())
            return null;

        final LetBinding baseLet = (LetBinding)baseRef.getBinding();

        if (baseLet.isIntrinsic() || !(baseLet.getValue() instanceof LambdaTerm))
            return null;

        final LambdaTerm lambda = (LambdaTerm)baseLet.getValue();
        final Type lambdaType = lambda.getType();
        final Type paramType = Types.funParam(lambdaType).deref();
        final Type resultType = Types.funResult(lambdaType).deref();

        if (paramType instanceof TypeDef)
        {
            // if param type is nominal, check for type dtor
            final TypeDef paramDef = (TypeDef)paramType;

            if (!paramDef.isNominal())
                return null;

            if (lambda != paramDef.getDtorLet().getValue())
                return null;

            return formatTermAs(arg, arg.getType());
        }
        else if (resultType instanceof TypeDef)
        {
            // if result type is nominal, check for type ctor
            final TypeDef resultDef = (TypeDef)resultType;

            if (!resultDef.isNominal())
                return null;

            if (lambda != resultDef.getCtorLet().getValue())
                return null;

            return formatTermAs(arg, arg.getType());
        }

        return null;
    }

    /**
     * try things based on derefing the lambda:
     * 0. inline some obvious stuff--direct conditionals, etc.
     * 1. generate an invocation with scattered params if we can - which means
     * when we have a specific Java signature for invoke(). Currently this
     * is true when our base term resolves to a specific Java implementation
     * class (so e.g. not a param ref), and that implementation has invoke()
     * NOTE several early returns
     */
    private String tryLambdaDeref(final ApplyTerm apply)
    {
        final Loc loc = apply.getLoc();

        final Term base = apply.getBase();
        final Type baseType = base.getType();
        assert Types.isFun(baseType);

        final Term arg = apply.getArg();
        final Type argType = arg.getType();

        // note that we don't simply take the result type of the application.
        // our generated code must deal with the unspecialized java type
        // produced by the polymorphic function.
        final Type resultType = Types.funResult(baseType);
        final Type paramType = Types.funParam(baseType);

        final InvokeInfo info = getInvokeInfo(base);
        if (info == null)
            return null;

        // format base expr
        final String invokeBase;
        if (info.className != null)
        {
            // non-null class name means this is not a closure,
            // so we have a static invoke()

            // NOTE: early return if we inline successfully
            final String inlined = TermInliner.tryInlining(apply, info, this);

            if (inlined != null)
            {
                if (Session.isDebug())
                    Session.debug(loc, "inlined: {0} => {1}", apply.dump(), inlined);

                return inlined;
            }

            // NOTE: visit base term even if we use class name directly,
            // to trigger code gen
            formatTermAs(base, baseType);

            invokeBase = info.className + "." + Constants.INVOKE;
        }
        else
        {
            invokeBase = formatTermAs(base, baseType) + "." + Constants.INVOKE;
        }

        // format call
        if (info.mode == InvokeInfo.InvokeMode.Scatter)
        {
            final String expr;

            final List<Type> paramTypes =
                ((TypeList)Types.tupMembers(paramType).deref()).getItems();

            final List<String> formatted = new ArrayList<String>();

            if (arg instanceof TupleTerm)
            {
                // argument is a tuple literal - just pass individual members

                final List<Term> args = ((TupleTerm)arg).getItems();

                for (int i = 0; i < paramTypes.size(); i++)
                    formatted.add(formatTermAs(args.get(i), paramTypes.get(i)));

                expr = invokeBase + "(" + StringUtils.join(formatted, ", ") + ")";
            }
            else if (arg instanceof RefTerm)
            {
                // if base is a simple ref term, call invoke with a scatter list

                assert Types.isTup(argType);

                final Type members = Types.tupMembers(argType);
                assert members instanceof TypeList;

                final Loc argLoc = arg.getLoc();

                final List<Type> argTypes = ((TypeList)members).getItems();

                for (int i = 0; i < paramTypes.size(); i++)
                {
                    final ApplyTerm accessTerm =
                        new ApplyTerm(argLoc, arg, new IntLiteral(argLoc, i), StructAddr);

                    accessTerm.setType(argTypes.get(i));

                    formatted.add(formatTermAs(accessTerm, paramTypes.get(i)));
                }

                expr = invokeBase + "(" + StringUtils.join(formatted, ", ") + ")";
            }
            else
            {
                // NOTE: more elaborate strategies (temp assignments, etc.)
                // don't seem to pay, so keeping things simple here.
                return null;
            }

            // invoke result is strongly typed
            return fixup(loc, expr, resultType);
        }
        else
        {
            final String expr = invokeBase + "(" + formatTermAs(arg, paramType) + ")";

            return fixup(loc, expr, resultType);
        }
    }

    /**
     * Build an info packet to facilitate some simple optimizations, if the
     * base term (of an application) can be dereferenced to a particular lambda,
     * rather than a param.
     * 1. In addition to the canonical apply(arg), Lambdas implement invoke(), which
     * takes a scattered argument list. We want to call that if we can.
     * 2. In non-closure Lambdas, the invoke() is static, so we can call with a
     * direct reference to the Lambda's classname.
     */
    public InvokeInfo getInvokeInfo(final Term base)
    {
        if (base instanceof LambdaTerm)
        {
            final LambdaTerm baseLambda = (LambdaTerm)base;

            // this ensures a stable class name, but doesn't generate code
            final String className = unit.ensureLambdaClassName(baseLambda);

            // can scatter if num args != 1
            final InvokeInfo.InvokeMode mode = baseLambda.getParams().size() == 1 ?
                InvokeInfo.InvokeMode.NoScatter :
                InvokeInfo.InvokeMode.Scatter;

            // if no captured bindings, can call static invoke() directly
            return new InvokeInfo(baseLambda, mode,
                baseLambda.hasCapturedLambdaBindings() ? null : className);
        }
        else if (base instanceof RefTerm)
        {
            // track down ref
            final ValueBinding binding = ((RefTerm)base).getBinding();

            if (binding.isLet())
            {
                // let binding, as opposed to a param binding
                final LetBinding let = (LetBinding)binding;

                if (let.isIntrinsic())
                {
                    final IntrinsicsResolver resolver =
                        IntrinsicsResolver.getThreadLocal();
                    try
                    {
                        final IntrinsicLambda intrinsic = resolver.resolve(let);

                        final String lambdaClass = intrinsic.getClass().getName();
                        final Type baseType = base.getType().deref();

                        if (Types.isFun(baseType))
                        {
                            final Type paramType = Types.funParam(baseType);

                            if (Types.isTup(paramType) &&
                                    Types.tupMembers(paramType) instanceof TypeList)
                            {
                                // scatter calls to non-variadic multi-arg functions
                                return new InvokeInfo(null,
                                        InvokeInfo.InvokeMode.Scatter, lambdaClass);
                            }
                            else
                            {
                                // otherwise, intrinsic takes a single argument
                                return new InvokeInfo(null,
                                        InvokeInfo.InvokeMode.NoScatter, lambdaClass);
                            }
                        }
                    }
                    catch (IntrinsicsResolver.ResolutionError e)
                    {
                        Session.error(base.getLoc(), e.getMessage());
                        assert false : "Intrinsics should have been previously resolved";
                        return null;
                    }

                    // should be an assert? calling an intrinsic with non-function type?
                    return null;
                }
                else
                {
                    // non-intrinsic, check RHS
                    return getInvokeInfo(let.getValue());
                }
            }
        }

        return null;
    }

    /**
     *
     */
    @Override
    public String visit(final CoerceTerm coerce)
    {
        return formatTermAs(coerce.getTerm(), coerce.getType());
    }
}
