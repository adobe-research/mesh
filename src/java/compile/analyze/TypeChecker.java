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
package compile.analyze;

import com.google.common.collect.*;
import compile.*;
import compile.module.Module;
import compile.module.Scope;
import compile.parse.ApplyFlavor;
import compile.term.*;
import compile.type.*;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.visit.*;

import java.util.*;

/**
 * Typecheck a module.
 *
 * @author Basil Hosmer
 */
public final class TypeChecker extends ModuleVisitor<Type> implements TypeEnv
{
    private final TypeExprPreprocessor typeExprPreprocessor;

    private int nextTypeVar;

    private SubstMap subs;

    private final ArrayDeque<GroupState> groupStateStack;

    private final ArrayDeque<Map<TypeParam, TypeVar>> paramVarStack;

    private final HashSet<Pair<Type, Type>> visited;

    public TypeChecker(final Module module)
    {
        super(module);
        this.typeExprPreprocessor = new TypeExprPreprocessor(this);
        this.groupStateStack = new ArrayDeque<GroupState>();
        this.paramVarStack = new ArrayDeque<Map<TypeParam, TypeVar>>();
        this.visited = new HashSet<Pair<Type, Type>>();
    }

    public boolean check()
    {
        if (Session.isDebug())
            Session.debug(getModule().getLoc(), "Typechecking...");

        nextTypeVar = 0;

        subs = new SubstMap();

        groupStateStack.clear();
        paramVarStack.clear();
        visited.clear();

        return process();
    }

    /**
     * private conduit between us and {@link #typeExprPreprocessor}
     */
    public Type visitTermInType(final Term term)
    {
        return visitTerm(term);
    }

    // pending

    private void pushGroupState()
    {
        groupStateStack.push(new GroupState());
    }

    private void popGroupState()
    {
        groupStateStack.pop();
    }

    private GroupState getGroupState()
    {
        return groupStateStack.peek();
    }

    /**
     *
     */
    private GroupState getParentGroupState()
    {
        final Iterator<GroupState> iter = groupStateStack.iterator();

        iter.next();

        assert iter.hasNext() : "at top level";

        return iter.next();
    }

    /**
     *
     */
    private void addPendingItem(final Typed item)
    {
        getGroupState().addPendingItem(item);
    }

    /**
     *
     */
    private void addPendingDef(final TypeDef def)
    {
        getGroupState().addPendingDef(def);
    }

    // ModuleVisitor

    @Override
    protected void processScope(final Scope scope)
    {
        if (Session.isDebug())
            Session.debug("{0}* scope", indent());

        for (final List<Statement> group : scope.getDependencyGroups())
            processGroup(group);

        if (Session.isDebug())
            Session.debug("{0}* done scope", indent());
    }

    /**
     * debug fanciness - show scope nesting level in section markers
     */
    private String indent()
    {
        String s = "";
        final int n = getStateStack().size() - 1;
        for (int i = 0; i < n; i++)
            s += "*** ";
        return s;
    }

    /**
     * Process a group of mutually type-interdependent statements
     * against the current type environment.
     */
    private void processGroup(final List<Statement> group)
    {
        if (Session.isDebug())
        {
            Session.debug("{0}** group, size = {1}", indent(), group.size());

            for (final Statement stmt : group)
                Session.debug(stmt.getLoc(), "{0}", stmt.dump());
        }

        // reset state for each top-level group: keeps subst map small
        // and type var indexes low. but note that we must stop resetting
        // once any group in a module fails to typecheck, because even though
        // we continue marching through all the groups in the module, a group
        // that fails doesn't get finished (ie quantified, etc.). This means
        // that its bindings, which we in a subsequent group may refer to,
        // are still carrying zombie type vars when we get checked, which
        // we may inadvertantly reuse if we reset, resulting in falsely
        // positive type var equality tests and hence overconstraint.
        //
        if (getCurrentScope() == getModule() && Session.getCurrentErrorCount() == 0)
        {
            subs.clear();
            nextTypeVar = 0;
        }

        // init pending and deferred sets
        pushGroupState();

        // seed let types before traversal
        if (Session.isDebug())
            Session.debug("{0}** lets", indent());

        for (final Statement statement : group)
            if (statement instanceof TypeDef)
                initTypeDef((TypeDef)statement);

        for (final Statement statement : group)
            if (statement.isBinding() && ((Binding)statement).isLet())
                initLet((LetBinding)statement);

        // traverse group statements and typedefs
        for (final Statement statement : group)
            processStatement(statement);

        // quantify types on pending items and decls
        if (Session.getCurrentErrorCount() == 0)
            finishGroup();

        // done--deferrals have been transferred to parent
        popGroupState();

        if (Session.isDebug())
            Session.debug("{0}** done group", indent());
    }

