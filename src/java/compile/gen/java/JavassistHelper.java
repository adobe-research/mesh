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

import compile.Session;
import javassist.*;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Helpers for multipass Javassist code generation.
 *
 * @author Basil Hosmer
 */
public final class JavassistHelper
{
    private static ClassPool classPool;

    static
    {
        resetClassPool();
    }

    /**
     * called as part of shell's clear command
     */
    public static void resetClassPool()
    {
        classPool = new ClassPool(null);
        classPool.appendSystemPath();
    }

    public JavassistHelper()
    {
    }

    /**
     * Create a CtClass for this interface definition, add to passed classPool.
     * Has supers specified but is otherwise empty.
     * Must later call {@link #finishInterface} to get an actual Class.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public CtClass startInterface(final InterfaceDef interfaceDef)
    {
        try
        {
            // make new ctclass
            final CtClass ctInterface = classPool.makeInterface(interfaceDef.getName());
            setInterfaces(ctInterface, interfaceDef.getSuperNames());
            interfaceDef.setCtInterface(ctInterface);
            return ctInterface;
        }
        catch (Exception e)
        {
            Session.error(
                "exception creating CtClass for Java interface {0}: {1} \"{2}\"",
                    interfaceDef.getName(), e.getClass().getName(),
                    e.getLocalizedMessage());

            e.printStackTrace();
        }

        return null;
    }

    /**
     * Set interfaces on a ctclass.
     */
    private void setInterfaces(final CtClass ctClass, final List<String> superNames)
        throws NotFoundException
    {
        final int numSupers = superNames.size();
        final CtClass[] ctInterfaces = new CtClass[numSupers];

        for (int i = 0; i < numSupers; i++)
            ctInterfaces[i] = classPool.getCtClass(superNames.get(i));

        ctClass.setInterfaces(ctInterfaces);
    }

    /**
     * Finish the Java interface for this definition.
     * Add method prototypes and convert from CtClass to Class.
     * Class is stored in cls member and returned.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public Class<?> finishInterface(final InterfaceDef interfaceDef)
    {
        try
        {
            final CtClass ctInterface = interfaceDef.getCtInterface();

            // add method prototypes
            for (final String methodPrototype : interfaceDef.getMethodPrototypes())
                ctInterface.addMethod(
                    CtNewMethod.make(methodPrototype + ";", ctInterface));

            // create Class
            interfaceDef.setCls(ctInterface.toClass());
        }
        catch (Exception e)
        {
            Session.error("exception finishing Java interface {0}: {1} \"{2}\"",
                interfaceDef.getName(), e.getClass().getName(),
                e.getLocalizedMessage());

            e.printStackTrace();
        }

        return interfaceDef.getCls();
    }

    /**
     * Create a CtClass for this class definition, add to passed classPool.
     * Class is empty except for implementing interfaces. Must call
     * {@link #addClassSignature}, then {@link #finishClass} to get an actual Class.
     */
    public CtClass startClass(final ClassDef classDef)
    {
        try
        {
            // make new ctclass
            final CtClass ctClass = classPool.makeClass(classDef.getName());
            ctClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
            setInterfaces(ctClass, classDef.getInterfaceNames());
            classDef.setCtClass(ctClass);
            return ctClass;
        }
        catch (Exception e)
        {
            Session.error("exception creating CtClass for Java class {0}: {1} \"{2}\"",
                classDef.getName(), e.getClass().getName(), e.getLocalizedMessage());

            e.printStackTrace();
        }

        return null;
    }

    /**
     * Add class signature.
     */
    public CtClass addClassSignature(final ClassDef classDef)
    {
        try
        {
            final CtClass ctClass = classDef.getCtClass();
            addUninitializedFields(classDef.getStaticFieldDefs(), ctClass);
            addUninitializedFields(classDef.getInstanceFieldDefs(), ctClass);
            addEmptyConstructor(classDef, ctClass);
            addEmptyClassMethods(classDef.getMethodDefs(), ctClass);
            return ctClass;
        }
        catch (Exception e)
        {
            Session.error("exception finishing Java class {0}: {1} \"{2}\"",
                classDef.getName(), e.getClass().getName(), e.getLocalizedMessage());

            e.printStackTrace();
        }

        return null;
    }

