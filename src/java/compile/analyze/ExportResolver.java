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
import compile.module.Module;
import compile.module.WhiteList;
import compile.term.*;

import java.util.*;

/**
 * Find load statements and load the module, placing all symbols defined into
 * a module namespace.
 *
 * @author Keith McGuigan
 */
public final class ExportResolver extends ImportExportResolverBase
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
}
