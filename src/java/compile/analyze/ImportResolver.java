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

        final String moduleName = stmt.getFrom();
        final Module current = getModule();
        final ModuleDictionary dict = current.getModuleDictionary();

        if (Session.isDebug())
            Session.debug(loc, "Importing module: ''{0}''", moduleName);

        Module loaded = dict.get(moduleName);

        if (loaded == null) 
        {
            loaded = performLoad(loc, moduleName); 
        }
        else
        {
            if (Session.isDebug())
                Session.debug(loc, "''{0}'' is already loaded", moduleName);
        }

        if (loaded != null) 
        {
            final WhiteList wl;

            if (!stmt.isWildcard())
            {
                final List<String> syms = stmt.getSymbols();

                verifyImports(syms, loaded);
                wl = WhiteList.enumerated(syms);
            }
            else
            {
                wl = WhiteList.open();
            }

            current.addImport(new Import(loaded, stmt.getInto(), wl));
        }
    }

    private boolean verifyImports(final List<String> syms, final Module module)
    {
        return verifySymbols(syms, module, "import");
    }

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
        final String pathFragment = NameUtils.module2file(moduleName);

        final File modulePath = getModulePath(loc, pathFragment);
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

        final Module module = ScriptCompiler.compileModule(
            new Loc(filePath), reader, moduleName,
            getModule().getModuleDictionary());

        if (module == null) 
        {
            Session.error(loc, "Error loading module ''{0}''", moduleName);
            return null;
        }
        else
        {
            if (Session.isDebug())
                Session.debug(loc,
                    "Successfully loaded module ''{0}''", moduleName);

            return module;
        }
    }

    /**
     * Attempt to build a valid full module path from a path fragment,
     * using the importing module's path and the current search path
     * as context.
     */
    private File getModulePath(final Loc loc, final String pathFragment)
    {
        File modulePath = null;

        final String currentModulePath = getModule().getLoc().getPath();
        final File currentModuleFile = new File(currentModulePath);
        if (currentModuleFile.isFile())
        {
            final String rootPath = currentModuleFile.getParent();
            modulePath = findModuleInPath(loc, rootPath, pathFragment);
        }

        if (modulePath == null)
        {
            final List<String> searchPaths = Session.getSearchPaths();
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
