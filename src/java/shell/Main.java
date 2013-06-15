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
package shell;

import com.google.common.collect.Sets;
import compile.*;
import compile.Compiler;
import compile.analyze.ImportResolver;
import compile.gen.UnitBuilder;
import compile.gen.Unit;
import compile.module.Import;
import compile.module.Module;
import compile.term.*;
import shell.console.Console;
import runtime.sys.Logging;

import java.io.*;
import java.util.*;

/**
 * Shell harness and {@link #main(String[])}.
 *
 * @author Basil Hosmer
 */
public final class Main
{
    private static final Loc SHELL_LOC = new Loc("<shell>");

    private final ShellConfig shellConfig;
    private final FileServices fileServices;
    private final Console console;
    private final ShellScriptManager shellScriptManager;

    private boolean blockMode = false;
    private String blockBuf = "";

    private int compareErrors = 0;

    public Main(final ShellConfig shellConfig)
    {
        this.shellConfig = shellConfig;
        this.fileServices = new FileServices(shellConfig);
        this.shellScriptManager = new ShellScriptManager();
        this.console = Console.create(
            shellConfig, shellScriptManager,
            fileServices, shellConfig.getCommandFiles());
    }

    /**
     *
     */
    public int run()
    {
        Session.pushErrorCount();
        Session.setMessageLevel(shellConfig.getMessageLevel());

        for (final String file : shellConfig.getLoadFiles())
        {
            loadFile(file);
        }

        if (shellConfig.getInteractive())
        {
            interactive();
        }

        final int errorCount = Session.popErrorCount() + Logging.getErrorCount();

        Session.info(SHELL_LOC, "quitting, {0} total errors{1}",
            errorCount,
            compareErrors > 0 ? ", " + compareErrors + " compare errors" : "");

        return errorCount == 0 ? 0 : 1;
    }

    /**
     *
     */
    private void interactive()
    {
        if (shellConfig.getHelp())
            cmdPrintHelp();

        String input;
        do
        {
            input = getConsoleInput().trim();
        }
        while (input.length() == 0 || evalInput(input));
    }

