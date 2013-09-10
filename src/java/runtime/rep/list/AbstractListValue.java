package runtime.rep.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Abstract super for ListValue impls, adds standard impls
 * of java.Util.List methods.
 *
 * @author Basil Hosmer
 */
public abstract class AbstractListValue implements ListValue
{
    // List<Object>

    public final boolean isEmpty()
    {
        return size() > 0;
    }

    public boolean contains(final Object obj)
    {
        return find(obj) < size();
    }

    public final Iterator<Object> iterator()
    {
        return iterator(0, size());
    }

    public Object[] toArray()
    {
        final Object[] array = new Object[size()];

        int i = 0;
        for (final Object item : this)
            array[i++] = item;

        return array;
    }

    // TODO
    public <T> T[] toArray(final T[] ts)
    {
        throw new UnsupportedOperationException();
    }

    public boolean add(final Object obj)
    {
        throw new UnsupportedOperationException();
    }

    public boolean remove(final Object obj)
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(final Collection<?> objects)
    {
        for (final Object obj : objects)
            if (!contains(obj))
                return false;

        return true;
    }

    public boolean addAll(final Collection<?> objects)
    {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(final int i, final Collection<?> objects)
    {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(final Collection<?> objects)
    {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(final Collection<?> objects)
    {
        throw new UnsupportedOperationException();
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public Object set(final int i, final Object obj)
    {
        throw new UnsupportedOperationException();
    }

    public void add(final int i, final Object obj)
    {
        throw new UnsupportedOperationException();
    }

    public Object remove(final int i)
    {
        throw new UnsupportedOperationException();
    }

    public int indexOf(final Object obj)
    {
        final int i = find(obj);
        return i < size() ? i : -1;
    }

    // TODO
    public int lastIndexOf(final Object obj)
    {
        throw new UnsupportedOperationException();
    }

    public ListIterator<Object> listIterator()
    {
        throw new UnsupportedOperationException();
    }

    public ListIterator<Object> listIterator(final int i)
    {
        throw new UnsupportedOperationException();
    }

    // Object

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (obj instanceof ListValue)
        {
            final ListValue other = (ListValue)obj;

            if (size() != other.size())
                return false;

            final Iterator<?> e1 = iterator();
            final Iterator<?> e2 = other.iterator();

            while (e1.hasNext() && e2.hasNext())
                if (!e1.next().equals(e2.next()))
                    return false;

            return true;
        }
        else
        {
            return false;
        }
    }

    // Object

    @Override
    public int hashCode()
    {
        int hash = 1;

        for (final Object obj : this)
            hash = 31 * hash + obj.hashCode();

        return hash;
    }
}
