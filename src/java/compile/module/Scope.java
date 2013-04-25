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
package compile.module;

import compile.Dumpable;
import compile.Located;
import compile.term.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Binding scope, either a {@link compile.module.Module} or
 * {@link runtime.rep.lambda.Lambda}.
 *
 * @author Basil Hosmer
 */
public interface Scope extends Located, Dumpable
{
    /**
     * List of scope's body statements. These are all scope statements as parsed
     * when scope is constructed. Analysis then makes sure anything nonexecutable
     * (e.g. {@link TypeDef}s) gets removed, and anything generated gets added.
     * Body statements are also referenced from elsewhere, e.g. the scope's
     * {@link LetBinding}s are available by name from {@link #getLets()}.
     */
    List<Statement> getBody();

    /**
     * Get parent scope. For modules this is null, for lambdas
     * it's a module or another lambda.
     */
    Scope getParentScope();

    /**
     * Set parent scope.
     */
    void setParentScope(Scope parentScope);

    /**
     * Get our root module scope, which may be ourselves.
     */
    Module getModule();

    /**
     * true if we're a {@link LambdaTerm} as opposed to a {@link Module}.
     */
    boolean isLambda();

    // value bindings

    /**
     * Find a value binding by name, either locally or in enclosing bindings.
     */
    ValueBinding findValueBinding(String name);

    /**
     * Look up a value binding defined in the current scope by name.
     * <strong>Note that in module scopes, this includes imported
     * modules.</strong> So it would be great to find a better adjective
     * than "local".
     */
    ValueBinding getValueBinding(String name);

    /**
     * Ordered map of scope's (value) param bindings.
     * Implementations must guarantee ordering.
     */
    Map<String, ParamBinding> getParams();

    /**
     * Ordered map of scope's let bindings.
     */
    LinkedHashMap<String, LetBinding> getLets();

    /**
     * Add or replace a local let by name.
     * If this value binding would shadow a <strong>local</strong>
     * param binding, an error is thrown.
     */
    void addLet(LetBinding let);

    // type bindings

    /**
     * Find a type binding by name, either locally or in enclosing bindings.
     */
    TypeBinding findType(String name);

    /**
     * Map of scope's local type defs.
     */
    Map<String, TypeDef> getTypeDefs();

    /**
     * get a locally defined type def
     */
    TypeBinding getTypeDef(String name);
    
    /**
     * Add a local type def
     */
    void addTypeDef(TypeDef typeDef);

    /**
     * Register a dependency from a term to a binding
     */
    void addDependency(Statement statement, Binding binding);

    /**
     * Get sublists of body statements in dependency order.
     */
    List<List<Statement>> getDependencyGroups();
}
