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
package compile.term;

import compile.Loc;
import compile.module.Scope;
import compile.term.visit.BindingVisitor;
import compile.type.*;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.visit.*;

import java.util.Collections;

/**
 * A named type definition.
 *
 * @author Basil Hosmer
 */
public final class TypeDef extends TypeBinding
{
    /**
     * convenience--defines a nominal type whose payload is featureless.
     * Used in foreign function signatures.
     */
    public static TypeDef opaque(final String name)
    {
        final TypeDef def = new TypeDef(Loc.INTRINSIC, name, Types.newType(Types.OPAQUE));
        ((TypeApp)def.getValue()).setKind(Kinds.STAR);
        return def;
    }

    //
    //  instance
    //

    /**
     * def's rhs
     */
    private Type value;

    /**
     * scope in which the def occurs (module or lambda)
     */
    private Scope scope;

    /**
     * if nominal, compiler will generate constructor and destructor functions
     */
    private LetBinding ctorLet;
    private LetBinding dtorLet;

    /**
     * true if type is nominal (indicated by New() TC at top level)
     */
    private boolean nominal;

    public TypeDef(final Loc loc, final String name, final Type value)
    {
        super(loc, name);
        this.value = value;
        this.scope = null;
    }

    // Intrinsic typedef -- must call resolveIntrinsic() to initialize the value
    public TypeDef(final Loc loc, final String name)
    {
        super(loc, name);
        this.value = null;
        this.scope = null;
    }

    /**
     * set nominal properties, if our rhs is an application of the New TC
     */
    private void initNominal(final Scope scope)
    {
        if (!resolveNewTypeConstructor(value, scope))
            return;

        this.nominal = true;

        final LambdaTerm ctor = makeCtor();
        this.ctorLet = new LetBinding(this.loc, this.name, ctor.getDeclaredType(), ctor);

        final LambdaTerm dtor = makeDtor();
        this.dtorLet = new LetBinding(this.loc, "_" + this.name, dtor.getDeclaredType(), dtor);
    }

    /**
     * chicanery: since TypeDefs get built before name resolution,
     * we need to figure out that we're an application of the New type
     * constructor before by detecting an unresolved reference to it.
     * Note: the language needs to prohibit redefinition of (at least) New
     * to avoid inconsistent behavior.
     */
    private static boolean resolveNewTypeConstructor(final Type type, final Scope scope)
    {
        if (!(type instanceof TypeApp))
            return false;

        final Type base = ((TypeApp)type).getBase();

        if (!(base instanceof TypeRef))
            return false;

        final TypeRef baseRef = (TypeRef)base;

        if (baseRef.getBinding() != null)
            return Types.isNew(type);

        final TypeBinding binding = scope.getModule().findTypeBinding(baseRef.getName());

        if (binding instanceof TypeDef && ((TypeDef)binding).getValue() == Types.NEW)
        {
            baseRef.setBinding(binding);
            return true;
        }

        return false;
    }

    private LambdaTerm makeCtor()
    {
        // BUG shouldn't have to specify declared types on param and result.
        // problem has to do with sequencing of typedef and coerce term inferencing
        // TODO retry removing after TC refac

        final Type inType = Types.newRep(value);
        final Type outType = new TypeRef(loc, this);

        final ParamBinding param = new ParamBinding(loc, "val", inType); // null);
        final Term ref = new RefTerm(loc, "val");
        final Statement result = new UnboundTerm(new CoerceTerm(loc, ref, outType));

        return new LambdaTerm(loc, Collections.<TypeParam>emptyList(),
            Collections.singletonList(param), outType, // null,
            Collections.singletonList(result));
    }

    private LambdaTerm makeDtor()
    {
        // BUG shouldn't have to specify declared types on param and result.
        // problem has to do with sequencing of typedef and coerce term inferencing
        // TODO retry removing after TC refac

        final Type inType = new TypeRef(loc, this);
        final Type outType = Types.newRep(value);

        final ParamBinding param = new ParamBinding(loc, "val", inType); // null);
        final Term ref = new RefTerm(loc, "val");
        final Statement result = new UnboundTerm(new CoerceTerm(loc, ref, outType));

        return new LambdaTerm(loc, Collections.<TypeParam>emptyList(),
            Collections.singletonList(param), outType, // null,
            Collections.singletonList(result));
    }

    public TypeDef(final String name, final Type type)
    {
        this(Loc.INTRINSIC, name, type);
    }

    public Type getValue()
    {
        return value;
    }

    /**
     * Note: this is used *only* by the type checker to install
     * a checked/quantified version of the def's rvalue. Otherwise
     * this method should not be called.
     */
    public void setValue(final Type value)
    {
        this.value = value;
    }

    public boolean isNominal()
    {
        return nominal;
    }

    public LetBinding getCtorLet()
    {
        return ctorLet;
    }

    public LetBinding getDtorLet()
    {
        return dtorLet;
    }

    public boolean isResolved()
    {
        return value != null;
    }

    public boolean resolveIntrinsic()
    {
        assert !isResolved() : "already resolved";

        value = Types.findIntrinsic(name);
        return value != null;
    }

    // Type

    public Type deref()
    {
        return nominal ? this : value.deref();
    }

    public Kind getKind()
    {
        return value.getKind();
    }

    public SubstMap unify(final Loc loc, final Type other, final TypeEnv env)
    {
        final Type otherDeref = other.deref();

        return
            equals(otherDeref) ?
                SubstMap.EMPTY :
            !nominal ?
                value.unify(loc, otherDeref, env) :
            other instanceof TypeVar ?
                SubstMap.bindVar(loc, (TypeVar)otherDeref, this, env) :
            null;
    }

    public boolean equiv(final Type other, final EquivState state)
    {
        return nominal ? equals(other.deref()) :
            getValue().equiv(other, state);
    }

    public <T> T accept(final TypeVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Binding

    public Scope getScope()
    {
        return scope;
    }

    public void setScope(final Scope scope)
    {
        initNominal(scope); // can't do this until we have scope
        this.scope = scope;
    }

    public <T> T accept(final BindingVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    // Object

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final TypeDef typeDef = (TypeDef)o;

        if (!value.equals(typeDef.value)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