    /**
     * overriden for debug messages
     */
    @Override
    protected void processStatement(final Statement statement)
    {
        if (Session.isDebug())
            Session.debug("{0}*** statement", indent());

        super.processStatement(statement);

        if (Session.isDebug())
            Session.debug("{0}*** done statement", indent());
    }

    /**
     * Initialize type def.
     * TODO these need to be instantiated. Until they are, nested typedefs
     * which refer to outer params are broken.
     * <X> f(x:X) { type P(Y) = (X,Y); { g:P(Int) => g.0 + g.1 } }
     */
    private void initTypeDef(final TypeDef typeDef)
    {
        final Type rhs = typeDef.getValue();

        typeExprPreprocessor.preprocess(rhs);

        addPendingDef(typeDef);
    }

    /**
     * Initialize let by (a) setting its type to a seed type of
     * either an instance of its declared type or a fresh type
     * variable, and (b) adding it as a pending item for the group.
     * <p/>
     * Later, when the let is visited in the course of processing
     * body statements, its RHS will be traversed and the resulting
     * type will be unified with the seed type installed here.
     * Then at group finishing time, the inferred type is quantified,
     * checked for equivalence with the declared type if present,
     * and set.
     */
    private void initLet(final LetBinding let)
    {
        if (let.isIntrinsic())
        {
            // Verify that the declaration is fully-specified
            if (let.getType().hasWildcards())
            {
                Session.error(let.getLoc(),
                    "declared intrinsic {0} does not have a fully-specified type.", let.getName());
            }

            final IntrinsicsResolver resolver = IntrinsicsResolver.getThreadLocal();
            final String errorMsg = resolver.verify(let);
            if (errorMsg != null)
                Session.error(let.getLoc(), errorMsg);
        }

        if (let.hasDeclaredType())
        {
            // if let has a declared type, then seed type is an instance of that

            final Type declaredType = let.getDeclaredType();

            typeExprPreprocessor.preprocess(declaredType);

            let.setType(declaredType.instance(this, true));

            if (Session.isDebug())
                Session.debug(let.getLoc(),
                    "seeded {0} : {1} (decl)",
                    let.dump(), let.getType().dump());
        }
        else
        {
            // if let has a priori type (e.g., rhs is a literal or a ref to an
            // already-typed binding) then seed type is an instance of that,
            // otherwise it's a fresh type variable

            final Type letType = let.getType();

            if (letType != null)
            {
                let.setType(letType.instance(this, true));

                if (Session.isDebug())
                    Session.debug(let.getLoc(),
                        "seeded {0} : {1} (prior)",
                        let.dump(), let.getType().dump());
            }
            else
            {
                final TypeVar type = freshVar(let.getLoc(), Kinds.STAR);
                let.setType(type);

                if (Session.isDebug())
                    Session.debug(let.getLoc(),
                        "seeded {0} : {1}",
                        let.dump(), type.dump());
            }
        }

        // note: add let as pending item before traversal
        // of *any* group terms
        addPendingItem(let);
    }

    /**
     * apply substitutions and (re)quantify types of group items
     */
    private void finishGroup()
    {
        final GroupState groupState = getGroupState();

        final Set<Typed> pendingItems = groupState.pendingItems;
        final Set<Typed> deferredItems = groupState.deferredItems;

        final Set<TypeDef> pendingDefs = groupState.pendingDefs;
        final Set<TypeDef> deferredDefs = groupState.deferredDefs;

        if (pendingItems.isEmpty() && deferredItems.isEmpty() &&
            pendingDefs.isEmpty() && deferredDefs.isEmpty())
            return;

        //
        final Set<TypeVar> ambientVars = getAmbientVars();

        if (Session.isDebug())
            Session.debug("{0}** finishing group, ambients = {1}",
                indent(), TypeDumper.dumpList(ambientVars));

        // collect generated params as we quantify pending items and typedefs,
        // to use in deferrals
        SubstMap params = SubstMap.EMPTY;

        if (!pendingItems.isEmpty())
        {
            for (final Typed pendingItem : pendingItems)
            {
                final SubstMap newParams =
                    finishItem(pendingItem, ambientVars, SubstMap.EMPTY);

                params = params.compose(pendingItem.getLoc(), newParams);
            }
        }

        if (!pendingDefs.isEmpty())
        {
            for (final TypeDef pendingDef : pendingDefs)
            {
                final SubstMap newParams =
                    finishDef(pendingDef, ambientVars, SubstMap.EMPTY);

                params = params.compose(pendingDef.getLoc(), newParams);
            }
        }

        if (!deferredItems.isEmpty())
        {
            for (final Typed deferredItem : deferredItems)
            {
                final SubstMap newParams =
                    finishItem(deferredItem, ambientVars, params);

                if (!newParams.isEmpty())
                    Session.error(deferredItem.getLoc(),
                        "internal error: params created in deferred item type {0}",
                        deferredItem.getType().dump());
            }
        }

        if (!deferredDefs.isEmpty())
        {
            for (final TypeDef deferredDef : deferredDefs)
            {
                final SubstMap newParams =
                    finishDef(deferredDef, ambientVars, params);

                if (!newParams.isEmpty())
                    Session.error(deferredDef.getLoc(),
                        "internal error: params created in deferred def type {0}",
                        deferredDef.getValue().dump());
            }
        }
    }

