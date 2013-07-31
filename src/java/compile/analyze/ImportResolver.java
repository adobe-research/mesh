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

import compile.*;
import compile.Compiler;
import compile.gen.Unit;
import compile.module.*;
import compile.term.*;

import java.util.*;
import java.io.*;

/**
 * Process import statements, loading imported symbols into
 * a module namespace.
 *
 * @author Keith McGuigan
 */
public final class ImportResolver extends ImportExportResolverBase
{
    private boolean importsDone;

    public ImportResolver(final Module module)
    {
        super(module);
    }

    /**
     *
     */
    public boolean resolve()
    {
        if (Session.isDebug())
            Session.debug(getModule().getLoc(), "Importing modules...");

        importsDone = false;
        return process();
    }

    // ModuleVisitor

    /**
     *
     */
    @Override
    protected void visitImportStatement(final ImportStatement stmt)
    {
        final Loc loc = stmt.getLoc();

        if (importsDone)
        {
            Session.error(loc,
                "Import statement occurs after executable statements");
            return;
        }

        final Module importer = getModule();
        final String importName = stmt.getModuleName();

        if (Session.isDebug())
            Session.debug(loc, "Importing module: ''{0}''", importName);

        // load imported module, if it hasn't been loaded already
        final Module imported;
        {
            final Module loaded = Compiler.getUnitDictionary().getModule(importName);

            if (loaded == null)
            {
                // note: will add to importer's module dictionary
                imported = performLoad(loc, importName);
            }
            else
            {
                if (Session.isDebug())
                    Session.debug(loc, "''{0}'' is already loaded", importName);

                imported = loaded;
            }
        }

        if (imported != null)
        {
            verifyWhiteList(stmt.getWhiteList(), imported, "import");

            importer.addImport(new Import(imported, stmt));
        }
    }

    /**
     * NOTE: we check positioning of both imports and exports here,
     * to avoid duplicated logic.
     */
    @Override
    protected void visitExportStatement(final ExportStatement stmt) 
    {
        final Loc loc = stmt.getLoc();

        if (importsDone)
        {
            Session.error(loc,
                "Export statement occurs after executable statements");
        }
    }

    @Override
    protected void visitUnboundTerm(final UnboundTerm unboundTerm)
    {
        importsDone = true;
    }

    @Override
    protected Object visitBinding(final Binding binding)
    {
        importsDone = true;
        return null;
    }

    /**
     * Find and load a module specified by the given name.
     * Search is based on importing module's path and the
     * current search path.
     * TODO rework pipeline to handle circularity
     */
    private Module performLoad(final Loc loc, final String moduleName)
    {
        final String currentModulePath = getModule().getLoc().getPath();

        final File modulePath = getModulePath(loc, moduleName, currentModulePath);

        if (modulePath == null)
        {
            Session.error(loc, "Could not load module ''{0}''", moduleName);
            return null;
        }

        if (Session.isDebug())
            Session.debug(loc, "Module ''{0}'' is at ''{1}''",
                moduleName, modulePath.getPath());

        final Reader reader = openFile(loc, modulePath);

        String filePath;
        try 
        {
            filePath = modulePath.getCanonicalPath();
        }
        catch (IOException e)
        {
            filePath = modulePath.getPath();
        }

        // note: compiling imported module eagerly, currently.
        // unit will be added to Session.getUnitDictionary() if successful
        // TODO two-phase

        final Unit unit = Compiler.compileScript(new Loc(filePath), reader, moduleName);

        if (unit == null)
        {
            Session.error(loc, "Error importing module ''{0}''", moduleName);
            return null;
        }
        else
        {
            if (Session.isDebug())
                Session.debug(loc, "Successfully imported module ''{0}''", moduleName);

            return unit.getModule();
        }
    }

    public static boolean moduleExists(final String moduleName)
    {
        return getModulePath(Loc.INTRINSIC, moduleName, null) != null;
    }

    /**
     * Attempt to build a valid full module path from a path fragment,
     * using the importing module's path and the current search path
     * as context.
     */
    private static File getModulePath(final Loc loc, final String moduleName, 
        final String currentPath)
    {
        File modulePath = null;
        final String pathFragment = NameUtils.module2file(moduleName);

        if (currentPath != null)
        {
            final File currentModuleFile = new File(currentPath);
            if (currentModuleFile.isFile())
            {
                final String rootPath = currentModuleFile.getParent();
                modulePath = findModuleInPath(loc, rootPath, pathFragment);
            }
        }

        if (modulePath == null)
        {
            final List<String> searchPaths = Config.getSearchPaths();
            for (int i = 0; modulePath == null && i < searchPaths.size(); ++i)
            {
                final String rootPath = searchPaths.get(i);
                modulePath = findModuleInPath(loc, rootPath, pathFragment);
            }
        }

        return modulePath;
    }

    private static File findModuleInPath(
        final Loc loc, final String path, final String fragment)
    {
        final File file = new File(path, fragment);

        if (file.canRead())
            return file;

        if (Session.isDebug())
            Session.debug(loc, "File ''{0}'' is not readable", file.getPath());

        return null;
    }

    private static Reader openFile(final Loc loc, final File file) 
    {
        Reader reader = null;

        try 
        {
            reader = new FileReader(file);
        }
        catch (IOException e)
        {
            Session.error(loc,
                "Could not load module from file ''{0}'': ''{1}''",
                file.toString(), e.toString());
        }

        return reader;
    }
}