    /**
     *
     */
    private String getConsoleInput()
    {
        console.displayPrompt();

        try
        {
            final String line = console.readLine();

            if (line != null)
            {
                // i.e., we're in a command file, not interactive. yup
                if (!shellConfig.getCommandFiles().isEmpty())
                    System.out.println(line);

                return line;
            }
            else
            {
                // hack for end of file stream
                return "$q";
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    /**
     *
     */
    private boolean evalInput(final String input)
    {
        boolean result = true;
        if (input.charAt(0) == '$')
        {
            result = evalCommand(input.substring(1));
        }
        else if (blockMode)
        {
            blockBuf += input + "\n";
        }
        else
        {
            shellScriptManager.evalShellInput(
                SHELL_LOC, new StringReader(input), shellConfig.getImports());
        }
        return result;
    }

    /**
     * Process command string. '$' has been removed,
     * command may be empty.
     * Return true to continue, false to quit shell.
     * TODO tighten up parsing--currently extra args ignored, etc.
     * TODO factor common stuff with ShellConfig command line processing
     */
    private boolean evalCommand(final String command)
    {
        boolean result = true;

        final String[] words = command.split(" ");
        final String first = words[0];

        if (first.isEmpty())
        {
            Session.error("no command specified");
        }
        else if (first.equals("b") || first.equals("block"))
        {
            result = cmdToggleBlockMode();
        }
        else if (first.equals("c") || first.equals("clear"))
        {
            cmdClearState(true);
        }
        else if (first.equals("d") || first.equals("debug"))
        {
            cmdSetDebug(words);
        }
        else if (first.equals("h") || first.equals("help"))
        {
            cmdPrintHelp();
        }
        else if (first.equals("i") || first.equals("import"))
        {
            cmdImport(words);
        }
        else if (first.equals("l") || first.equals("load"))
        {
            cmdLoadScript(words);
        }
        else if (first.equals("m") || first.equals("messages"))
        {
            cmdSetMessages(words);
        }
        else if (first.equals("q") || first.equals("quit"))
        {
            return false;   //  NOTE
        }
        else if (first.equals("t") || first.equals("type"))
        {
            cmdPrintTypes(command.substring(first.length()));
        }
        else if (first.equals("u") || first.equals("unit"))
        {
            cmdDumpUnit(words);
        }
        else if (first.equals("v") || first.equals("vars"))
        {
            cmdDumpVars(words);
        }
        else if (first.equals("w") || first.equals("write"))
        {
            cmdWriteUnits();
        }
        else
        {
            Session.error("unrecognized command: {0}", command);
        }

        return result;
    }

    /**
     *
     */
    private boolean cmdWriteUnits()
    {
        final String path =
            shellConfig.getWritePath() == null ? "ws" : shellConfig.getWritePath();
        return compile.Compiler.writeUnits(path);
    }

    /**
     * Toggle block (multi-line) input mode
     */
    private boolean cmdToggleBlockMode()
    {
        boolean result = true;
        blockMode = !blockMode;
        console.setBlockMode(blockMode);
        if (blockMode)
        {
            System.out.println(
                "(Entering block-input mode, $b again on empty line to commit.)");
        }
        else
        {
            if (!blockBuf.isEmpty())
            {
                result = evalInput(blockBuf);
                blockBuf = "";
            }
        }
        return result;
    }

    /**
     * Clear bound variables, etc.
     */
    private void cmdClearState(final boolean verbose)
    {
        shellScriptManager.clearShellState();
        if (verbose)
            Session.info("cleared");
    }

    /**
     * help text
     */
    private static final String[] helpText =
    {
        "",
        "Enter a command, an expression, or several expressions separated by semicolons.",
        "",
        "Commands:",
        "",
        "$b / $block            enter block (multi-line) input mode. $b again on",
        "                       empty line to commit",
        "",
        "$c / $clear            clear definitions",
        "",
        "$d / $debug            toggle runtime debug messages",
        "",
        "$h / $help             print this message",
        "",
        "$i / $import           print current import list. The import list provides",
        "                       definitions to interactive shell input.",
        "                       Optional args:",
        "           <spec>      add <spec> to import list. <spec> is standard import",
        "                       statement syntax. Note that this command will not attempt",
        "                       to load the specified module until the next interactive",
        "                       input is evaluated.",
        "           -<spec>     removes <spec> from import list",
        "           -<pos>      removes import list item at given position",
        "",
        "$l / $load <module>    load and run a script. A file pathname is created by",
        "                       replacing dot with the file separator and appending a",
        "                       .m extension. If such a file is found in the path, it",
        "                       is loaded and executed. The search path is the cwd and",
        "                       what is specified on the command line with ",
        "                       -path <path1>;<path2>;...",
        "",
        "$m / $messages         toggle verbose compilation messages",
        "",
        "$q / $quit             quit",
        "",
        "$t / $type <expr>      print the type of <expr>",
        "",
        "$u / $unit             print unit details (including generated source, if available).",
        "                       A unit is the compilation product of an interactive statement,",
        "                       or loaded or imported script. Without arguments, details are",
        "                       printed for most recent unit compilation attempt (failed or ",
        "                       successful).",
        "                       Optional args:",
        "           ?           print unit history, most recent first",
        "           !           print transitive closure of most recent unit and all of its",
        "                       imports (except language support implicits)",
        "           <module>    print unit for module <module>",
        "           <module>!   ...and all of its imports (except language support implicits)",
        "           *           print all units, including language support implicits",
        "",
        "$v / $vars             print variable bindings. Without arguments, all available",
        "                       qualified and unqualified bindings are included.",
        "                       Optional args:",
        "           ?           list inhabited namespaces",
        "           !           print only variables defined in the most recent interactive",
        "                       input or loaded script",
        "           <namespace> print only variables defined in namespace <namespace>",
        "",
        "$w / $write            write binaries to <cwd>/ws, or to path specified",
        "                       on the command line with -writepath",
        ""
    };

    /**
     * print help text
     */
    private void cmdPrintHelp()
    {
        for (int i = 0; i < helpText.length; i++)
            System.out.println(helpText[i]);
    }

    /**
     * toggle emission of debug info into compiled code
     */
    private void cmdSetDebug(final String[] words)
    {
        // toggle if true/false not specified
        final boolean debug =
            words.length > 1 ? Boolean.parseBoolean(words[1]) : !shellConfig.getDebug();
        if (debug != shellConfig.getDebug())
        {
            shellConfig.setDebug(debug);
            Session.info("debug code {0}", debug ? "on" : "off");

            // set runtime logging level
            Logging.setDebug(debug);
        }
    }

    /**
     * Add, list, or remove a import to be used for every (following) shell module
     */
    private void cmdImport(final String[] words)
    {
        assert words.length > 0;

        if (words.length == 1)
        {
            // $import => list 'em
            listImplicitImports();
            return;
        }

        final String spec = StringUtils.join(
            Arrays.asList(words).subList(1, words.length), " ");

        if (spec.startsWith("-"))
        {
            // $import -<spec> => remove <spec> from import list
            clearImplicitImport(spec.substring(1));
        }
        else
        {
            // $import <spec> => add <spec> to import list
            addImplicitImport(spec);
        }
    }

    /**
     *
     */
    private void listImplicitImports()
    {
        final List<ImportStatement> imports = shellConfig.getImports();

        if (imports.isEmpty())
        {
            System.out.println("import list is empty");
            return;
        }

        System.out.println("import list: ");
        for (int i = 0; i < imports.size(); ++i)
        {
            System.out.println(i + ". " + imports.get(i).dumpAbbrev());
        }
    }

    /**
     *
     */
    private void clearImplicitImport(final String spec)
    {
        final List<ImportStatement> imports = shellConfig.getImports();

        try
        {
            final int position = Integer.parseInt(spec);
            if (position >= 0 && position < imports.size())
                imports.remove(position);
            else
                Session.error("No import at position {0}", position);
        }
        catch (NumberFormatException nfe)
        {
            int matches = 0;
            int match_position = -1;
            for (int i = 0; i < imports.size(); ++i)
            {
                if (imports.get(i).dumpAbbrev().startsWith(spec))
                {
                    ++matches;
                    match_position = i;
                }
            }
            if (matches == 1)
                imports.remove(match_position);
            else if (matches == 0)
                Session.error("No import matches ''{0}''", spec);
            else // matches > 1
                Session.error("Ambiguous import specification ''{0}''", spec);
        }
    }

    /**
     *
     */
    private void addImplicitImport(final String spec)
    {
        final ImportStatement stmt =
            ShellScriptManager.parseImportSpec(Loc.INTRINSIC, spec);

        if (stmt != null)
        {
            if (!ImportResolver.moduleExists(stmt.getModuleName()))
                Session.error("Cannot find module ''{0}''", stmt.getModuleName());
            else
                shellConfig.getImports().add(stmt);
        }
        else
        {
            Session.error("invalid import specification ''{0}''", spec);
        }
    }

    /**
     * load script file
     */
    private void cmdLoadScript(final String[] words)
    {
        switch(words.length)
        {
            case 1:
                Session.error("script file not specified");
                break;
            case 2:
                loadFile(words[1]);
                break;
            default:
                Session.error("extra arguments after module name {0}", words[1]);
                break;
        }
    }

    /**
     *
     */
    private void loadFile(final String moduleName)
    {
        final String fileName = NameUtils.module2file(moduleName);
        final File file = fileServices.findFile(fileName);

        if (file != null)
        {
            try
            {
                final Loc loc = new Loc(file.getPath());
                final Reader r = fileServices.getReader(file);

                shellScriptManager.loadScript(loc, r);

                r.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Session.error("script file \"{0}\" not found", fileName);
        }
    }

    /**
     * toggle debug messages during compilation
     */
    private void cmdSetMessages(final String[] words)
    {
        // toggle if true/false not specified
        final boolean messages =
            words.length > 1 ? Boolean.parseBoolean(words[1]) :
                !shellConfig.getMessages();

        if (messages != shellConfig.getMessages())
        {
            shellConfig.setMessages(messages);
            Session.setMessageLevel(shellConfig.getMessageLevel());
            Session.info("trace messages {0}", messages ? "on" : "off");
        }
    }

    /**
     * Compile and print types of expr list. Treat inline comment as test target.
     */
    private void cmdPrintTypes(final String input)
    {
        if (input.length() > 0)
        {
            Session.pushErrorCount();

            final Map<String, String> dumps =
                shellScriptManager.dumpTypes(new Loc("<shell>"),
                    new StringReader(input), shellConfig.getImports());

            if (Session.popErrorCount() == 0)
            {
                int i = 0;
                for (final Map.Entry<String, String> entry : dumps.entrySet())
                {
                    final String valueDump = entry.getKey();
                    final String typeDump = entry.getValue();

                    System.out.println(valueDump + " : " + typeDump);

                    i++;
                }
            }
        }
    }

    /**
     * Dump unit info to the console.
     * If no number is specified, unit dumped is the last one for which Java
     * source was successfully generated (though it may not have run, due to
     * failure to compile the generated Java).
     * If a number is specified, it signifies steps into the past. 0 is the
     * most recent unit successfully run; 1 is the one before that, etc.
     * If '*' is specified, dump all units, earliest to latest.
     */
    private void cmdDumpUnit(final String[] words)
    {
        final Unit lastBuildAttempt = UnitBuilder.getLastBuildAttempt();

        if (lastBuildAttempt == null)
        {
            System.out.println("no units have yet been compiled");
            return;
        }

        if (words.length == 1)
        {
            // Note: for compiler dev, second param ensures
            // a source dump even on failed compiles
            dumpUnit(lastBuildAttempt, false);
        }
        else if (words[1].equals("!"))
        {
            // Dumps all units and imports recursively
            dumpUnitsAndImports(lastBuildAttempt);
        }
        else if (words[1].equals("*") || words[1].equals("+"))
        {
            final boolean omitIfEmpty = words[1].equals("+");
            final List<Unit> history = shellScriptManager.getUnitHistory();
            for (int i = history.size() - 1; i >= 0; --i)
                dumpUnit(history.get(i), omitIfEmpty);
        }
        else
        {
            try
            {
                final Unit pastUnit = shellScriptManager.getHistoryUnit(
                    Integer.valueOf(words[1]));

                if (pastUnit == null)
                {
                    System.out.println("too far back: " + words[1]);
                }
                else
                {
                    dumpUnit(pastUnit, false);
                }
            }
            catch (NumberFormatException ignored)
            {
                System.out.println("invalid step number " + words[1]);
            }
        }
    }

    /**
     * Dump unit with current debug trace setting
     * Note that this is not an exact copy of generated code,
     * except at the statement level.
     */
    private void dumpUnit(final Unit unit, final boolean omitIfEmpty)
    {
        if (!(omitIfEmpty &&
            unit.getModule().getLets().isEmpty() &&
            unit.getModule().getTypeDefs().isEmpty()))
        {
            System.out.println(unit.dump());
        }
        else
        {
            System.out.println(
                "unit " + unit.getModule().getName() +
                    " contains no definitions, skipping");
        }

        System.out.println();
    }

    /**
     * dump a unit, then its imports and their imports, etc. (depth-first)
     */
    private void dumpUnitsAndImports(final Unit unit)
    {
        dumpUnit(unit, false);

        for (final Module imported : unit.getModule().getImportedModules())
            dumpUnitsAndImports(Compiler.getUnitDictionary().getUnit(imported));
    }

    /**
     * Dump current var bindings, sorted by name.
     * "." is the current set of bindings in the default namespace, "?" prints
     * the known namespaces, and any other arg is a namespace that will be
     * printed.
     */
    private void cmdDumpVars(final String[] words)
    {
        final List<Unit> history = shellScriptManager.getUnitHistory();

        if (history.size() == 0)
            return;

        final Module module = history.get(0).getModule();

        if (words.length == 2 && words[1].equals("?"))
        {
            // list namespaces
            System.out.println("Namespaces:\n.");

            for (final String ns : Sets.newTreeSet(module.getNamespaces()))
                System.out.println(ns);

            return;
        }

        //  otherwise print bindings by namespace:

        // collect namespaces
        final LinkedHashSet<String> namespaces = new LinkedHashSet<String>();
        boolean printLocalBindings = false;
        if  (words.length == 1)
        {
            printLocalBindings = true;
        }
        else
        {
            for (int i = 1; i < words.length; i++)
            {
                final String word = words[i];
                if (word.equals("."))
                    printLocalBindings = true;
                else
                    namespaces.add(word);
            }
        }

        // print local bindings if desired, split between locals and unq imports
        if (printLocalBindings)
        {
            System.out.println("// Unqualified bindings:");
            System.out.println("// local definitions");

            for (final LetBinding let : module.getLets().values())
                System.out.println(let.dump());

            System.out.println("// unqualified imports");

            for (final Import imp : module.getUnqualifiedImports())
            {
                System.out.println("// imported from " + imp.getModule().getName());
                for (final LetBinding let : imp.getModule().getLets().values())
                    System.out.println(let.dump());
            }

            System.out.println("// End unqualified bindings");
        }

        // print qualified bindings by namespace
        for (final String namespace : namespaces)
        {
            System.out.println("// Bindings in namespace " + namespace);

            for (final Import imp : module.getNamespaceImports(namespace))
            {
                System.out.println("// imported from " + imp.getModule().getName());
                for (final LetBinding let : imp.getModule().getLets().values())
                    System.out.println(let.dump());
            }

            System.out.println("// End bindings in namespace " + namespace);
        }
    }

    /**
     *
     */
    public static void main(final String[] args)
    {
        final ShellConfig shellConfig = new ShellConfig();
        if (shellConfig.processArgs(args))
        {
            final Main main = new Main(shellConfig);
            main.cmdClearState(false);
            final int result = main.run();
            System.exit(result);
        }
        else
        {
            System.exit(1);
        }
    }
}
