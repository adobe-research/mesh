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
package runtime;

import java.util.ArrayDeque;

/**
 * Basic debug hook. Generated code should push, pop activation
 * descriptors and set statement descriptors.
 * Part of a dormant effort to build some basic debugging
 * infrastructure, not in a very useful state currently.
 *
 * @author Basil Hosmer
 */
public final class DebugWatcher
{
    public static final ThreadLocal<DebugWatcher> LOCAL = new ThreadLocal<DebugWatcher>()
    {
        protected DebugWatcher initialValue()
        {
            return new DebugWatcher();
        }
    };

    public static DebugWatcher getThreadLocal()
    {
        return LOCAL.get();
    }

    public static ArrayDeque<String> getActivationStack()
    {
        final DebugWatcher threadLocal = getThreadLocal();
        return threadLocal.getActStack();
    }

    public static void setLocation(final String location)
    {
        final DebugWatcher threadLocal = getThreadLocal();
        threadLocal.setLoc(location);
    }

    public static void pushActivation()
    {
        final DebugWatcher threadLocal = getThreadLocal();
        threadLocal.pushAct();
    }

    public static void popActivation()
    {
        final DebugWatcher threadLocal = getThreadLocal();
        threadLocal.popAct();
    }

    // instance

    private ArrayDeque<String> actStack;

    public DebugWatcher()
    {
        actStack = new ArrayDeque<String>();
        actStack.push("");  // needed?
    }

    private ArrayDeque<String> getActStack()
    {
        return actStack;
    }

    private void setLoc(final String loc)
    {
        actStack.pop();
        actStack.push(loc);
    }

    private void pushAct()
    {
        actStack.push("");
    }

    private void popAct()
    {
        actStack.pop();
    }
}
