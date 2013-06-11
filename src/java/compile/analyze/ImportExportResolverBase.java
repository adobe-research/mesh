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

import compile.Session;
import compile.module.Module;
import compile.module.WhiteList;

/**
 * Base class for ImportResolver and ExportResolver to contain common code
 *
 * @author Keith McGuigan
 */
public class ImportExportResolverBase extends ModuleVisitor<Object>
{
    public ImportExportResolverBase(final Module module)
    {
        super(module);
    }

    protected boolean verifyWhiteList(final WhiteList whiteList,
        final Module module, final String direction)
    {
        boolean status = true;

        if (!whiteList.isOpen())
        {
            for (final String sym : whiteList.getEntries())
            {
                if (module.getLocalValueBinding(sym) == null &&
                    module.getLocalTypeBinding(sym) == null)
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
