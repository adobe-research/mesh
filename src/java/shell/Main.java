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

import compile.Loc;
import compile.Session;
import compile.StringUtils;
import compile.gen.java.Unit;
import compile.gen.java.UnitBuilder;
import compile.gen.java.UnitDumper;
import compile.module.Module;
import compile.module.Import;
import compile.term.ValueBinding;
import compile.term.LetBinding;
import shell.console.Console;
import runtime.Logging;

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
        this.shellScriptManager = new ShellScriptManager();
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
    private void loadFile(final String fileName)
    {
        final File file = fileServices.findFile(fileName);

        if (file != null)
        {
            try
            {
                final Loc loc = new Loc(file.getPath());
                final Reader r = fileServices.getReader(file);
                final boolean debug = shellConfig.getDebug();

                shellScriptManager.runScript(loc, r, debug, false);

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
            shellScriptManager.runScript(SHELL_LOC, new StringReader(input),
                shellConfig.getDebug(), true);
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
        else if (first.equals("k") || first.equals("check"))
        {
            cmdCheckOutput(command.substring(first.length()));
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
        return shellScriptManager.writeUnits(path);
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
        shellScriptManager.clear(shellConfig.getLoads());
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
        "$l / $load <filename>  load and run a script file. <filename> path can be",
        "                       absolute, relative to cwd, or relative to a path",
        "                       specified on the command line with",
        "                       -path <path1>;<path2>;...",
        "",
        "$m / $messages         toggle compilation debug messages",
        "",
        "$q / $quit             quit",
        "",
        "$t / $type <expr>      print the type of <expr>",
        "",
        "$u / $unit [<num>|*]   print generated code for a unit (= interactive",
        "                       statement or loaded script). Optional arg is either",
        "                       <num> for the unit that number of steps into the past,",
        "                       or * for all",
        "",
        "$v / $vars [!]         print variable bindings (! to include intrinsics)",
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
     * run input as if entered without $k, and treat inline comment as test target
     */
    private void cmdCheckOutput(final String line)
    {
        if (Session.isDebug())
        {
            System.out
                .println("$k/$check not supported with debug messages enabled ($m)");
            return;
        }

        final int comment = line.indexOf("//");
        final String input;
        final String comp;
        if (comment >= 0)
        {
            input = line.substring(0, comment);
            comp = StringUtils.unescapeJava(line.substring(comment + 2).trim());
        }
        else
        {
            input = line;
            comp = null;
        }

        if (input.length() > 0)
        {
            final ByteArrayOutputStream captureBuffer = new ByteArrayOutputStream();
            final PrintStream stdout = System.out;
            System.setOut(new PrintStream(captureBuffer));

            Session.pushErrorCount();
            shellScriptManager.runScript(SHELL_LOC, new StringReader(line),
                shellConfig.getDebug(), true);
            final boolean err = Session.popErrorCount() > 0;

            System.setOut(stdout);

            String output = captureBuffer.toString();
            System.out.print(output);

            if (comp != null)
            {
                output = output.trim();
                if (err ? (comp.isEmpty() || !output.endsWith(comp)) :
                    !output.equals(comp))
                {
                    Session
                        .error(SHELL_LOC,
                            "COMPARE ERROR $k: expected ''{0}'', got ''{1}''", comp,
                            output);
                    compareErrors++;
                }
            }
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
                shellScriptManager.printExprTypes(new Loc("<shell>"),
                    new StringReader(exprs));

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
                            Session
                                .error(SHELL_LOC,
                                    "COMPARE ERROR $t: expected ''{0}'', got ''{1}''",
                                    comp, typeDump);
                            compareErrors++;
                        }
                        else
                        {
                            Session
                                .debug(SHELL_LOC, "$t ''{0}'' expected and got ''{1}''",
                                    valueDump,
                                    comp);
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
                        Session
                            .debug(SHELL_LOC, "$t ''{0}'' expected and got ''{1}''",
                                exprs.trim(),
                                comp);
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
        if (words.length == 1)
        {
            // DEV ONLY: on failed compile, this gives last generated source
            dumpUnit(UnitBuilder.LastUnit, false);
        }
        else if (words[1].equals("!")) 
        {
            // Dumps all units and imports recursively
            dumpAllUnits(UnitBuilder.LastUnit);
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
                dumpUnit(shellScriptManager.getPastUnit(
                    Integer.valueOf(words[1])), false);
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
        if (unit != null)
        {
            if (!(omitIfEmpty &&
                unit.getModule().getLets().isEmpty() &&
                unit.getModule().getTypeDefs().isEmpty()))
                System.out.println(UnitDumper.dump(unit, shellConfig.getDebug()));
            else
                System.out.println(
                    "unit " + unit.getModule().getName() +
                        " contains no definitions, skipping");
        }
        else
        {
            System.out.println("unit not available");
        }

        System.out.println();
    }

    private void dumpAllUnits(final Unit unit)
    {
        final Set<String> alreadyDumped = new HashSet<String>();
        dumpAllUnits(unit, alreadyDumped);
    }

    private void dumpAllUnits(final Unit unit, final Set<String> alreadyDumped)
    {
        for (final Unit imported : unit.getUnitDictionary().getUnits())
        {
            dumpAllUnits(imported, alreadyDumped);
        }
        if (!alreadyDumped.contains(unit.getModule().getName())) 
        {
            dumpUnit(unit, true);
            alreadyDumped.add(unit.getModule().getName());
        }
    }

    /**
     * Dump current var bindings, sorted by name. "!" arg includes intrinsics,
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

        final Set<String> names = getAllBindings(module);

        if (words.length == 2 && words[1].equals("?")) 
        {
            for (final String ns : module.getNamespaces())
                System.out.println(ns);
        }
        else 
        {
            final Map<String, LetBinding> bindings = new TreeMap<String, LetBinding>();
            if (words.length == 1) 
            {
                for (final String name : names)
                {
                    final ValueBinding vb = module.findUnqualBinding(name);
                    if (vb != null && vb.isLet())
                        bindings.put(name, (LetBinding)vb);
                }
            }
            else
            {
                for (int i = 1; i < words.length; ++i) 
                {
                    for (final String name : names)
                    {
                        final ValueBinding vb = module.findValueBinding(name);
                        if (vb != null && vb.isLet() &&
                            name.startsWith(words[i] + "."))
                            bindings.put(name, (LetBinding)vb);
                    }
                }
            }

            for (final Map.Entry<String,LetBinding> b : bindings.entrySet())
                System.out.println(b.getValue().dump(b.getKey()));
        }
    }

    private Set<String> getAllBindings(final Module module)
    {
        final Set<String> names = new HashSet<String>();
        for (final String name : module.getUnqualifiedNames())
            names.add(name);

        for (final Import imp : module.getImports())
        {
            final Module m = imp.getModule();
            final String qualifier = imp.getQualifier();
            if (qualifier != null)
                for (final String name : m.getUnqualifiedNames())
                    names.add(qualifier + "." + name);
        }
        return names;
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
