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
package compile.parse;

import compile.term.ParamBinding;
import compile.type.Type;

import java.util.List;

/**
 * Lambda value signature info.
 * This is just Pair<List<ParamBinding>, Type>. It works around
 * a problem Rats has with nested generic class names.
 *
 * @author Basil Hosmer
 */
public class SigInfo
{
    public final List<ParamBinding> params;
    public final Type rtype;

    public SigInfo(final List<ParamBinding> params, final Type rtype)
    {
        this.params = params;
        this.rtype = rtype;
    }
}
