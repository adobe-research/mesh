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

import compile.Config;
import compile.Session;
import compile.Loc;
import compile.term.ImportStatement;
import compile.analyze.ImportResolver;
import runtime.sys.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Container for shell configuration options.
 *
 * @author Basil Hosmer
 */
public final class ShellConfig
{
    private final List<String> scriptPath = new ArrayList<String>();
    private final List<ImportStatement> imports = new ArrayList<ImportStatement>();
    private final List<String> commandFiles = new ArrayList<String>();
    private String writePath;
    private boolean interactive = true;
    private boolean debug = false;
    private boolean messages = false;
    private boolean help = false;
    private boolean readline = true;

    public List<String> getScriptPath()
    {
        return scriptPath;
    }

    // These get implicitly imported into every module
    public List<ImportStatement> getImports()
    {
        return imports;
    }

    public void listImplicitImports()
    {
        System.out.println("Auto-imports: ");
        for (int i = 0; i < imports.size(); ++i)
            System.out.println("(" + i + ")\t" + imports.get(i).dumpAbbrev());
    }

    public void addImplicitImport(final String spec)
    {
        final ImportStatement stmt = ShellScriptManager.parseImportStatement(
                Loc.INTRINSIC, "import " + spec);

        if (stmt != null)
        {
            if (!ImportResolver.moduleExists(stmt.getModuleName()))
                Session.error("Cannot find module ''{0}''", stmt.getModuleName());
            else
                imports.add(stmt);
        }
    }

    public void clearImplicitImport(final String spec)
    {
        try 
        {
            final int position = Integer.parseInt(spec);
            if (position >= 0 && position < imports.size())
                imports.remove(position);
            else
                Session.error("No import at index {0}", position);
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

    public List<String> getCommandFiles()
    {
        return commandFiles;
    }

    public String getWritePath()
    {
        return writePath;
    }

    public void setWritePath(final String writePath)
    {
        this.writePath = writePath;
    }

    public boolean getInteractive()
    {
        return interactive;
    }

    public void setInteractive(final boolean interactive)
    {
        this.interactive = interactive;
    }

    public boolean getDebug()
    {
        return debug;
    }

    public void setDebug(final boolean debug)
    {
        this.debug = debug;
    }

    public boolean getMessages()
    {
        return messages;
    }

    public void setMessages(final boolean messages)
    {
        this.messages = messages;
    }

    public int getMessageLevel()
    {
        return messages ? Session.MESSAGE_DEBUG : Session.MESSAGE_INFO;
    }

    public boolean getHelp()
    {
        return help;
    }

    public void setHelp(final boolean help)
    {
        this.help = help;
    }

    public boolean getReadline() 
    {
        return readline;
    }

    public void setReadline(final boolean readline) 
    {
        this.readline = readline;
    }


    /**
     * Process command line arguments as shell config commands.
     */
    public boolean processArgs(final String... args)
    {
        Session.pushErrorCount();

        for (int i = 0; i < args.length; i++)
        {
            final String arg = args[i];

            if (arg.length() == 0)
                continue;

            if (arg.charAt(0) == '-')
            {
                final String command = arg.substring(1);

                if (command.equals("arg"))
                {
                    if (i < args.length - 1)
                    {
                        Arguments.add(args[i + 1]);
                        i = i + 1;
                    }
                    else
                    {
                        Session.error("argument value not specified");
                    }
                }
                else if (command.equals("args"))
                {
                    if (i < args.length - 1)
                    {
                        for (final String value : args[i + 1].split(","))
                            Arguments.add(value.trim());
                        i = i + 1;
                    }
                    else
                    {
                        Session.error("argument values not specified");
                    }
                }
                else if (command.equals("debug"))
                {
                    if (i < args.length - 1)
                    {
                        final boolean debug = parseBoolArg(command, args[i + 1]);
                        setDebug(debug);
                        i = i + 1;
                    }
                    else
                    {
                        Session.error("debug mode not specified");
                    }
                }
                else if (command.equals("exit"))
                {
                    setInteractive(false);
                }
                else if (command.equals("help"))
                {
                    setHelp(true);
                }
                else if (command.equals("import"))
                {
                    if (i < args.length - 1)
                    {
                        for (final String script : Arrays.asList(args[i + 1].split(";")))
                        {
                            addImplicitImport(script);
                        }
                        i = i + 1;
                    }
                    else
                    {
                        Session.error("import scripts not specified");
                    }
                }
                else if (command.equals("messages"))
                {
                    if (i < args.length - 1)
                    {
                        final boolean messages = parseBoolArg(command, args[i + 1]);
                        setMessages(messages);
                        i = i + 1;
                    }
                    else
                    {
                        Session.error("message mode not specified");
                    }
                }
                else if (command.equals("path"))
                {
                    if (i < args.length - 1)
                    {
                        getScriptPath().addAll(Arrays.asList(args[i + 1].split(";")));
                        for (final String path : Arrays.asList(args[i + 1].split(";")))
                        {
                            Config.addSearchPath(path);
                        }
                        i = i + 1;
                    }
                    else
                    {
                        Session.error("script path not specified");
                    }
                }
                else if (command.equals("readline"))
                {
                    if (i < args.length - 1)
                    {
                        final boolean readline = parseBoolArg(command, args[i + 1]);
                        setReadline(readline);
                        i = i + 1;
                    }
                    else
                    {
                        Session.error("readline mode not specified");
                    }
                }
                else if (command.equals("writepath"))
                {
                    if (i < args.length - 1)
                    {
                        setWritePath(args[i + 1]);
                        i = i + 1;
                    }
                    else
                    {
                        Session.error("write path not specified");
                    }
                }
                else
                {
                    Session.error("unrecognized command \"{0}\"", arg);
                }
            }
            else if (arg.charAt(0) == '@')
            {
                getCommandFiles().addAll(Arrays.asList(arg.substring(1).split(";")));
            }
            else
            {
                Session.error("commands must start with '-'; command file references must start with '@'", arg);
            }
        }

        return Session.popErrorCount() == 0;
    }

    private static boolean parseBoolArg(final String command, final String arg)
    {
        if (arg.equals("true"))
            return true;
        else if (arg.equals("false"))
            return false;

        Session.error("invalid boolean argument ''{0}'' to command ''{1}''", arg, command);
        return false;
    }
}
