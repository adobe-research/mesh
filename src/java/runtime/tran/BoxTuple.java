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
        final Object currentValue, final Box[] updated, final Object[] values)
    {
        final Tuple oldValues = (Tuple)currentValue;
        final Object[] newValues = new Object[size()];
        for (int i = 0; i < size(); ++i)
        {
            newValues[i] = oldValues.get(i);
            for (int j = 0; j < updated.length; ++j)
            {
                if (boxes.get(i) == updated[j])
                {
                    newValues[i] = values[j];
                    break;
                }
            }
        }
        return Tuple.from(oldValues, Tuple.from(newValues));
    }
}