    /**
     * finish a typed item, by setting its type to a quantified version
     * of its current type. Ambient vars are not quantified, and ambient
     * params (the residue of already-quantified vars) are reused.
     * A map of [newly quantified vars : the params that quantify them]
     * is returned, and is added to the map of ambient params for deferred
     * items.
     * <p/>
     * The procedure for finishing items is complicated a bit by the
     * potential presence of a declared type on the item, which also gets
     * quantified.
     */
    private SubstMap finishItem(
        final Typed item,
        final Set<TypeVar> ambientVars,
        final SubstMap ambientParams)
    {
        final boolean hasDeclType = item.hasDeclaredType();

        // if item has a declared type, quantify it and save both
        // quantified type and generated params for later use

        final SubstMap declTypeParams;
        final Type quantDeclType;

        if (hasDeclType)
        {
            // apply all current substitutions to declared type
            final Type declType = item.getDeclaredType().subst(subs);

            // build type param map, avoiding ambient vars and existing params
            declTypeParams = buildTypeParams(declType, ambientVars, ambientParams);

            // quantify type over both new params and params held by outer-scope types
            quantDeclType = declType.quantify(declTypeParams, ambientParams);

            // set declared type on item
            item.setDeclaredType(quantDeclType);
        }
        else
        {
            declTypeParams = SubstMap.EMPTY;
            quantDeclType = null;
        }

        // now quantify inferred type:

        // apply all current substitutions to inferred type
        final Type itemType = item.getType().subst(subs);

        // build type param map, avoiding ambient vars and existing params
        final SubstMap itemTypeParams =
            buildTypeParams(itemType, ambientVars, ambientParams);

        // quantify type over both new params and params held by outer-scope types
        final Type quantItemType = itemType.quantify(itemTypeParams, ambientParams);

        // set item type, though we may patch in declared type below for aesthetics
        item.setType(quantItemType);

        // if both inferred and declared types are fully quantified (no vars remain),
        // finish, otherwise defer item to parent scope for further quantification

        if (quantItemType.hasVars() || (hasDeclType && quantDeclType.hasVars()))
        {
            // vars remain, defer
            if (groupStateStack.size() == 1)
            {
                Session.error(item.getLoc(),
                    "internal error on item {0}: unquantified type vars at top level",
                    item.dump());
            }
            else
            {
                getParentGroupState().addDeferredItem(item);
            }

            if (Session.isDebug())
                Session.debug(item.getLoc(), "{0} {1} : {2}{3}",
                    ambientParams.isEmpty() ? "deferring" : "re-deferring",
                    item.dump(),
                    quantItemType.dump(),
                    hasDeclType ? " decl " + quantDeclType.dump() : "");
        }
        else
        {
            // finished.
            if (hasDeclType)
            {
                // inferred type must be equivalent to declared type
                if (quantDeclType.equiv(quantItemType))
                {
                    if (Session.isDebug())
                        Session.debug(quantDeclType.getLoc(),
                            "declared type {0} matches inferred type {1}",
                            quantDeclType.dump(), quantItemType.dump());
                }
                else
                {
                    // if dumps are identical, print location of inferred type as well
                    final String declaredDump = quantDeclType.dump();
                    final String inferredDump = quantItemType.dump();

                    Session.error(quantDeclType.getLoc(),
                        "declared type {0} does not agree with inferred type {1} {2}",
                        declaredDump, inferredDump,
                        declaredDump.equals(inferredDump) ? "from " + quantItemType
                            .getLoc() : "");
                }

                // if declared type is complete, use it as item's type.
                // (goal here is roundtripping, e.g. preservation of param names etc.)
                // TODO use declared type particulars even if fragmentary

                if (!quantDeclType.hasWildcards())
                    item.setType(quantDeclType);
            }

            // check post-correctness restrictions
            postInferenceChecks(item);

            if (Session.isDebug())
                Session.debug(item.getLoc(), "{0} {1} : {2}{3}",
                    ambientParams.isEmpty() ? "finished" : "finished deferred",
                    item.dump(),
                    item.getType().dump(),
                    hasDeclType ? " decl " + quantDeclType.dump() : "");
        }

        // return all generated params
        return hasDeclType ?
            declTypeParams.compose(item.getLoc(), itemTypeParams) :
            itemTypeParams;
    }

