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
import compile.module.ImportedModule;
import compile.module.Module;
import compile.module.ModuleDictionary;
import compile.term.ImportStatement;
import compile.term.Binding;
import compile.term.UnboundTerm;

import java.util.*;
import java.io.*;

/**
 * Find load statements and load the module, placing all symbols defined into
 * a module namespace.
 *
 * @author Keith McGuigan
 */
public final class ImportResolver extends ModuleVisitor<Object>
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
            Session.error(loc, "Import statement occurs after executable statements");
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
            String namespace = stmt.getAs();
            if (namespace == null)
                namespace = moduleName;

            final ImportedModule imported = new ImportedModule(loaded, false);
            current.addQualifiedSymbols(namespace, loaded);
            current.addImport(imported);

            if (stmt.isWildcard()) 
            {
                imported.setFullyExported();
            }
            else if (!stmt.qualifiedOnly())
            {
                for (final String sym : stmt.getSymbols())
                {
                    imported.addExport(sym);
                }
            }
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

    private Module performLoad(final Loc loc, final String moduleName)
    {
        final String pathFragment = moduleNameToPathFragment(moduleName);
        File modulePath = null;

        // TODO: should there be a module-relative path component?  I.e, can 
        //       module.abc load module.def just by specifying "import def"?
        final List<String> searchPaths = Session.getSearchPaths();
        for (int i = 0; modulePath == null && i < searchPaths.size(); ++i) 
        {
            final String rootPath = searchPaths.get(i);
            modulePath = findModuleInPath(loc, rootPath, pathFragment);
        }

        if (modulePath == null) 
        {
            Session.error(loc, "Could not load module ''{0}''", moduleName);
            return null;
        }

        if (Session.isDebug())
            Session.debug(loc, "Module ''{0}'' is at ''{1}''", moduleName, modulePath.getPath());

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
                new Loc(filePath), reader, moduleName, getModule().getModuleDictionary());

        if (module == null) 
        {
            Session.error(loc, "Error loading module ''{0}''", moduleName);
            return null;
        }
        else
        {
            if (Session.isDebug())
                Session.debug(loc, "Successfully loaded module ''{0}''", moduleName);

            return module;
        }
    }

    private static File findModuleInPath(final Loc loc, final String path, final String fragment) 
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
            Session.error(loc, "Could not load module from file ''{0}'': ''{1}''", 
                    file.toString(), e.toString());
        }

        return reader;
    }

    private static String moduleNameToPathFragment(final String name) 
    {
        return name.replace('.', File.separatorChar) + ".m";
    }
}
