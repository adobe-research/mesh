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
package runtime.intrinsic.demo;

import compile.Loc;
import compile.analyze.KindChecker;
import compile.term.SymbolLiteral;
import compile.term.Term;
import compile.type.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 */
public final class XNode
{
    public final static Type INSTANCE = initType();

    /**
     * XNode - structural type for XML node representation.
     * This one differs from the others here because (a) it's a structural
     * type, not an opaque type, (b) it has a self-reference, which makes
     * the Java that hacks it together more complicated, and (c) its key
     * names are exposed for use by {@link runtime.intrinsic.demo._parsexml}.
     * TODO this should be a source-level type def, used by a source-level
     * prototype of ParseXml (but will still need to be accessible from the
     * XNode-building implementation code).
     */
    public static final String XNODE_NAME = "name";
    public static final String XNODE_ATTRS = "attrs";
    public static final String XNODE_ELEMS = "elems";

    private static Type initType()
    {
        final SymbolLiteral nameKey = new SymbolLiteral(Loc.INTRINSIC, XNODE_NAME);
        final SymbolLiteral attrsKey = new SymbolLiteral(Loc.INTRINSIC, XNODE_ATTRS);
        final SymbolLiteral elemsKey = new SymbolLiteral(Loc.INTRINSIC, XNODE_ELEMS);

        final Set<Term> keySet = new LinkedHashSet<Term>();
        keySet.add(nameKey);
        keySet.add(attrsKey);
        keySet.add(elemsKey);

        final ChoiceType
            keyEnum = new ChoiceType(Loc.INTRINSIC, Types.SYMBOL, keySet);

        final Type attrsType = Types.map(Types.SYMBOL, Types.STRING);

        final TypeRef xnodeTypeRef = new TypeRef(Loc.INTRINSIC,
            XNode.class.getSimpleName());

        final Type elemsType = Types.list(xnodeTypeRef);

        final LinkedHashMap<Term, Type> fieldMap = new LinkedHashMap<Term, Type>();
        fieldMap.put(nameKey, Types.STRING);
        fieldMap.put(attrsKey, attrsType);
        fieldMap.put(elemsKey, elemsType);

        final TypeApp xnodeType = Types.rec(new TypeMap(Loc.INTRINSIC, fieldMap));

        xnodeType.collectInlineParams();

        KindChecker.check(xnodeType);

        return xnodeType;
    }
}
