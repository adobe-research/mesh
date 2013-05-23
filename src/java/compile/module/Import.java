
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

import compile.term.*;

/**
 * An imported is a wrapper around a module which adds restrictions 
 * and/or qualifications to symbols that are visible.
 *
 * @author Keith McGuigan
 */
public class Import
{
    private final Module module; // module that the import is from
    private final String qualifier;
    private final WhiteList whitelist;

    public Import(final Module module, final String qualifier,
                  final WhiteList whitelist)
    {
        this.module = module;
        this.qualifier = qualifier;
        this.whitelist = whitelist;
    }

    public Module getModule() { return module; }
    public String getQualifier() { return qualifier; }

    private String stripQualifier(final String qname) 
    {
        return qname.substring(qualifier.length() + 1);
    }

    private boolean qualifierMatch(final String qname) 
    {
        return qname.length() > qualifier.length() &&
            qname.startsWith(qualifier) && 
            qname.charAt(qualifier.length()) == '.';
    }

    private String whiteListCheck(final String qname) 
    {
        return whitelist.allows(qname) ? qname : null;
    }

    // If successful, qname is returned (possibly with a top-level qualification 
    // stripped off.  If there was no match or a whitelist violation, null is
    // returned.
    private String checkQualifierAndWhiteList(final String qname) 
    {
        if (qualifier == null) 
            return whiteListCheck(qname);

        if (qualifierMatch(qname)) 
            return whiteListCheck(stripQualifier(qname));

        return null;
    }
    
    public ValueBinding findValueBinding(final String qname)
    {
        final String base = checkQualifierAndWhiteList(qname);
        if (base != null) 
        {
            if (module.isExported(base)) 
                return module.findValueBinding(base, false);
        }
        return null;
    }

    public TypeDef findTypeDef(final String qname) 
    {
        final String base = checkQualifierAndWhiteList(qname);
        if (base != null)
        {
            if (module.isExported(base)) 
                return module.findType(base, false);
        }
        return null;
    }
}