    /**
     * perform post-inference checks.
     */
    private void postInferenceChecks(final Typed typed)
    {
        if (typed instanceof LambdaTerm)
        {
            final LambdaTerm lambda = (LambdaTerm)typed;
            final Type type = lambda.getType();

            // check that functions from unit don't have a declared param
            if (Types.funParam(type) == Types.unit())
            {
                if (lambda.getParams().size() > 0)
                {
                    Session.error(typed.getLoc(),
                        "functions taking the Unit value cannot declare or use parameter explicitly");
                }
            }
        }
        //else if (typed instanceof ApplyTerm)
        //{
            // TODO value restriction
        //}
    }

    /**
     * helper--build and return the param map that will be used
     * to quantify this type. Avoid quantifying ambient vars, or
     * requantifying already-quantified vars.
     */
    private static SubstMap buildTypeParams(
        final Type type,
        final Set<TypeVar> ambientVars,
        final SubstMap ambientParams)
    {
        // don't quantify any of type's vars that appear in types from outer scopes
        final Set<TypeVar> innerVars = Sets.difference(type.getVars(), ambientVars);

        // don't requantify vars that have already been quantified in outer scopes
        final Set<TypeVar> newVars = Sets.difference(innerVars, ambientParams.keySet());

        // create type params for all vars that don't appear in outer scopes,
        // and have not yet been quantified
        return type.buildParamMap(newVars, ambientVars.size());
    }

    /**
     * finish a typedef, by setting its type to a quantified version
     * of its current type. Ambient vars are not quantified, and ambient
     * params (the residue of already-quantified vars) are reused.
     * A map of [newly quantified vars : the params that quantify them]
     * is returned, and is added to the map of ambient params for deferred
     * items.
     */
    private SubstMap finishDef(
        final TypeDef def,
        final Set<TypeVar> ambientVars,
        final SubstMap ambientParams)
    {
        // quantify declared type:
        // if item has a declared type, quantify it and save both
        // quantified type and generated params for later use

        final SubstMap declTypeParams;
        final Type quantDeclType;

        // apply all current substitutions to declared type
        final Type declType = def.getValue().subst(subs);

        // build type param map, avoiding ambient vars and existing params
        declTypeParams = buildTypeParams(declType, ambientVars, ambientParams);

        // quantify type over both new params and params held by outer-scope types
        quantDeclType = declType.quantify(declTypeParams, ambientParams);

        // set declared type on item
        def.setValue(quantDeclType);

        // if type is fully quantified (no vars remain),
        // finish, otherwise defer item to parent scope for further quantification

        if (quantDeclType.hasVars())
        {
            // vars remain, defer

            getParentGroupState().addDeferredDef(def);

            if (groupStateStack.size() == 1)
                assert false : "unquantified type vars at top level";

            if (Session.isDebug())
                Session.debug(def.getLoc(), "{0} {1}",
                    ambientParams.isEmpty() ? "deferring" : "re-deferring",
                    def.dump());
        }
        else
        {
            if (Session.isDebug())
                Session.debug(def.getLoc(), "{0} {1}",
                    ambientParams.isEmpty() ? "finished" : "finished deferred",
                    def.dump());
        }

        // return all generated params
        return declTypeParams;
    }

    /**
     * Ambient type vars appear in types that are still in progress,
     * i.e., types of terms in parent scopes.
     */
    private Set<TypeVar> getAmbientVars()
    {
        final Set<TypeVar> ambientVars = new LinkedHashSet<TypeVar>();

        final Iterator<GroupState> iter = groupStateStack.iterator();

        // skip to parent
        iter.next();

        while (iter.hasNext())
        {
            for (final Typed pending : iter.next().pendingItems)
            {
                ambientVars.addAll(pending.getType().subst(subs).getVars());
            }
        }

        return ambientVars;
    }

    //
    // TypeEnv
    //

    /**
     *
     */
    public void pushInstanceVars(final Map<TypeParam, TypeVar> map)
    {
        paramVarStack.push(map);
    }

    /**
     *
     */
    public void popInstanceVars()
    {
        paramVarStack.pop();
    }

    /**
     *
     */
    public TypeVar findInstanceVar(final TypeParam param)
    {
        for (final Map<TypeParam, TypeVar> map : paramVarStack)
        {
            final TypeVar var = map.get(param);

            if (var != null)
                return var;
        }

        return null;
    }

