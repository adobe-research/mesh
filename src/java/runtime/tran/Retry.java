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
package runtime.tran;

/**
 * Error object with optional transaction failure info.
 *
 * @author Basil Hosmer
 */
@SuppressWarnings("serial")
class Retry extends Error
{
    /**
     * the attempt that caused us to retry, if any
     */
    final Attempt blocker;

    /**
     * true if we were successfully queued before
     * retry exception was thrown
     */
    final boolean queued;

    /**
     * 
     */
    Retry(final Attempt blocker, final boolean queued)
    {
        this.blocker = blocker;
        this.queued = queued;
    }
    
    /**
     *
     */
    Retry()
    {
        this(null, false);
    }
}
