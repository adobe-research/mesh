
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
 * Note: for now, this class is also responsible for enforcing the
 * constraints of the imported module's export statement, as obtained
 * from {@link Module#isExported(String)}.
 *
 * @author Keith McGuigan
 */
public class Import
{
    /**
     * imported module
     */
    private final Module module;

    /**
     * originating import statement, specifies whitelist and namespace
     */
    private final ImportStatement spec;

    /**
     * Create import from module and specifier.
     * Note: it is the <strong>caller's responsibility</strong> to ensure
     * the following invariants, which are asserted:
     * 1. spec's module name matches our name
     * 2. spec's symbols list is valid w.r.t. our locally defined symbols.
     */
    public Import(final Module module, final ImportStatement spec)
    {
        assert spec.getModuleName().equals(module.getName());
        assert spec.getWhiteList().isValid(module);

        this.module = module;
        this.spec = spec;
    }

    /**
     * Underlying module
     */
    public Module getModule()
    {
        return module;
    }

    /**
     * Return a value binding bound by the given qname, if available,
     * or null.
     *
     * <strong></strong>Note that we filter not only by our own namespace
     * and whitelist, but also by the module's export whitelist.</strong>
     */
    public ValueBinding findValueBinding(final String qname)
    {
        final String base = checkQName(qname);

        return base != null && module.isExported(base) ?
            module.getLocalValueBinding(base) : null;
    }

    /**
     * Return a type binding bound by the given qname, if available,
     * or null.
     *
     * <strong></strong>Note that we filter not only by our own namespace
     * and whitelist, but also by the module's export whitelist.</strong>
     */
    public TypeBinding findTypeBinding(final String qname)
    {
        final String base = checkQName(qname);

        return base != null && module.isExported(base) ?
            module.getLocalTypeBinding(base) : null;
    }

    /**
     *
     */
    public boolean isQualified()
    {
        return spec.isQualified();
    }

    /**
     * namespace
     */
    public String getNamespace()
    {
        return spec.getNamespace();
    }

    /**
     * Checks that a qname matches our namespace and (if so) is allowed by
     * our whitelist. If so, the passed qname is returned, with any qualifying
     * namespace stripped off. If there was no match or a whitelist violation,
     * null is returned.
     */
    private String checkQName(final String qname)
    {
        if (isQualified())
        {
            final String ns = spec.getNamespace();
            final int nslen = ns.length();

            if (qname.length() > nslen && qname.charAt(nslen) == '.' &&
                qname.startsWith(ns))
                return whiteListCheck(qname.substring(nslen + 1));
            else
                return null;
        }
        else
        {
            return !qname.contains(".") ? whiteListCheck(qname) : null;
        }
    }

    /**
     * Returns passed name if allowed by whitelist, otherwise null.
     * Note: expects unqualified names.
     */
    private String whiteListCheck(final String name)
    {
        return spec.getWhiteList().allows(name) ? name : null;
    }

}
