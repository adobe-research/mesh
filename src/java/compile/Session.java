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
package compile;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Simple session logging and error tracking
 *
 * @author Basil Hosmer
 */
public final class Session
{
    public static final ThreadLocal<Session> LOCAL = new ThreadLocal<Session>()
    {
        protected Session initialValue()
        {
            return new Session();
        }
    };

    private static Session getThreadLocal()
    {
        return LOCAL.get();
    }

    public static final int MESSAGE_ERROR = 0;
    public static final int MESSAGE_WARNING = 1;
    public static final int MESSAGE_INFO = 2;
    public static final int MESSAGE_DEBUG = 3;

    /**
     * Message prefixes from message levels
     */
    private static final String[] levelPrefix =
        new String[]{"ERROR", "WARNING", "INFO", "DEBUG"};

    //
    // instance
    //

    private Writer writer;
    private int messageLevel;

    private final Stack<Integer> errorCountStack;
    private int currentErrorCount;
    private int pushedErrorCount;
    private String lastMessage;

    private List<String> searchPath;
    private List<String> implicitImports;

    // TODO remove when we go to two-phase pipeline
    private ArrayDeque<String> moduleStack;
    private boolean inImplicitImport;

    /**
     * @param writer       Writer to use. If null, use System.out
     * @param messageLevel initial message level
     */
    private Session(final Writer writer, final int messageLevel)
    {
        this.writer = writer;
        this.messageLevel = messageLevel;
        this.errorCountStack = new Stack<Integer>();
        this.currentErrorCount = 0;
        this.pushedErrorCount = 0;

        this.searchPath = new ArrayList<String>();
        this.implicitImports = new ArrayList<String>();

        this.moduleStack = new ArrayDeque<String>();
        this.inImplicitImport = false;
    }

    /**
     * Begin with System.out, message level MESSAGE_INFO
     */
    private Session()
    {
        this(null, MESSAGE_INFO);
    }

    public static Writer getWriter()
    {
        return getThreadLocal().writer;
    }

    public static void setWriter(final Writer writer)
    {
        getThreadLocal().writer = writer;
    }

    public static int getMessageLevel()
    {
        return getThreadLocal().messageLevel;
    }

    public static void setMessageLevel(final int messageLevel)
    {
        if (messageLevel < MESSAGE_ERROR || messageLevel > MESSAGE_DEBUG)
        {
            throw new IllegalArgumentException("unknown message level: " + messageLevel);
        }

        getThreadLocal().messageLevel = messageLevel;
    }

    public static void pushErrorCount()
    {
        final Session session = getThreadLocal();

        session.errorCountStack.push(session.currentErrorCount);
        session.pushedErrorCount += session.currentErrorCount;
        session.currentErrorCount = 0;
    }

    public static int popErrorCount()
    {
        final Session session = getThreadLocal();

        final int popErrorCount = session.currentErrorCount;
        session.pushedErrorCount -= session.errorCountStack.peek();
        session.currentErrorCount += session.errorCountStack.pop();
        return popErrorCount;
    }

    public static int getCurrentErrorCount()
    {
        return getThreadLocal().currentErrorCount;
    }

    public static int getErrorCount()
    {
        final Session session = getThreadLocal();

        return session.pushedErrorCount + session.currentErrorCount;
    }

    public static String getLastMessage()
    {
        return getThreadLocal().lastMessage;
    }

    public static void addSearchPath(final String path)
    {
        final Session session = getThreadLocal();
        session.searchPath.add(path);
    }

    public static List<String> getSearchPaths()
    {
        final Session session = getThreadLocal();
        return session.searchPath;
    }

    public static void addImplicitImport(final String script)
    {
        final Session session = getThreadLocal();
        session.implicitImports.add(script);
    }

    public static List<String> getImplicitImports()
    {
        final Session session = getThreadLocal();
        return session.implicitImports;
    }

    public static void pushCurrentModule(final String name)
    {
        final Session session = getThreadLocal();
        session.moduleStack.push(name);
    }

    public static void popCurrentModule()
    {
        final Session session = getThreadLocal();
        session.moduleStack.pop();
    }

    public static boolean inModule(final String name)
    {
        final Session session = getThreadLocal();

        for (final String module : session.moduleStack)
            if (module.equals(name))
                return true;

        return false;
    }

    public static boolean isInImplicitImport()
    {
        final Session session = getThreadLocal();
        return session.inImplicitImport;
    }

    public static void setInImplicitImport()
    {
        final Session session = getThreadLocal();
        session.inImplicitImport = true;
    }

    public static void clearInImplicitImport()
    {
        final Session session = getThreadLocal();
        session.inImplicitImport = false;
    }

    public static void error(final String msg, final Object... args)
    {
        error(null, msg, args);
    }

    public static void error(final Loc loc, final String msg, final Object... args)
    {
        final Session session = getThreadLocal();
        session.currentErrorCount++;
        session.log(loc, MESSAGE_ERROR, msg, args);
    }

    public static void warn(final String msg, final Object... args)
    {
        warn(null, msg, args);
    }

    public static void warn(final Loc loc, final String msg, final Object... args)
    {
        getThreadLocal().log(loc, MESSAGE_WARNING, msg, args);
    }

    public static void info(final String msg, final Object... args)
    {
        info(null, msg, args);
    }

    public static void info(final Loc loc, final String msg, final Object... args)
    {
        getThreadLocal().log(loc, MESSAGE_INFO, msg, args);
    }

    public static void debug(final String msg, final Object... args)
    {
        debug(null, msg, args);
    }

    public static void debug(final Loc loc, final String msg, final Object... args)
    {
        if (isDebug())
            getThreadLocal().log(loc, MESSAGE_DEBUG, msg, args);
    }

    public static boolean isDebug()
    {
        return getThreadLocal().messageLevel >= MESSAGE_DEBUG;
    }

    /**
     * Log a message at a given message level. Message string should use
     * {@link MessageFormat#format} syntax.
     */
    private void log(final Loc loc, final int level, final String message,
        final Object... args)
    {
        if (level <= this.messageLevel)
        {
            final String formatted =
                levelPrefix[level] + " " +
                    (loc != null ? loc.toString() : "") + ": " +
                    (args != null ? MessageFormat.format(message, args) : message);

            writeString(formatted);
        }
    }

    private void writeString(final String msg)
    {
        lastMessage = msg;

        if (writer != null)
        {
            try
            {
                writer.write(msg);
                writer.write("\n");
                writer.flush();
            }
            catch (IOException e)
            {
                System.err.println("IOException from writer: " + writer);
            }
        }
        else
        {
            System.out.println(msg);
            System.out.flush();
        }
    }
}