    public TypeVar freshVar(final Loc loc)
    {
        return new TypeVar(loc, "t" + (nextTypeVar++), Kinds.STAR);
    }

    public TypeVar freshVar(final Loc loc, final Kind kind)
    {
        return new TypeVar(loc, "t" + (nextTypeVar++), kind);
    }

    public TypeVar freshVar(final TypeParam typeParam)
    {
        return new TypeVar("t" + (nextTypeVar++), typeParam);
    }

    public boolean unify(final Type t1, final Type t2)
    {
        return unify(t1.getLoc(), t1, t2);
    }

    public boolean unify(final Located located, final Type t1, final Type t2)
    {
        return unify(located.getLoc(), t1, t2);
    }

    public boolean unify(final Loc loc, final Type t1, final Type t2)
    {
        if (t1 == null)
        {
            Session.error(loc, "internal error: null first type on unify");
            return false;
        }

        if (t2 == null)
        {
            Session.error(loc, "internal error: null second type on unify");
            return false;
        }

        final Type t1sub = t1.subst(subs);
        assert !t1sub.hasParams() :
            "unify: t1sub contains type params: " + t1sub.dump();

        final Type t2sub = t2.subst(subs);
        assert !t2sub.hasParams() :
            "unify: t2sub contains type params: " + t2sub.dump();

        visited.clear();

        final SubstMap newSubs = t1sub.unify(loc, t2sub, this);

        if (newSubs != null)
        {
            if (Session.isDebug())
                Session.debug(loc, "unify {0}, {1}: mgu {2}",
                    t1sub.dump(), t2sub.dump(), newSubs.dump());

            subs = subs.compose(loc, newSubs);

            return true;
        }
        else
        {
            if (Session.isDebug())
                Session.debug(loc, "unify {0}, {1}: FAILS, subs {2}",
                    t1sub.dump(), t2sub.dump(), subs.dump());

            return false;
        }
    }

    /**
     *
     */
    public boolean checkVisited(final Type left, final Type right)
    {
        final Pair<Type, Type> pair = new Pair<Type, Type>(left, right);
        if (visited.contains(pair))
        {
            return true;
        }
        else
        {
            visited.add(pair);
            return false;
        }
    }

    /**
     * Helper - apply current substitutions to a type
     * and quantify the result, for printing in errors.
     * Here we don't respect ambient type vars,
     * because we want the type to appear in the error
     * in a resonably recognizable form.
     * Protected so {@link #typeExprPreprocessor} has access.
     */
    public Type errorFormat(final Type type)
    {
        final Type subsType = type.subst(subs);

        // NOTE: don't filter out ambients
        //final Set<TypeVar> qvars = Sets.difference(subsType.getVars(), getAmbientVars());

        final SubstMap paramMap = subsType.buildParamMap(subsType.getVars(), 0);

        return subsType.quantify(paramMap, SubstMap.EMPTY);
    }

    //
    // BindingVisitor
    //

    /**
     * overridden for debug
     */
    @Override
    protected Type visitBinding(final Binding binding)
    {
        if (Session.isDebug())
            Session.debug(binding.getLoc(), "> {0}", binding.dump());

        final Type type = super.visitBinding(binding);

        if (Session.isDebug())
            Session.debug(binding.getLoc(), "< {0} : {1}",
                binding.dump(), type.dump());

        return type;
    }

    /**
     * See {@link #initLet} for our prelim pass over lets.
     * There we seed the let's type with either declared
     * type or RHS seed type. Here we're iterating over
     * body statements, and we actually traverse the RHS
     * and unify with the seed type.
     */
    @Override
    public Type visit(final LetBinding let)
    {
        if (let.isIntrinsic())
        {
            return let.getType();
        }

        final Term rhs = let.getValue();

        final Type rhsType = visitTerm(rhs);

        final Type letType = let.getType();

        if (!unify(let.getLoc(), letType, rhsType))
            Session.error(let.getLoc(),
                "let type {0} is incompatible with rhs type {1}",
                errorFormat(letType).dump(),
                errorFormat(rhsType).dump());

        return letType;
    }

    /**
     * Note: typedefs are processed in {@link #initTypeDef},
     * prior to body statement iteration. Since they're explicit
     * type decls, nothing else needs to be done until finishing
     * time.
     */
    @Override
    public Type visit(final TypeDef typeDef)
    {
        return typeDef;
    }

    //
    // TermVisitor
    //

