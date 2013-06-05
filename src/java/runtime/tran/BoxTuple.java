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

import java.util.Map;

/**
 * An multi-box implementation of the Boxes class
 *
 * @author Keith McGuigan
 */
public class BoxTuple extends Boxes
{
    private Tuple boxes;

    public BoxTuple(final Tuple boxes)
    {
        super();
        this.boxes = boxes;
    }

    public Box get(final int index)
    {
        assert index >= 0 && index < size() : "Invalid box index";
        return (Box)boxes.get(index);
    }

    public int size()
    {
        return boxes.size();
    }

    public Object getValues()
    {
        return TransactionManager.gets(boxes);
    }

    public Tuple applyUpdates(
        final Object currentValue, final Map<Box,Object> updates)
    {
        final Tuple oldValues = (Tuple)currentValue;
        final Object[] newValues = new Object[size()];
        for (int i = 0; i < size(); ++i)
        {
            final Object newValue = updates.get(get(i));
            newValues[i] = newValue != null ? newValue : oldValues.get(i);
        }
        return Tuple.from(oldValues, Tuple.from(newValues));
    }
}
