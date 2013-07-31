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
package compile.type.kind;

import compile.Loc;

/**
 * Predefined kinds. Experimental stuff at bottom.
 *
 * @author Basil Hosmer
 */
public class Kinds
{
    public static Kind STAR = new Kind(Loc.INTRINSIC)
    {
        public String dump()
        {
            return "*";
        }
    };

    public static Kind STAR_LIST = new Kind(Loc.INTRINSIC)
    {
        public String dump()
        {
            return "[*]";
        }
    };

    public static Kind STAR_MAP = new Kind(Loc.INTRINSIC)
    {
        public String dump()
        {
            return "[:*]";
        }
    };

    public static TupleKind STAR_PAIR = new TupleKind(STAR, STAR);

    public static ArrowKind UNARY_CONS = new ArrowKind(STAR, STAR);

    public static ArrowKind BINARY_CONS = new ArrowKind(STAR_PAIR, STAR);

    public static ArrowKind LIST_CONS = new ArrowKind(STAR_LIST, STAR);

    public static ArrowKind MAP_CONS = new ArrowKind(STAR_MAP, STAR);

    public static ArrowKind TYPE_MAP_KIND =
        new ArrowKind(new TupleKind(STAR_LIST, UNARY_CONS), STAR_LIST);

    // index enum for list: [X, Y, Z] => enum(Int, [0, 1, 2])
    public static ArrowKind TYPE_INDEX_KIND = new ArrowKind(STAR_LIST, STAR);

    // two type lists in, one out: [A, B, C] + [X, Y, Z] => [A, B, C, X, Y, Z]
    public static ArrowKind BINARY_TYPELIST_FUNC =
        new ArrowKind(new TupleKind(STAR_LIST, STAR_LIST), STAR_LIST);

    // associates *values* of a key type with a list of value *types*.
    // TODO what we want really is an enum kind for the first argument.
    public static ArrowKind TYPE_ASSOC_KIND =
        new ArrowKind(new TupleKind(STAR, STAR_LIST), STAR_MAP);

    // type-level exponentiation produces value-uniform tuples or records.
    // left operand is the value type. right operand is the key/index enum.
    // e.g. Int ^ 3 is int triple. (type level int constant N denotes {0..n-1}
    // e.g. String ^ {#first, #last} => (#first: String, #last: String)
    public static ArrowKind TYPE_EXP_KIND = new ArrowKind(STAR_PAIR, STAR);

    // specialized type list constructor: given a list of domains and
    // a single codomain, return a list of functions, one for each
    // domain and all to the shared codomain
    //
    public static ArrowKind CONE_KIND =
        new ArrowKind(new TupleKind(STAR_LIST, STAR), STAR_LIST);

}
