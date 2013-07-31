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
package compile.type.visit;

import compile.Loc;
import compile.Session;
import compile.term.Term;
import compile.type.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Performs reduction on type terms.
 * TODO system is getting a little tippy here with experiments, trim back.
 *
 * @author Basil Hosmer
 */
public final class TypeReducer extends TypeTransformerBase
{
    private static TypeReducer INSTANCE = new TypeReducer();

    public static Type reduce(final Type type)
    {
        return INSTANCE.transform(type);
    }

    // TypeTransformerBase

    /**
     * Here we just transfer all params from the original onto
     * the transformed result, effectively destroying the
     * original.
     */
    @Override
    protected void fixupParams(final Type original, final Type result)
    {
        for (final TypeParam param : original.getParams().values())
            result.addParam(param);
    }

    // TypeVisitor

    /**
     * note: using a lower-level access idiom here. ultimately
     * should choose between this and the higher-level one used
     * elsewhere
     */
    @Override
    public Type visit(final TypeApp app)
    {
        final Loc loc = app.getLoc();
        final Type base = visitType(app.getBase());
        final Type arg = visitType(app.getArg());

        if (base == Types.TMAP)
        {
            if (arg instanceof TypeTuple)
            {
                final List<Type> args = ((TypeTuple)arg).getMembers();
                final Type tlist = args.get(0);
                final Type tcon = args.get(1);

                if (tlist instanceof TypeList)
                {
                    final TypeList list = (TypeList)tlist;

                    final List<Type> results = new ArrayList<Type>();

                    for (final Type item : list.getItems())
                        results.add(Types.app(loc, tcon, item));

                    return new TypeList(loc, results);
                }
            }
        }
        else if (base == Types.ASSOC)
        {
            if (arg instanceof TypeTuple)
            {
                final List<Type> args = ((TypeTuple)arg).getMembers();
                final Type keyType = args.get(0);
                final Type valTypes = args.get(1);

                if (keyType instanceof EnumType && valTypes instanceof TypeList)
                {
                    final EnumType keyEnum = (EnumType)keyType;

                    final List<Type> valTypeList = ((TypeList)valTypes).getItems();

                    if (keyEnum.getSize() != valTypeList.size())
                    {
                        Session.error(loc,
                            "Assoc: size mismatch between type map key enum {0}, val type list {1}",
                            keyType.dump(), valTypes.dump());

                        return null;
                    }

                    final LinkedHashMap<Term, Type> members = new LinkedHashMap<Term, Type>();

                    final Iterator<Type> valTypeIter = valTypeList.iterator();

                    for (final Term keyTerm : keyEnum.getValues())
                    {
                        final Type valType = valTypeIter.next();
                        members.put(keyTerm, valType);
                    }

                    return new TypeMap(loc, keyEnum, members);
                }
            }
        }
        else if (base == Types.CONE)
        {
            if (arg instanceof TypeTuple)
            {
                final List<Type> args = ((TypeTuple)arg).getMembers();
                final Type domTypes = args.get(0);
                final Type codType = args.get(1);

                if (domTypes instanceof TypeList)
                {
                    final List<Type> domTypeList = ((TypeList)domTypes).getItems();

                    final ArrayList<Type> items = new ArrayList<Type>();

                    for (final Type domType : domTypeList)
                        items.add(Types.fun(domType.getLoc(), domType, codType));

                    return new TypeList(loc, items);
                }
            }
        }

        return base == app.getBase() && arg == app.getArg() ?
            app : Types.app(loc, base, arg);
    }
}