    /**
     * overridden for debug
     */
    @Override
    protected Type visitTerm(final Term term)
    {
        if (Session.isDebug())
            Session.debug(term.getLoc(), "> {0}", term.dump());

        final Type type = super.visitTerm(term);

        if (Session.isDebug())
            Session.debug(term.getLoc(), "< {0} : {1}",
                term.dump(), type.dump());

        return type;
    }

    /**
     * RefTerm.getType() returns the type of its binding,
     * which has been initialized above.
     */
    @Override
    public Type visit(final RefTerm ref)
    {
        final ValueBinding binding = ref.getBinding();

        final Type bindingType = binding.getType();

        final boolean useParamNames = binding.hasDeclaredType();

        return bindingType.instance(this, useParamNames).subst(subs);
    }

    @Override
    public Type visit(final ParamValue paramValue)
    {
        return paramValue.getType();
    }

    @Override
    public Type visit(final BoolLiteral boolLiteral)
    {
        return boolLiteral.getType();
    }

    @Override
    public Type visit(final IntLiteral intLiteral)
    {
        return intLiteral.getType();
    }

    @Override
    public Type visit(final LongLiteral longLiteral)
    {
        return longLiteral.getType();
    }

    @Override
    public Type visit(final DoubleLiteral doubleLiteral)
    {
        return doubleLiteral.getType();
    }

    @Override
    public Type visit(final StringLiteral stringLiteral)
    {
        return stringLiteral.getType();
    }

    @Override
    public Type visit(final SymbolLiteral symbolLiteral)
    {
        return symbolLiteral.getType();
    }

    @Override
    public Type visit(final ListTerm list)
    {
        final Loc loc = list.getLoc();

        final Type seedItemType = freshVar(loc, Kinds.STAR);

        for (final Term item : list.getItems())
        {
            final Type itemType = visitTerm(item);

            if (!unify(item.getLoc(), seedItemType, itemType))
                Session.error(item.getLoc(),
                    "list item type {0} is incompatible with established list item type {1}",
                    errorFormat(item.getType()).dump(),
                    errorFormat(seedItemType).dump());
        }

        final Type type = Types.list(loc, seedItemType).subst(subs);

        list.setType(type);

        addPendingItem(list);

        return type;
    }

    @Override
    public Type visit(final MapTerm map)
    {
        final Loc loc = map.getLoc();

        final Type seedKeyType = freshVar(loc, Kinds.STAR);
        final Type seedValueType = freshVar(loc, Kinds.STAR);

        for (final Map.Entry<Term, Term> entry : map.getItems().entrySet())
        {
            final Term key = entry.getKey();

            final Type keyType = visitTerm(key);

            if (!unify(key, seedKeyType, keyType))
                Session.error(key.getLoc(),
                    "map key type {0} is incompatible with established map key type {1}",
                    errorFormat(key.getType()).dump(),
                    errorFormat(seedKeyType).dump());

            final Term value = entry.getValue();

            final Type valueType = visitTerm(value);

            if (!unify(value, seedValueType, valueType))
                Session.error(value.getLoc(),
                    "map value type {0} is incompatible with established map value type {1}",
                    errorFormat(value.getType()).dump(),
                    errorFormat(seedValueType).dump());
        }

        final Type type = Types.map(loc, seedKeyType, seedValueType).subst(subs);

        map.setType(type);

        addPendingItem(map);

        return type;
    }

    @Override
    public Type visit(final TupleTerm tuple)
    {
        final List<Type> itemTypes = new ArrayList<Type>();

        for (final Term item : tuple.getItems())
            itemTypes.add(visitTerm(item));

        final Type type = Types.tup(tuple.getLoc(), itemTypes);

        tuple.setType(type);

        addPendingItem(tuple);

        return type;
    }

    @Override
    public Type visit(final RecordTerm rec)
    {
        final Loc loc = rec.getLoc();

        // keys

        final Type seedKeyType = freshVar(loc, Kinds.STAR);

        final Set<Term> keySet = rec.getItems().keySet();

        for (final Term key : keySet)
        {
            if (!key.isConstant())
                Session.error(key.getLoc(),
                    "key {0} is not a compile-time constant", key.dump());

            final Type keyType = visitTerm(key);

            if (!unify(key.getLoc(), seedKeyType, keyType))
                Session.error(key.getLoc(),
                    "record key type {0} is incompatible with established key type {1}",
                    errorFormat(key.getType()).dump(),
                    errorFormat(seedKeyType).dump());
        }

        final ChoiceType
            keyEnum = new ChoiceType(loc, seedKeyType.subst(subs), keySet);

        // values

        final ImmutableMap.Builder<Term, Type> builder = ImmutableMap.builder();

        for (final Map.Entry<Term, Term> entry : rec.getItems().entrySet())
            builder.put(entry.getKey(), visitTerm(entry.getValue()));

        final Map<Term, Type> entryTypes = builder.build();

        // done

        final TypeApp type = Types.rec(loc, new TypeMap(loc, keyEnum, entryTypes));

        rec.setType(type);

        addPendingItem(rec);

        return type;
    }

