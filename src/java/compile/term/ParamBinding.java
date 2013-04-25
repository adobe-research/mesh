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
import compile.type.ScopeType;
import compile.type.Type;
import compile.type.visit.TypeDumper;

/**
 * Parameter binding.
 * NOTE: non-final to support a hack in
 * {@link compile.gen.java.LambdaClassGenerator#buildApplyMethodDef},
 * which creates a local subclass that overrides {@link #getType} and
 * {@link #getDeclaredType} so they can create instances of this class
 * without a backing lambda.
 *
 * @author Basil Hosmer
 */
public class ParamBinding extends ValueBinding
{
    private LambdaTerm lambda;
    private final boolean inline;
    private int index;
    private final ParamValue value;

    public ParamBinding(final Loc loc, final String name,final Type declaredType,
        final boolean inline)
    {
        super(loc, name, declaredType);
        this.inline = inline;
        this.index = -1;
        this.value = new ParamValue(this);
    }

    public ParamBinding(final Loc loc, final String name, final Type declaredType)
    {
        this(loc, name, declaredType, false);
    }

    public boolean getInline()
    {
        return inline;
    }

    public void setIndex(final int index)
    {
        this.index = index;
    }

    // Binding

    public boolean isLet()
    {
        return false;
    }

    public LambdaTerm getScope()
    {
        return lambda;
    }

    public void setScope(final Scope scope)
    {
        assert scope instanceof LambdaTerm;
        this.lambda = (LambdaTerm)scope;
    }

    // ValueStatement
    
    public Term getValue()
    {
        return value;
    }

    // Typed

    public Type getType()
    {
        assert lambda != null : "lambda not set";

        if (lambda.getType() != null)
        {
            assert index >= 0 : "index not set";
            return lambda.getParamType(index);
        }
        else
        {
            return null;
        }
    }
    
    public void setType(final Type type)
    {
        throw new UnsupportedOperationException("ParamBinding.setType()");
    }

    /**
     * Note: param binding types begin as freestanding annotations,
     * but are merged into the declared type for the hosting lambda.
     * We use the presence of the original annotation to test for
     * existence, but delegate to the lambda for the actual type.
     */
    public Type getDeclaredType()
    {
        if (!hasDeclaredType())
            return null;

        if (lambda != null && lambda.getType() != null)
        {
            assert index >= 0 : "index not set";
            return lambda.getSignatureParamType(index);
        }
        else
        {
            return super.getDeclaredType();
        }
    }

    public void setDeclaredType(final Type type)
    {
        throw new UnsupportedOperationException("ParamBinding.setDeclaredType()");
    }
    
    // Statement

    public String dump()
    {
        final StringBuilder buf = new StringBuilder(name);

        if (hasDeclaredType())
        {
            final String dump = lambda != null ?
                TypeDumper.dumpWithScope(
                    getDeclaredType(), (ScopeType)lambda.getDeclaredType()) :
                    getDeclaredType().dump();

            buf.append(": ").append(dump);
        }

        return buf.toString();
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

        final ParamBinding that = (ParamBinding)o;

        if (hasDeclaredType() ? !declaredType.equals(that.declaredType) :
            that.hasDeclaredType()) return false;
        if (!name.equals(that.name)) return false;
        if (lambda != that.lambda) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + (hasDeclaredType() ? declaredType.hashCode() : 0);
        return result;
    }
}