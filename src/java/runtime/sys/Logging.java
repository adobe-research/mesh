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
package runtime.sys;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Brute-force multithreaded runtime debug messages
 *
 * @author Basil Hosmer
 */
public final class Logging
{
    public static final Logger
        LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).getParent();

    static
    {
        while (LOGGER.getHandlers().length > 0)
            LOGGER.removeHandler(LOGGER.getHandlers()[0]);

        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new MicroFormatter());
        consoleHandler.setLevel(Level.FINE);
        LOGGER.addHandler(consoleHandler);
    }

    private static String lineSeparator = System.getProperty("line.separator");

    /**
     * Log a single line, no timestamp or classname
     */
    public static class MicroFormatter extends Formatter
    {
        public synchronized String format(final LogRecord record)
        {
            final StringBuilder sb = new StringBuilder();
            final String message = formatMessage(record);
            return sb.append(record.getLevel().getLocalizedName()).
                    append(": ").
                    append(message).
                    append(lineSeparator).
                    toString();
        }
    }

    private static final AtomicBoolean debug = new AtomicBoolean(false);
    private static final AtomicInteger errorCount = new AtomicInteger();

    public static boolean setDebug(final boolean debug)
    {
        Logging.LOGGER.setLevel(debug ? Level.FINE : Level.INFO);
        return Logging.debug.getAndSet(debug);
    }

    public static boolean isDebug()
    {
        return debug.get();
    }

    public static int getErrorCount()
    {
        return errorCount.get();
    }

    public static void debug(final String msg, final Object... args)
    {
        if (debug.get())
            LOGGER.fine("thread " + Thread.currentThread().getId() + " " +
                                MessageFormat.format(msg, args));
    }

    public static void info(final String msg, final Object... args)
    {
        LOGGER.info("thread " + Thread.currentThread().getId() + " " +
            MessageFormat.format(msg, args));
    }

    public static void warning(final String msg, final Object... args)
    {
        LOGGER.warning("thread " + Thread.currentThread().getId() + " " +
            MessageFormat.format(msg, args));
    }

    public static void error(final String msg, final Object... args)
    {
        errorCount.incrementAndGet();
        LOGGER.severe("thread " + Thread.currentThread().getId() + " " +
            MessageFormat.format(msg, args));
    }
}
