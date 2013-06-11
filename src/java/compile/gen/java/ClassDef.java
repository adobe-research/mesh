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

import javassist.CtClass;
import compile.NameUtils;
import compile.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Parts for Java class def, Javassist CG methods,
 * ref to generated Class
 *
 * @author Basil Hosmer
 */
public final class ClassDef
{
    private final String name;
    private final List<String> interfaceNames;
    private final List<FieldDef> staticFieldDefs;
    private final List<FieldDef> instanceFieldDefs;
    private ConstructorDef constructorDef;
    private final List<MethodDef> methodDefs;

    // generated
    private CtClass ctClass;
    private Class<?> cls;

    /**
     * Create empty (named) class def.
     */
    public ClassDef(final String name)
    {
        this.name = name;
        this.interfaceNames = new ArrayList<String>();
        this.staticFieldDefs = new ArrayList<FieldDef>();
        this.instanceFieldDefs = new ArrayList<FieldDef>();
        this.methodDefs = new ArrayList<MethodDef>();
    }

    /**
     * Create empty named class def, with one interface name specified.
     */
    public ClassDef(final String name, final String interfaceName)
    {
        this(name);
        addInterfaceName(interfaceName);
    }

    /**
     * Create class def preinitialized with an already-built Java class. Used for intrinsics.
     */
    public ClassDef(final Class<?> cls)
    {
        this.name = cls.getName();
        this.interfaceNames = null;
        this.staticFieldDefs = null;
        this.instanceFieldDefs = null;
        this.methodDefs = null;
        this.cls = cls;
    }

    public String getName()
    {
        return name;
    }

    public List<String> getInterfaceNames()
    {
        return interfaceNames;
    }

    public void addInterfaceName(final String interfaceName)
    {
        interfaceNames.add(interfaceName);
    }

    public List<FieldDef> getStaticFieldDefs()
    {
        return staticFieldDefs;
    }

    public void addStaticFieldDef(final FieldDef fieldDef)
    {
        if (!fieldDef.getDeclaration().contains("static"))
        {
            throw new IllegalArgumentException(
                "static field declaration lacks 'static' modifier: " + fieldDef.getDeclaration());
        }

        staticFieldDefs.add(fieldDef);
    }

    public List<FieldDef> getInstanceFieldDefs()
    {
        return instanceFieldDefs;
    }

    public void addFieldDef(final FieldDef fieldDef)
    {
        instanceFieldDefs.add(fieldDef);
    }

    public ConstructorDef getConstructorDef()
    {
        return constructorDef;
    }

    public void setConstructorDef(final ConstructorDef constructorDef)
    {
        this.constructorDef = constructorDef;
    }

    public List<MethodDef> getMethodDefs()
    {
        return methodDefs;
    }

    public void addMethodDef(final MethodDef methodDef)
    {
        methodDefs.add(methodDef);
    }

    /**
     * Get the Javassist CtClass associated with this definition.
     * <p/>
     * TODO shouldn't hard linked here - can just be name - the only callers who need the actual object can look them up.
     */
    public CtClass getCtClass()
    {
        return ctClass;
    }

    public void setCtClass(final CtClass ctClass)
    {
        this.ctClass = ctClass;
    }

    /**
     * Get the compiled Java class associated with this definition.
     * <p/>
     * TODO shouldn't hard linked here - can just be name - the only callers who need the actual object can look them up.
     */
    public Class<?> getCls()
    {
        return cls;
    }

    public void setCls(final Class<?> cls)
    {
        this.cls = cls;
    }

    public boolean isExternal()
    {
        return interfaceNames == null;
    }

    /**
     * Java source. NOTE: not used by Javassist CG, which builds a class incrementally.
     */
    public String getSource()
    {
        if (isExternal())
            return "// external Class " + name + " = " + cls;

        final StringBuilder buf = new StringBuilder();
        
        buf.append("package ").append(NameUtils.qualifier(name)).append(";");
        buf.append("\n");

        buf.append("public final class ").append(NameUtils.unqualify(name));
        if (!interfaceNames.isEmpty())
            buf.append(" implements ").append(StringUtils.join(interfaceNames, ", "));
        buf.append("\n{");

        // static fields
        final List<String> staticFieldDecls = new ArrayList<String>();
        for (final FieldDef staticFieldDef : staticFieldDefs)
            staticFieldDecls.add(staticFieldDef.getSource(true));
        buf.append(StringUtils.joinAndDelimit(staticFieldDecls, "\n", "\n"));

        // instance fields
        final List<String> instanceFieldDecls = new ArrayList<String>();
        for (final FieldDef instanceFieldDef : instanceFieldDefs)
            instanceFieldDecls.add(instanceFieldDef.getSource(true));
        buf.append(StringUtils.joinAndDelimit(instanceFieldDecls, "\n", "\n"));

        // methods, including constructor
        final List<String> methodDecls = new ArrayList<String>();
        if (constructorDef != null)
            methodDecls.add(constructorDef.getSource(true));

        for (final MethodDef methodDef : methodDefs)
            methodDecls.add(methodDef.getSource(true));

        buf.append(StringUtils.joinAndDelimit(methodDecls, "\n\n", "\n", "\n"));
        buf.append("}");

        return buf.toString();
    }
}