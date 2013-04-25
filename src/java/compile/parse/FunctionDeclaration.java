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

import compile.Loc;
import compile.term.ParamBinding;
import compile.type.Type;
import compile.type.TypeParam;

import java.util.List;

/**
 * Simple container for function declaration items parsed out
 * of the source code
 */
public class FunctionDeclaration {
    public final Loc loc;
    public final String name;
    public final List<TypeParam> typeParams;
    public final List<ParamBinding> params;
    public final Type returnType;

    public FunctionDeclaration(
            final Loc loc, final String name, final List<TypeParam> typeParams,
            final List<ParamBinding> params, final Type returnType) {
        this.loc = loc;
        this.name = name;
        this.typeParams = typeParams;
        this.params = params;
        this.returnType = returnType;
    }
}
