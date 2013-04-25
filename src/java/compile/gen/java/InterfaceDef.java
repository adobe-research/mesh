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

import compile.StringUtils;
import javassist.CtClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Parts for Java interface def, Javassist CG methods, ref to generated Class
 *
 * @author Basil Hosmer
 */
public final class InterfaceDef
{
    private final String name;
    private final List<String> superNames;
    private final List<String> methodPrototypes;

    // generated
    private CtClass ctInterface;
    private Class<?> cls;

    /**
     * standard constructor, named and empty
     *
     * @param name
     */
    public InterfaceDef(final String name)
    {
        this.name = name;
        this.superNames = new ArrayList<String>();
        this.methodPrototypes = new ArrayList<String>();
    }

    /**
     * Create interface def preinitialized with an already-built Java class. Used for intrinsics.
     *
     * @param name
     * @param cls
     */
    public InterfaceDef(final String name, final Class<?> cls)
    {
        this(name);
        this.cls = cls;
    }

    public String getName()
    {
        return name;
    }

    public List<String> getSuperNames()
    {
        return superNames;
    }

    public List<String> getMethodPrototypes()
    {
        return methodPrototypes;
    }

    public CtClass getCtInterface()
    {
        return ctInterface;
    }

    public void setCtInterface(final CtClass ctInterface)
    {
        this.ctInterface = ctInterface;
    }

    public Class<?> getCls()
    {
        return cls;
    }

    public void setCls(final Class<?> cls)
    {
        this.cls = cls;
    }

    /**
     * Java source. NOTE: not used by Javassist CG.
     *
     * @return
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public String getSource(final boolean debug)
    {
        final StringBuilder buf = new StringBuilder().
            append("public interface ").
            append(name);

        if (!superNames.isEmpty())
        {
            buf.append(" extends ").
                append(StringUtils.join(superNames, ", "));
        }

        buf.append("\n{");

        final List<String> methods = new ArrayList<String>();

        for (final String methodPrototype : methodPrototypes)
        {
            methods.add("\n\t" + methodPrototype + ";");
        }

        buf.append(StringUtils.join(methods, "\n"));

        buf.append("\n}");

        return buf.toString();
    }
}
