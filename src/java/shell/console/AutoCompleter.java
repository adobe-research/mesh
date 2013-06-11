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
package shell.console;

import compile.gen.Unit;
import compile.term.LetBinding;
import jline.console.completer.Completer;
import shell.ShellScriptManager;

import java.util.LinkedHashMap;
import java.util.List;

class AutoCompleter implements Completer
{
    private ShellScriptManager shellScriptManager;

    AutoCompleter(final ShellScriptManager ssm)
    {
        this.shellScriptManager = ssm;
    }

    private static boolean isValidCompleteLocation(final String buffer, final int cursor)
    {
        return buffer != null && cursor > 0 && cursor - 1 < buffer.length() &&
            Character.isLetterOrDigit(buffer.charAt(cursor - 1));
    }

    private static int findBeginningOfWord(final String buffer, final int cursor)
    {
        int begin = cursor - 1;
        while (begin > 0)
        {
            final char letter = buffer.charAt(begin - 1);
            if (!Character.isLetterOrDigit(letter))
            {
                break;
            }
            --begin;
        }
        return begin;
    }

    private static void findCandidatesInLets(final String fragment,
        final LinkedHashMap<String, LetBinding> lets, final List<CharSequence> candidates)
    {
        for (final String name : lets.keySet())
        {
            if (name.startsWith(fragment) && !name.equals(fragment))
            {
                candidates.add(name);
            }
        }
    }

    private void findCandidatesFor(final String fragment,
        final List<CharSequence> candidates)
    {
        for (final Unit unit : shellScriptManager.getUnitHistory())
        {
            findCandidatesInLets(fragment, unit.getModule().getLets(), candidates);
        }
        // TODO: look for type names and record keys that might match too
    }

    // ConsoleReader calls this when user hits tab for auto-complete
    public int complete(final String buffer, final int cursor,
        final List<CharSequence> candidates)
    {
        if (isValidCompleteLocation(buffer, cursor))
        {
            final int begin = findBeginningOfWord(buffer, cursor);

            final String fragment = buffer.substring(begin, cursor);
            findCandidatesFor(fragment, candidates);

            if (candidates.size() > 0)
            {
                return begin;
            }
        }
        return -1; // no autocomplete for location or no candidates
    }
}
