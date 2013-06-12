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

import runtime.rep.Tuple;
import runtime.rep.Lambda;

import java.util.*;

/**
 * Operations upon a box, or a set (tuple) of boxes.
 *
 * @author Keith McGuigan
 */
public abstract class Boxes implements Iterable<Box>
{
    private boolean hasReadLock;
    private boolean hasWriteLock;

    protected Boxes()
    {
        this.hasReadLock = false;
        this.hasWriteLock = false;
    }

    public static Boxes from(final Box box)
    {
        return new SingleBox(box);
    }

    public static Boxes from(final Tuple boxes)
    {
        return new BoxTuple(boxes);
    }

    private class BoxesIterator implements Iterator<Box>
    {
        private int index = 0;

        public boolean hasNext()
        {
            return index < size();
        }

        public Box next()
        {
            return get(index++);
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    public Iterator<Box> iterator()
    {
        return new BoxesIterator();
    }

    public abstract Box get(final int index);

    public abstract int size();

    // Get the current value(s) of the boxes.  If this was initiated as a
    // singleton operation, the result will be a singleton.  Otherwise it will
    // be a tuple which contains a value for each box in the operation.
    public abstract Object getValues();

    // Apply updates to a set of current values and return a (<old>,<new>)
    // tuple, where old and new are either tuples or scalars (depending on
    // whether this boxing operation started as a singleton box or a tuple of
    // boxes
    public abstract Tuple applyUpdates(
        final Object currentValues, final Box[] boxes, final Object[] values);

    public void acquireWriteLocks()
    {
        if (!hasWriteLock)
        {
            for (final Box b : this)
                b.acquireWriteLock();
            hasWriteLock = true;
        }
    }

    public void acquireReadLocks()
    {
        if (!hasReadLock && !hasWriteLock)
        {
            for (final Box b : this)
                b.acquireReadLock();
            hasReadLock = true;
        }
    }

    public void releaseWriteLocks()
    {
        if (hasWriteLock)
        {
            for (final Box b : this)
                b.releaseWriteLock();
            hasWriteLock = false;
        }
    }

    public void releaseReadLocks()
    {
        if (hasReadLock)
        {
            for (final Box b : this)
                b.releaseReadLock();
            hasReadLock = false;
        }
    }

    public void addWatcher(final Lambda key, final Watcher watcher)
    {
        for (final Box b : this)
            b.addWatcher(key, watcher);
    }

    public void removeWatcher(final Lambda action)
    {
        for (final Box b : this)
            b.removeWatcher(action);
    }
}
