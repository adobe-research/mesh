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
package compile.term.visit;

import compile.term.*;

/**
 * Term visitor interface, parameterized by return type.
 *
 * @author Basil Hosmer
 */
public interface TermVisitor<T>
{
    T visit(RefTerm ref);

    T visit(ParamValue paramValue);

    T visit(BoolLiteral boolConstant);

    T visit(IntLiteral intConstant);

    T visit(LongLiteral longConstant);

    T visit(DoubleLiteral doubleConstant);

    T visit(StringLiteral stringConstant);

    T visit(SymbolLiteral symbolConstant);

    T visit(ListTerm list);

    T visit(MapTerm map);

    T visit(TupleTerm tuple);

    T visit(RecordTerm record);

    T visit(VariantTerm variant);

    T visit(CondTerm select);

    T visit(LambdaTerm lambda);

    T visit(ApplyTerm apply);

    T visit(CoerceTerm coerce);
}
