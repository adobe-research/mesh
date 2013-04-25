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
package runtime.intrinsic.demo.processing;

import runtime.rep.lambda.IntrinsicLambda;
import runtime.rep.Symbol;

/**
 * Demo support, Processing hook
 *
 * @author Basil Hosmer
 */
public final class _prmousebutton extends IntrinsicLambda
{
    public static final _prmousebutton INSTANCE = new _prmousebutton(); 
    public static final String NAME = "prmousebutton";

    public String getName()
    {
        return NAME;
    }

    public Object apply(final Object arg)
    {
        return invoke();
    }

    private static final Symbol LEFTSYM = Symbol.get("LEFT");
    private static final Symbol CENTERSYM = Symbol.get("CENTER");
    private static final Symbol RIGHTSYM = Symbol.get("RIGHT");
    private static final Symbol EMPTYSYM = Symbol.get("");

    /**
     * CAUTION not thread safe when called outside of setup/draw func
     */
    public static Symbol invoke()
    {
        if (Processing.INSTANCE != null)
        {
            final int b = Processing.INSTANCE.mouseButton;
            return
                b == Processing.LEFT ? LEFTSYM :
                    b == Processing.CENTER ? CENTERSYM :
                        b == Processing.RIGHT ? RIGHTSYM :
                            EMPTYSYM;
        }

        return EMPTYSYM;
    }
}
