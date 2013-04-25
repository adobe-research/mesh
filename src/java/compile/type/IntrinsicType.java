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
package compile.type;

/**
 * Superclass for intrinsic types
 *
 * @author Keith McGuigan
 */
public class IntrinsicType
{
    /**
     * Name of the static field that each subclass must contain
     */
    public static final String INSTANCE_FIELD_NAME = "INSTANCE";

    private final String name;
    private final Type type;

    protected IntrinsicType(final String name, final Type type)
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public Type getType()
    {
        return type;
    }
}