    @Override
    public Type visit(final LambdaTerm lambda)
    {
        if (lambda.hasDeclaredType())
        {
            // process any type declarations in lambda sig.
            // note that finishing the declared type will happen
            // as part of finishing the lambda itself, so no need
            // to add this as a pending decl.
            typeExprPreprocessor.preprocess(lambda.getDeclaredType());
        }

        // Here we pre-initialize the lambda term's type, populated with
        // a mixture of fresh type variables and as much declared type
        // info as we have.
        // This provides the backing types for param bindings as their
        // types are queried during body traversal. Also, a declared
        // result type will be unified with the inferred result type.
        //
        assert lambda.getType() == null : "lambda type already set";

        final ScopeType seedType = (ScopeType)lambda.getSignatureType();

        final TypeInstantiator inst = new TypeInstantiator(seedType, this, true);

        pushInstanceVars(inst.getParamVars());

        lambda.setType(inst.getInstance());

        if (Session.isDebug())
            Session.debug(lambda.getLoc(), "seeded {0} : {1}",
                lambda.dump(), lambda.getType().dump());

        // add this before traversing lambda body, so that
        // vars in signature type will be ambient
        addPendingItem(lambda);

        super.visit(lambda);

        final Type type = lambda.getType();
        final Type resultType = Types.funResult(type);

        final UnboundTerm resultStatement = lambda.getResultStatement();
        final Type resultTermType = resultStatement.getType().instance(this, true);

        if (!unify(resultStatement.getLoc(), resultType, resultTermType))
            Session.error(resultStatement.getLoc(),
                "actual result type {0} is incompatible with expected result type {1}",
                errorFormat(resultTermType).dump(),
                errorFormat(resultType).dump());

        final Type lambdaType = type.subst(subs);

        lambda.setType(lambdaType);

        popInstanceVars();

        return lambdaType;
    }

