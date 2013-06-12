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

/**
 * Method {@link #generate} generates all necessary Java classes for a
 * given {@link JavaUnit}.
 *
 * @author Basil Hosmer
 */
public final class UnitClassGenerator
{
    private final JavaUnit unit;
    private final JavassistHelper javassist;

    public UnitClassGenerator(final JavaUnit unit)
    {
        this.unit = unit;
        this.javassist = new JavassistHelper();
    }

    /**
     * Generates Java classes (module representation and lambda implementations, currently)
     * for a given module unit. Currently, generated classes are attached directly to the
     * source {@link ClassDef}, but that should change.
     * <p/>
     * Nontrivial thing here is to split signature and implementation generation to avoid
     * circular refs in Javassist.
     *
     * @return
     */
    public boolean generate()
    {
        Session.pushErrorCount();

        final JavassistHelper javassist = new JavassistHelper();

        // first create empty classes as reference targets
        startClasses(unit.getLambdaClassDefs().values());
        javassist.startClass(unit.getModuleClassDef());

        // add class internal signatures
        addClassSignatures(unit.getLambdaClassDefs().values());
        javassist.addClassSignature(unit.getModuleClassDef());

        // now fill them in
        finishClasses(unit.getLambdaClassDefs().values());
        javassist.finishClass(unit.getModuleClassDef());

        return Session.popErrorCount() == 0;
    }

    /**
     * Establish CtClasses for the passed ClassDef collection.
     * See {@link JavassistHelper#startInterface}.
     * <p/>
     * ClassDefs carry internal reference to the generated CtClasses.
     */
    private boolean startClasses(final Iterable<ClassDef> classDefs)
    {
        boolean success = true;
        for (final ClassDef classDef : classDefs)
            if (classDef.getCls() == null)
                success &= javassist.startClass(classDef) != null;
        return success;
    }

    /**
     * Add signatures to the CtClasses for the passed ClassDef collection.
     * See {@link JavassistHelper#addClassSignature(ClassDef)}.
     */
    private boolean addClassSignatures(final Iterable<ClassDef> classDefs)
    {
        boolean success = true;
        for (final ClassDef classDef : classDefs)
            if (classDef.getCls() == null)
                success &= javassist.addClassSignature(classDef) != null;
        return success;
    }

    /**
     * Add implementations to the CtClasses for the passed ClassDef collection, and
     * generate Java class objects.
     * See {@link JavassistHelper#finishClass}.
     * <p/>
     * ClassDefs carry internal reference to the generated class objects.
     */
    private boolean finishClasses(final Iterable<ClassDef> classDefs)
    {
        boolean success = true;
        for (final ClassDef classDef : classDefs)
            if (classDef.getCls() == null)
                success &= javassist.finishClass(classDef) != null;
        return success;
    }
}