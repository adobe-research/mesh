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

import compile.term.Term;
import compile.term.TypeDef;
import compile.type.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Traverses a type and produces a new type if any part
 * of the traversal produces a new subterm. Unchanged
 * subterrms are shared between original and new types.
 * Base implementation only implements traversal, with
 * identity transformations at the leaves.
 * <p/>
 * NOTE: this base class does *not* install type params
 * on a transformed result. This is because our hard param
 * refs combined with the structure sharing that happens
 * here would require an expensive strategy to guarantee
 * integrity in the general case, and different transformers
 * can take advantage of the particular things they're doing
 * to avoid the expense in many cases. BUT this means that
 * every subclass is responsible for ensuring the integrity
 * of transformed results w.r.t. params hosted on scopes,
 * and param refs within those scopes.
 *
 * @author Basil Hosmer
 */
public abstract class TypeTransformerBase extends TypeVisitorBase<Type>
{
    /**
     * see header comment.
     */
    protected abstract void fixupParams(final Type original, final Type result);

    /**
     * root entry point for subs.
     */
    protected Type transform(final Type type)
    {
        final Type transformed = visitType(type);

        if (transformed != type)
            fixupParams(type, transformed);

        return transformed;
    }

    //
    // TypeVisitor
    //

    @Override
    public Type visit(final WildcardType wildcard)
    {
        return wildcard;
    }

    @Override
    public Type visit(final EnumType enumType)
    {
        final Type baseType = enumType.getBaseType();

        if (baseType == null)
            assert false : "null base type in enum";

        final Type newBaseType = visitType(baseType);

        return baseType == newBaseType ? enumType :
            new EnumType(enumType.getLoc(), newBaseType, enumType.getValues());
    }

    @Override
    public Type visit(final TypeVar var)
    {
        return var;
    }

    /**
     * NOTE: subclasses must ensure that refs track any
     * replacement that occurs during a traversal.
     */
    @Override
    public Type visit(final TypeRef ref)
    {
        return ref;
    }

    @Override
    public Type visit(final TypeDef def)
    {
        final Type value = def.getValue();
        final Type newValue = visitType(value);

        return newValue == value ? def :
            new TypeDef(def.getLoc(), def.getName(), newValue);
    }

    @Override
    public Type visit(final TypeParam param)
    {
        return param;
    }

    // -------------------------------------------------------------

    @Override
    public Type visit(final TypeCons cons)
    {
        final Type body = cons.getBody();

        if (body == null)
            return cons;

        final Type newBody = visitType(body);

        return newBody == body ? cons :
            new TypeCons(cons.getLoc(), cons.getName(), cons.getKind(), newBody);
    }

    @Override
    public Type visit(final TypeApp app)
    {
        final Type base = app.getBase();
        final Type newBase = visitType(base);

        final Type arg = app.getArg();
        final Type newArg = visitType(arg);

        return newBase == base && newArg == arg ? app :
            new TypeApp(app.getLoc(), newBase, newArg, app.getKind());
    }

    @Override
    public Type visit(final TypeTuple tuple)
    {
        final List<Type> members = tuple.getMembers();
        final List<Type> newMembers = transformTypeList(members);

        return newMembers == null ? tuple :
            new TypeTuple(tuple.getLoc(), newMembers, tuple.getKind());
    }

    /**
     * Transform a list of types. Return null
     * if no types change during transformation.
     */
    private ArrayList<Type> transformTypeList(final List<Type> items)
    {
        ArrayList<Type> newItems = null;

        final int size = items.size();
        for (int i = 0; i < size; i++)
        {
            final Type item = items.get(i);
            final Type newItem = visitType(item);

            if (item != newItem)
            {
                if (newItems == null)
                    newItems = new ArrayList<Type>(items);

                newItems.set(i, newItem);
            }
        }

        return newItems;
    }

    @Override
    public Type visit(final TypeList list)
    {
        final List<Type> items = list.getItems();
        final List<Type> newItems = transformTypeList(items);

        return newItems == null ? list :
            new TypeList(list.getLoc(), newItems);
    }

    @Override
    public Type visit(final TypeMap map)
    {
        final EnumType keyType = map.getKeyType();
        final EnumType newKeyType = (EnumType)visitType(keyType);

        final Map<Term, Type> members = map.getMembers();
        final Map<Term, Type> newMembers = transformTypeMap(members);

        return newKeyType == keyType && newMembers == members ? map :
            new TypeMap(map.getLoc(), newKeyType, newMembers);
    }

    /**
     * helper--transform a map from terms to types. Original map is returned
     * if no types change during transformation.
     */
    protected Map<Term, Type> transformTypeMap(final Map<Term, Type> map)
    {
        Map<Term, Type> newMap = null;

        for (final Map.Entry<Term, Type> entry : map.entrySet())
        {
            final Type value = entry.getValue();
            final Type newValue = visitType(value);

            if (newValue != value)
            {
                if (newMap == null)
                    newMap = new LinkedHashMap<Term, Type>(map);

                newMap.put(entry.getKey(), newValue);
            }
        }

        return newMap == null ? map : newMap;
    }
}