    /**
     * TODO explain current version
     */
    @Override
    public Type visit(final ApplyTerm apply)
    {
        final Loc loc = apply.getLoc();
        final ApplyFlavor flav = apply.getFlav();
        final Term base = apply.getBase();
        final Term arg = apply.getArg();
        final Loc argLoc = arg.getLoc();

        // TODO remove eval once we're no longer sniffing here
        final Type baseType = visitTerm(base).subst(subs).deref().eval();
        final Type argType = visitTerm(arg);

        final Type resultType;

        switch (flav)
        {
            case FuncApp:
            {
                // base must be a function
                resultType = freshVar(loc);
                final Type targetBaseType = Types.fun(loc, argType, resultType);

                if (!unify(loc, baseType, targetBaseType))
                {
                    // TODO: when we see that failure is due to argument type,
                    // report memberwise mismatches when we can
                    Session.error(loc,
                        "cannot unify actual base type {0} with target base type {1}",
                        errorFormat(baseType).dump(),
                        errorFormat(targetBaseType).dump());
                }

                break;
            }

            case CollIndex:
            {
                // argument is a single index (position or key) item
                // base defaults to list, unless it is already known to be map

                if (Types.isMap(baseType))
                {
                    // base is known to be a map
                    resultType = freshVar(loc, Kinds.STAR);
                    final Type targetBaseType = Types.map(loc, argType, resultType);

                    if (!unify(loc, targetBaseType, baseType))
                    {
                        Session.error(loc,
                            "cannot unify actual map type {0} with target map type {1}",
                            errorFormat(baseType).dump(),
                            errorFormat(targetBaseType).dump());
                    }
                }
                else
                {
                    // not known, assume list
                    resultType = freshVar(loc, Kinds.STAR);
                    final Type targetBaseType = Types.list(loc, resultType);

                    if (!unify(loc, baseType, targetBaseType))
                    {
                        Session.error(loc,
                            "cannot unify actual base type {0} with target type {1}",
                            errorFormat(baseType).dump(),
                            errorFormat(targetBaseType).dump());
                    }

                    if (!unify(argLoc, Types.INT, argType))
                    {
                        Session.error(argLoc,
                            "index argument is of type {0}, must be of type Int",
                            errorFormat(argType).dump());
                    }
                }

                break;
            }

            case StructAddr:
            {
                // argument must be constant or deref to one
                // base defaults to tuple, unless it is already known to be record

                final Term argDeref = (arg instanceof RefTerm) ?
                    ((RefTerm)arg).deref() : arg;

                if (!argDeref.isConstant())
                {
                    Session.error(loc,
                        "structure addressing argument {0} is not constant",
                        arg.dump());

                    resultType = Types.unit();
                }
                else if (Types.isRec(baseType))
                {
                    // base type is known to be a record
                    final Type fields = Types.recFields(baseType).deref();

                    assert fields instanceof TypeMap;
                    final TypeMap fieldMap = (TypeMap)fields;

                    final Type keyType = fieldMap.getKeyType();
                    final Type keyBaseType = keyType instanceof ChoiceType ?
                        ((EnumType)keyType).getBaseType() : keyType;

                    if (unify(loc, keyBaseType, argType))
                    {
                        final Map<Term, Type> fieldTypes = fieldMap.getMembers();

                        if (fieldTypes.keySet().contains(argDeref))
                        {
                            resultType = fieldTypes.get(argDeref);
                        }
                        else
                        {
                            Session.error(arg.getLoc(), "invalid key {0} for record {1}",
                                argDeref.dump(), base.dump());

                            resultType = Types.unit();
                        }
                    }
                    else
                    {
                        Session.error(loc,
                            "record term has key type {0}, cannot be addressed with term of type {1}",
                            errorFormat(keyBaseType).dump(),
                            errorFormat(argType).dump());

                        resultType = Types.unit();
                    }
                }
                else if (Types.isTup(baseType))
                {
                    // base type is known to be a tuple

                    // TODO once we have polymorphic structs, this case folds into next one
                    // ...but until then, this is the only way to not infer a singleton tuple
                    // with only the current address.

                    if (!unify(loc, Types.INT, argType))
                    {
                        Session.error(argLoc,
                            "address argument is of type {0}, must be of type Int",
                            errorFormat(argType).dump());

                        resultType = Types.unit();
                    }
                    else
                    {
                        final Type members = Types.tupMembers(baseType).deref();

                        final List<Type> items = ((TypeList)members).getItems();

                        final int pos = ((IntLiteral)argDeref).getValue();

                        if (pos >= 0 && pos < items.size())
                        {
                            resultType = items.get(pos);
                        }
                        else
                        {
                            Session
                                .error(arg.getLoc(), "invalid position {0} for tuple {1}",
                                    argDeref.dump(), base.dump());

                            resultType = Types.unit();
                        }
                    }
                }
                else
                {
                    if (!unify(loc, Types.INT, argType))
                    {
                        Session.error(argLoc,
                            "address argument is of type {0}, must be of type Int",
                            errorFormat(argType).dump());

                        resultType = Types.unit();
                    }
                    else
                    {
                        // otherwise infer tuple base
                        resultType = freshVar(loc, Kinds.STAR);

                        final int pos = ((IntLiteral)argDeref).getValue();

                        final List<Type> targetMembers = Lists.newArrayList();

                        for (int i = 0; i < pos; i++)
                            targetMembers.add(freshVar(loc, Kinds.STAR));

                        targetMembers.add(resultType);

                        final Type targetBaseType = Types.tup(loc, targetMembers);

                        if (!unify(loc, targetBaseType, baseType))
                        {
                            Session.error(loc,
                                "cannot unify actual base type {0} with target type {1}",
                                errorFormat(baseType).dump(),
                                errorFormat(targetBaseType).dump());
                        }
                    }
                }

                break;
            }

            default:
            {
                resultType = Types.unit();
                assert false;
            }
        }

        final Type applyType = resultType.subst(subs);

        apply.setType(applyType);

        addPendingItem(apply);

        return applyType;
    }

    /**
     *
     */
    @Override
    public Type visit(final CoerceTerm coerce)
    {
        final Type coerceType = coerce.getType();

        // TODO remove once refs are used in dtor/ctor
        typeExprPreprocessor.preprocess(coerceType);
        addPendingItem(coerce);

        visitTerm(coerce.getTerm());

        return coerceType;
    }

    /**
     * Helper, holds typed items and typedefs
     * (both pending and deferred) from group.
     */
    private static final class GroupState
    {
        final Set<Typed> pendingItems = Sets.newIdentityHashSet();

        final Set<Typed> deferredItems = Sets.newIdentityHashSet();

        final Set<TypeDef> pendingDefs = Sets.newIdentityHashSet();

        final Set<TypeDef> deferredDefs = Sets.newIdentityHashSet();

        void addPendingItem(final Typed item)
        {
            pendingItems.add(item);
        }

        void addDeferredItem(final Typed item)
        {
            deferredItems.add(item);
        }

        void addPendingDef(final TypeDef def)
        {
            pendingDefs.add(def);
        }

        void addDeferredDef(final TypeDef def)
        {
            deferredDefs.add(def);
        }
    }
}
