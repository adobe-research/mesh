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
package runtime.rep;

/**
 * All runtime lambdas implement this interface.
 * In addition to apply(), which takes a single (possibly
 * tupled) argument, each lambda must also implement an
 * invoke() whose signature conforms to its scattered arg list.
 * invoke() should be static if the lambda has no captured bindings.
 * invoke() is called in generated code in contexts where a
 * strongly-typed reference to the lambda is available. (Also,
 * standard practice is to have apply() always delegate to
 * invoke()).
 *
 * @author Basil Hosmer
 */
public interface Lambda
{
    Object apply(Object x);
}