    /**
     * Add uninitialized field declarations to a ctclass.
     * May be the actual decls, or uninitialized versions.
     */
    private void addUninitializedFields(final List<FieldDef> fieldDefs,
        final CtClass ctClass)
        throws CannotCompileException
    {
        for (final FieldDef fieldDef : fieldDefs)
        {
            final CtField ctField =
                CtField.make(fieldDef.getUninitializedSource(), ctClass);

            fieldDef.setCtField(ctField);
            ctClass.addField(ctField);
        }
    }

    /**
     * Add empty constructor to a ctclass. May be the actual ctor, or an emptied version.
     */
    private void addEmptyConstructor(final ClassDef classDef, final CtClass ctClass)
        throws CannotCompileException
    {
        final ConstructorDef ctorDef = classDef.getConstructorDef();
        if (ctorDef != null)
        {
            final CtConstructor ctConstructor =
                CtNewConstructor.make(ctorDef.getEmptySource(), ctClass);

            ctorDef.setCtConstructor(ctConstructor);
            ctClass.addConstructor(ctConstructor);
        }
    }

    /**
     * Add empty methods to a ctclass. May be the actual methods, or emptied versions.
     */
    private void addEmptyClassMethods(final List<MethodDef> methodDefs,
        final CtClass ctClass)
        throws CannotCompileException
    {
        for (final MethodDef methodDef : methodDefs)
        {
            final CtMethod ctMethod =
                CtNewMethod.make(methodDef.getEmptySource(), ctClass);

            methodDef.setCtMethod(ctMethod);
            ctClass.addMethod(ctMethod);
        }
    }

    /**
     * Finish the Java class for this definition.
     * Add method prototypes and convert from CtClass to Class.
     * Class is stored in cls member and returned.
     */
    public Class<?> finishClass(final ClassDef classDef)
    {
        try
        {
            final CtClass ctClass = classDef.getCtClass();

            // this clears Modifier.ABSTRACT, which was set
            // as a side-effect in addClassSignature()
            ctClass.setModifiers(Modifier.PUBLIC | Modifier.FINAL);

            addInitializedFields(classDef.getStaticFieldDefs(), ctClass);
            addInitializedFields(classDef.getInstanceFieldDefs(), ctClass);
            addFullConstructor(classDef, ctClass);
            addFullClassMethods(classDef.getMethodDefs(), ctClass);

            // create class
            classDef.setCls(ctClass.toClass());
        }
        catch (Exception e)
        {
            Session.error("exception finishing Java class {0}: {1} \"{2}\"",
                classDef.getName(), e.getClass().getName(),
                e.getLocalizedMessage());

            e.printStackTrace();
        }

        return classDef.getCls();
    }

    /**
     * Ensures that full versions of field defs are present in ctclass.
     */
    private void addInitializedFields(
        final List<FieldDef> fieldDefs, final CtClass ctClass)
        throws CannotCompileException, NotFoundException
    {
        for (final FieldDef fieldDef : fieldDefs)
        {
            if (fieldDef.isInitialized())
            {
                // remove empty version
                ctClass.removeField(fieldDef.getCtField());

                // add initialized version
                final CtField ctField =
                    CtField.make(fieldDef.getSource(false), ctClass);

                fieldDef.setCtField(ctField);
                ctClass.addField(ctField);
            }
        }
    }

    /**
     * Ensure that full version of constructor is present in the class.
     */
    private void addFullConstructor(final ClassDef classDef, final CtClass ctClass)
        throws CannotCompileException, NotFoundException
    {
        final ConstructorDef ctorDef = classDef.getConstructorDef();

        if (ctorDef != null)
        {
            if (!ctorDef.isEmpty())
            {
                // remove empty version
                ctClass.removeConstructor(ctorDef.getCtConstructor());

                // add full version
                final CtConstructor ctConstructor =
                    CtNewConstructor.make(ctorDef.getSource(false), ctClass);

                ctorDef.setCtConstructor(ctConstructor);
                ctClass.addConstructor(ctConstructor);
            }
        }
    }

    /**
     * Ensure that full versions of method defs are present in the class.
     */
    private void addFullClassMethods(
        final List<MethodDef> methodDefs, final CtClass ctClass)
        throws CannotCompileException, NotFoundException
    {
        for (final MethodDef methodDef : methodDefs)
        {
            // remove empty version
            ctClass.removeMethod(methodDef.getCtMethod());

            // add full version
            final CtMethod ctMethod =
                CtNewMethod.make(methodDef.getSource(false), ctClass);

            methodDef.setCtMethod(ctMethod);
            ctClass.addMethod(ctMethod);
        }
    }
}
