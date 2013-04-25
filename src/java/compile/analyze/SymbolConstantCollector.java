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
package compile.analyze;

import compile.Session;
import compile.module.Module;
import compile.term.*;
import compile.term.SimpleLiteralTerm;
import compile.term.SymbolLiteral;
import compile.type.Type;
import compile.type.Types;

import java.util.*;

/**
 * Method {@link #collect} collects symbol constants mentioned in
 * a module.
 *
 * @author Basil Hosmer
 */
public final class SymbolConstantCollector extends ModuleVisitor<Object>
{
    private final HashSet<SymbolLiteral> symbolConstants;
    private final HashSet<List<SimpleLiteralTerm>> keysetConstants;

    public SymbolConstantCollector(final Module module)
    {
        super(module);
        symbolConstants = new HashSet<SymbolLiteral>();
        keysetConstants = new HashSet<List<SimpleLiteralTerm>>();
    }

    public boolean collect()
    {
        if (Session.isDebug())
            Session.debug(getModule().getLoc(), "Collecting symbol constants...");

        return process();
    }

    public HashSet<SymbolLiteral> getSymbolConstants()
    {
        return symbolConstants;
    }

    public HashSet<List<SimpleLiteralTerm>> getKeysetConstants()
    {
        return keysetConstants;
    }

    // TermVisitor

    @Override
    public Object visit(final SymbolLiteral symbolLiteral)
    {
        symbolConstants.add(symbolLiteral);
        return null;
    }

    @Override
    public Object visit(final RecordTerm record)
    {
        // visit substructures
        super.visit(record);

        final Type recordType = record.getType();

        final List<SimpleLiteralTerm> list = Types.recKeyList(recordType);

        if (list != null)
            keysetConstants.add(list);

        return null;
    }

}
