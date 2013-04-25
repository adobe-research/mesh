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
package compile.module;

import compile.term.TypeDef;
import compile.term.LetBinding;

import java.util.*;

/**
 * A dictionary of qualified types and values available to a module
 *
 * @author Keith McGuigan
 */
public final class QualifiedSymbols
{
    final Map<String, Map<String,TypeDef> > types;
    final Map<String, Map<String,LetBinding> > values;

    public QualifiedSymbols() 
    {
        types = new HashMap<String, Map<String,TypeDef> >();
        values = new HashMap<String, Map<String,LetBinding> >();
    }

    private Map<String,TypeDef> getOrAddTypeNS(final String name)
    {
        Map<String,TypeDef> ns = types.get(name);
        if (ns == null)
        {
            ns = new HashMap<String,TypeDef>();
            types.put(name, ns);
        }
        return ns;
    }

    private Map<String,LetBinding> getOrAddValueNS(final String name)
    {
        Map<String,LetBinding> ns = values.get(name);
        if (ns == null)
        {
            ns = new HashMap<String,LetBinding>();
            values.put(name, ns);
        }
        return ns;
    }

    public Set<String> getNamespaces() 
    {
        final Set<String> ns = new HashSet<String>();
        ns.addAll(values.keySet());
        ns.addAll(types.keySet());
        return ns;
    }

    public Map<String,TypeDef> getTypes(final String namespace)
    {
        final Map<String,TypeDef> qtypes = types.get(namespace);
        return qtypes == null ?  Collections.<String,TypeDef>emptyMap() : qtypes;
    }

    public Map<String,LetBinding> getValues(final String namespace)
    {
        final Map<String,LetBinding> qvalues = values.get(namespace);
        return qvalues == null ?  Collections.<String,LetBinding>emptyMap() : qvalues;
    }

    public void putType(final String qualifier, final TypeDef type) 
    {
        final Map<String,TypeDef> qtypes = getOrAddTypeNS(qualifier);
        qtypes.put(type.getName(), type);
    }

    public void putAllTypes(final String qualifier, final Module source)
    {
        final Map<String,TypeDef> qtypes = getOrAddTypeNS(qualifier);
        for (final Map.Entry<String,TypeDef> entry : source.getTransitiveTypeDefs().entrySet())
            qtypes.put(entry.getKey(), entry.getValue());
    }

    public TypeDef getType(final String qualifier, final String name)
    {
        final Map<String,TypeDef> qtypes = types.get(qualifier);
        return qtypes == null ? null : qtypes.get(name);
    }

    public void putValue(final String qualifier, final LetBinding value) 
    {
        final Map<String,LetBinding> qvalues = getOrAddValueNS(qualifier);
        qvalues.put(value.getName(), value);
    }

    public void putAllValues(final String qualifier, final Module source)
    {
        final Map<String,LetBinding> qvalues = getOrAddValueNS(qualifier);
        for (final Map.Entry<String,LetBinding> entry : source.getTransitiveLets().entrySet())
            qvalues.put(entry.getKey(), entry.getValue());
    }

    public LetBinding getValue(final String qualifier, final String name)
    {
        final Map<String,LetBinding> qvalues = values.get(qualifier);
        return qvalues == null ? null : qvalues.get(name);
    }
}
