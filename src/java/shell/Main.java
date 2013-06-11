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
        this(shellConfig, new FileServices(shellConfig));
    }

    public Main(final ShellConfig shellConfig, final FileServices fileServices)
    {
        this.shellConfig = shellConfig;
        this.fileServices = fileServices;
        this.shellScriptManager = new ShellScriptManager(Config.newUnitManager());
        this.console = Console.create(shellConfig, shellScriptManager,
            fileServices, shellConfig.getCommandFiles());
    }

    /**
     *
     */
    public int run()
    {
        Session.pushErrorCount();
        Session.setMessageLevel(shellConfig.getMessageLevel());

        if (shellConfig.getInteractive())
            interactive();

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
     * Process commands.
     * TODO unify with ShellConfig command line processing, clean up generally
     */
    private boolean evalCommand(final String command)
    {
        final String[] words = command.split(" ");
        final String first = words[0];
        boolean result = true;

        if (first.equals("b") || first.equals("block"))
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
        "$b / $block            enter block (multi-line) input mode, $b again on",
        "                       empty line to commit",
        "",
        "$c / $clear            clear variable bindings",
        "",
        "$d / $debug            toggle runtime debug messages",
        "",
        "$h / $help             print this message",
        "",
        "$i / $import           print current import list. The import list provides",
        "                       definitions to interactive shell input.",
        "                       Optional args:",
        "             <spec>    add <spec> to import list. <spec> is standard import",
        "                       statement syntax. Note that this command will attempt to",
        "                       locate and initialize the specified module immediately.",
        "             -<spec>   removes <spec> from import list",
        "             -<pos>    removes import list item at given position",
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
        "$u / $unit             print unit details (including generated source, if available)",
        "                       for most recent unit compilation attempt (failed or successful).",
        "                       A unit is the compilation product of an interactive statement,",
        "                       or loaded or imported script.",
        "                       Optional args:",
        "           ?           print unit history, most recent first",
        "           !           print transitive closure of most recent unit and all its imports",
        "           <module>    print unit for module <module>",
        "           <module>!   ...and all its imports",
        "           *           print all units",
        "",
        "$v / $vars             print variable bindings. Without arguments, all available",
        "                       qualified and unqualified bindings are included.",
        "                       Optional args:",
        "           .           print variables defined in the most recent interactive,",
        "                       input or loaded script",
        "           ?           list inhabited namespaces and the modules inhabiting them",
        "           <module>    print variable definitions from module <module>",
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
            shellConfig.listImplicitImports();
        }
        else if (words.length == 2 && words[1].equals("?"))
        {
            // $import ? => list 'em
            shellConfig.listImplicitImports();
        }
        else if (words[1].equals("-"))
        {
            // $import - x, y, z => remove x, y, z from import list
            final List<String> specPart =
                Arrays.asList(words).subList(2, words.length);
            final String spec = StringUtils.join(specPart, " ");
            shellConfig.clearImplicitImport(spec);
        }
        else
        {
            // $import + x, y, z => add x, y, z to import list
            // $import x, y, z => ditto
            final int specStart = words.length > 2 && words[1].equals("+") ? 2 : 1;
            final List<String> specPart =
                Arrays.asList(words).subList(specStart, words.length);
            final String spec = StringUtils.join(specPart, " ");
            shellConfig.addImplicitImport(spec);
        }
    }

    /**
     * load script file
     */
    private void cmdLoadScript(final String[] words)
    {
        if (words.length > 1)
            loadFile(words[1]);
        else
            Session.error("script file not specified");
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
                final boolean debug = shellConfig.getDebug();
                final List<ImportStatement> imports = shellConfig.getImports();

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
        final int comment = input.indexOf("//");
        final String exprs;
        final List<String> comps;
        if (comment >= 0)
        {
            exprs = input.substring(0, comment);
            comps = Arrays.asList(input.substring(comment + 2).split(";"));
        }
        else
        {
            exprs = input;
            comps = null;
        }

        if (exprs.length() > 0)
        {
            Session.pushErrorCount();

            final Map<String, String> dumps =
                shellScriptManager.dumpTypes(new Loc("<shell>"),
                    new StringReader(exprs), shellConfig.getImports());

            if (Session.popErrorCount() == 0)
            {
                int i = 0;
                for (final Map.Entry<String, String> entry : dumps.entrySet())
                {
                    final String valueDump = entry.getKey();
                    final String typeDump = entry.getValue();

                    System.out.println(valueDump + " : " + typeDump);

                    if (comps != null)
                    {
                        final String comp = comps.size() > i ? comps.get(i).trim() : null;
                        if (!typeDump.equals(comp))
                        {
                            Session.error(SHELL_LOC,
                                "COMPARE ERROR $t: expected ''{0}'', got ''{1}''",
                                comp, typeDump);
                            compareErrors++;
                        }
                        else
                        {
                            Session.debug(SHELL_LOC,
                                "$t ''{0}'' expected and got ''{1}''",
                                valueDump, comp);
                        }
                    }

                    i++;
                }
            }
            else
            {
                final String errorMsg = Session.getLastMessage();
                if (comps != null)
                {
                    // TODO need @ERR token
                    final String comp = comps.get(0).trim();

                    if (comp.isEmpty() || !errorMsg.endsWith(comp))
                    {
                        Session.error(SHELL_LOC,
                            "COMPARE ERROR $t: expected ''{0}'', got ''{1}''",
                            comp, errorMsg);

                        compareErrors++;
                    }
                    else if (Session.isDebug())
                    {
                        Session.debug(SHELL_LOC,
                            "$t ''{0}'' expected and got ''{1}''",
                            exprs.trim(), comp);
                    }
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
