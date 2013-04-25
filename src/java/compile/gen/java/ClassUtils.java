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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Basil Hosmer
 */
class ClassUtils
{
    private static BiMap<Class<?>, Class<?>> primToObj = HashBiMap.create();

    static
    {
        primToObj.put(boolean.class, Boolean.class);
        primToObj.put(char.class, Character.class);
        primToObj.put(byte.class, Byte.class);
        primToObj.put(short.class, Short.class);
        primToObj.put(int.class, Integer.class);
        primToObj.put(long.class, Long.class);
        primToObj.put(float.class, Float.class);
        primToObj.put(double.class, Double.class);
    }

    private static BiMap<Class<?>, Class<?>> objToPrim = primToObj.inverse();

    /**
     * true if c is boxed primitive
     */
    public static boolean isBoxedPrimitive(final Class<?> c)
    {
        return objToPrim.containsKey(c);
    }

    /**
     * return boxed equivalent of c, or c iif c is not a primitive class
     */
    public static Class<?> boxed(final Class<?> c)
    {
        return c.isPrimitive() ? primToObj.get(c) : c;
    }

    /**
     * return unboxed equivalent of c, or c if c is not a boxed primitive class
     */
    public static Class<?> unboxed(final Class<?> c)
    {
        return isBoxedPrimitive(c) ? objToPrim.get(c) : c;
    }

    /**
     * given expr (in text) and source type, add unboxing to text
     * if c is a boxed primitive class, otherwise return original.
     */
    public static String unbox(final String expr, final Class<?> c)
    {
        return
            c == Boolean.class ? "(" + expr + ").booleanValue()" :
            c == Character.class ? "(" + expr + ").charValue()" :
            c == Byte.class ? "(" + expr + ").byteValue()" :
            c == Short.class ? "(" + expr + ").shortValue()" :
            c == Integer.class ? "(" + expr + ").intValue()" :
            c == Long.class ? "(" + expr + ").longValue()" :
            c == Float.class ? "(" + expr + ").floatValue()" :
            c == Double.class ? "(" + expr + ").doubleValue()" :
            expr;
    }

    /**
     * given expr (in text) and source type, add boxing to text
     * if c is a primitive class, otherwise return original
     */
    public static String box(final String expr, final Class<?> c)
    {
        return
            c == boolean.class ? "Boolean.valueOf(" + expr + ")" :
            c == char.class ? "Character.valueOf(" + expr + ")" :
            c == byte.class ? "Byte.valueOf(" + expr + ")" :
            c == short.class ? "Short.valueOf(" + expr + ")" :
            c == int.class ? "Integer.valueOf(" + expr + ")" :
            c == long.class ? "Long.valueOf(" + expr + ")" :
            c == float.class ? "Float.valueOf(" + expr + ")" :
            c == double.class ? "Double.valueOf(" + expr + ")" :
            expr;
    }

    private final static Map<Class<?>, Integer> NUM_CAST_PREC =
        new HashMap<Class<?>, Integer>();

    static
    {
        NUM_CAST_PREC.put(int.class, 0);
        NUM_CAST_PREC.put(long.class, 1);
        NUM_CAST_PREC.put(float.class, 2);
        NUM_CAST_PREC.put(double.class, 3);
    }

    /**
     *
     */
    public static boolean canCastFromPrimToPrim(
        final Class<?> lclass, final Class<?> rclass)
    {
        final Integer lprec = NUM_CAST_PREC.get(lclass);
        final Integer rprec = NUM_CAST_PREC.get(rclass);

        return lprec != null && rprec != null && rprec <= lprec;
    }
}
