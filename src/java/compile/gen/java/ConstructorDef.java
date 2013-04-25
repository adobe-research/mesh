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

import javassist.CtConstructor;

/**
 * Parts for a Java constructor method.
 *
 * @author Basil Hosmer
 */
public final class ConstructorDef extends MethodDef
{
    public ConstructorDef(final String sig)
    {
        super(sig);
    }

    public CtConstructor getCtConstructor()
    {
        return (CtConstructor)ctBehavior;
    }

    public void setCtConstructor(final CtConstructor ctConstructor)
    {
        this.ctBehavior = ctConstructor;
    }
}