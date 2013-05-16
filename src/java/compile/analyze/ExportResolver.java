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
package compile.analyze;

import compile.Loc;
import compile.Session;
import compile.ScriptCompiler;
import compile.module.*;
import compile.term.*;

import java.util.*;
import java.io.*;

/**
 * Find load statements and load the module, placing all symbols defined into
 * a module namespace.
 *
 * @author Keith McGuigan
 */
public final class ExportResolver extends ModuleVisitor<Object>
{
    private boolean exportFound;

    public ExportResolver(final Module module)
    {
        super(module);
    }

    /**
     *
     */
    public boolean resolve()
    {
        if (Session.isDebug())
            Session.debug(getModule().getLoc(), "Processing exports...");

        exportFound = false;
        return process();
    }

    // ModuleVisitor

    @Override
    protected void visitExportStatement(final ExportStatement stmt) 
    {
        final Loc loc = stmt.getLoc();

        if (exportFound) 
        {
            Session.error(loc, "Multiple export statements");
            return;
        }

        exportFound = true;

        final Module current = getModule();
        if (stmt.getLocalsOnly()) 
            current.setExportLocalsOnly();
        else if (stmt.getSymbols() == null) 
            current.setExports(WhiteList.open());
        else
        {
            if (verifyExports(stmt.getSymbols(), current))
                current.setExports(WhiteList.enumerated(stmt.getSymbols()));
        }
    }

    private boolean verifyExports(final List<String> syms, final Module module)
    {
        return verifySymbols(syms, module, "export");
    }

    // TODO: an exact duplicate of this function exists in ImportResolver.  
    // Find a way to unify them.
    private boolean verifySymbols(
        final List<String> syms, final Module module, final String direction)
    {
        boolean status = true;
        final Set<String> namespaces = module.getAllNamespaces();

        for (final String sym : syms) 
        {
            if (sym.endsWith(".*")) 
            {
                final String ns = sym.substring(0, sym.length() - 2);
                if (!namespaces.contains(ns)) 
                {
                    Session.error("No ''{0}'' namespace available for {1}", 
                        ns, direction);
                    status = false;
                }
            }
            else
            {
                if (module.findValueBinding(sym) == null &&
                    module.findType(sym) == null)
                {
                    Session.error("No value or type ''{0}'' available for {1}",
                        sym, direction);
                    status = false;
                }
            }
        }
        return status;
    }
}
