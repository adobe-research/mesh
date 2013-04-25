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
package compile.type.visit;

import compile.term.Term;
import compile.term.TypeBinding;
import compile.type.*;
import compile.type.kind.*;

import java.util.Map;
import java.util.Set;

/**
 * Dump a string representation of a type in tree form.
 *
 * @author Keith McGuigan
 */
public final class DebugDumper extends StackedTypeVisitor<Void>
{
    private final StringBuilder sb;
    private int indent;
    private boolean needsListSep;
    private boolean firstField;
    private boolean verboseKind;

    private DebugDumper()
    {
        this.sb = new StringBuilder();
        this.indent = 0;
        this.needsListSep = false;
        this.firstField = true;
        this.verboseKind = false;
    }

    public String toString() 
    {
        return sb.toString();
    }

    public static String dump(final Type type)
    {
        if (type == null)
        {
            return "NULL";
        }
        else
        {
            final DebugDumper dd = new DebugDumper();
            dd.visitType(type);
            return dd.toString();
        }
    }

    private void newline() 
    {
        sb.append("\n");
        for (int i = 0; i < indent; ++i) {
            sb.append(" ");
        }
    }

    private void begin(final String id)
    {
        sb.append(id);
        sb.append("(");
        indent += 2;
        firstField = true;
    }

    private void end() 
    {
        indent -= 2;
        newline();
        sb.append(")");
    }

    private void field(final String id)
    {
        if (!firstField) 
        {
            sb.append(",");
        }
        firstField = false;
        newline();
        sb.append(id);
        sb.append(":");
    }

    private void listbegin() 
    {
        sb.append("[");
        needsListSep = false;
        indent += 2;
    }

    private void listsep()
    {
        if (needsListSep) 
        {
            sb.append(",");
        }
        newline();
        needsListSep = true;
    }

    private void listend()
    {
        indent -= 2;
        newline();
        sb.append("]");
    }

    private void addKind(final Kind k) 
    {
        if (verboseKind) 
        {
           field("kind");
           visit(k);
        }
        else
        {
            if (k != null) 
                sb.append(k.dump());
            else
                sb.append("NULL_KIND");
        }
    }

    private void visit(final Kind k) 
    {
        if (k instanceof ArrowKind) 
        {
            visit((ArrowKind)k);
        }
        else if (k instanceof TupleKind) 
        {
            visit((TupleKind)k);
        }
        else if (k == Kinds.STAR) 
        {
            sb.append("STAR");
        }
        else if (k == Kinds.STAR_LIST)
        {
            sb.append("STAR_LIST");
        }
        else if (k == Kinds.STAR_MAP)
        {
            sb.append("STAR_MAP");
        }
        else
        {
            sb.append("UNKNOWN_KIND");
        }
    }

    private void visit(final ArrowKind k)
    {
        begin("ARROW_KIND");
        field("param");
        visit(k.getParamKind());
        field("result");
        visit(k.getResultKind());
        end();
    }

    private void visit(final TupleKind k)
    {
        begin("TUPLE_KIND");
        listbegin();
        for (final Kind tk : k.getMembers())
        {
            listsep();
            visit(tk);
        }
        listend();
        end();
    }

    @Override
    protected Void visitType(final Type type)
    {
        if (type == null) 
        {
            sb.append("<null>");
        }
        else
        {
            if (type.hasParams()) 
            {
                sb.append("(TypeParams: ");
                listbegin();
                for (final Map.Entry<String,TypeParam> param : type.getParams().entrySet())
                {
                    listsep();
                    visit(param.getValue());
                }
                listend();
                sb.append(" => ");
                super.visitType(type);
                sb.append(" )");
            }
            else
            {
                super.visitType(type);
            }
        }
        return null;
    }

    @Override
    public Void visit(final TypeParam param) 
    {
        begin("TypeParam");
        addKind(param.getKind());
        field("name");
        sb.append(param.getName());
        end();
        return null;
    }

    @Override
    public Void visit(final WildcardType wildcard)
    {
        sb.append("WildcardType");
        return null;
    }

    public void visit(final ChoiceType choice) 
    {
        begin("ChoiceType");
        visit((EnumType)choice);
        end();
    }
    
    @Override
    public Void visit(final EnumType enumType)
    {
        begin("EnumType");
        field("base");
        visitType(enumType.getBaseType());
        field("values");
        listbegin();
        for (final Term term : enumType.getValues())
        {
            listsep();
            sb.append(term.dump());
        }
        listend();
        end();
        return null;
    }

    public void visit(final ExtentType extentType) 
    {
        begin("ExtentType");
        visit((EnumType)extentType);
        field("size");
        sb.append(extentType.getSize());
        end();
    }

    @Override
    public Void visit(final TypeApp app)
    {
        begin("TypeApp");
        addKind(app.getKind());
        field("base");
        visitType(app.getBase());
        field("arg");
        visitType(app.getArg());
        end();
        return null;
    }

    @Override
    public Void visit(final TypeCons cons)
    {
        begin("TypeCons");
        addKind(cons.getKind());
        field("name");
        sb.append(cons.getName());
        field("body");
        if (cons.isAbs()) 
        {
            sb.append("<abstract>");
        }
        else
        {
            visitType(cons.getBody());
        }
        end();
        return null;
    }

    @Override
    public Void visit(final TypeList list)
    {
        begin("TypeList");
        listbegin();
        for (final Type t : list.getItems())
        {
            listsep();
            visitType(t);
        }
        listend();
        end();
        return null;
    }

    @Override
    public Void visit(final TypeMap map)
    {
        begin("TypeMap");
        field("keytype");
        visitType(map.getKeyType());
        field("members");
        listbegin();
        for (final Map.Entry<Term,Type> entry : map.getMembers().entrySet())
        {
            listsep();
            sb.append(entry.getKey().dump());
            sb.append(" : ");
            visitType(entry.getValue());
        }
        listend();
        end();
        return null;
    }

    @Override
    public Void visit(final TypeRef ref)
    {
        final TypeBinding tb = ref.getBinding();
        begin("TypeRef");
        if (tb != null) 
        {
            addKind(tb.getKind());
        }
        field("name");
        sb.append(ref.getName());
        field("binding");
        if (tb == null) 
        {
            sb.append("<unresolved>");
        } 
        else
        {
            sb.append(tb.getName());
        }
        end();
        return null;
    }

    @Override
    public Void visit(final TypeTuple tuple)
    {
        begin("TypeTuple");
        listbegin();
        for (final Type t : tuple.getMembers())
        {
            listsep();
            visitType(t);
        }
        listend();
        end();
        return null;
    }

    @Override
    public Void visit(final TypeVar var)
    {
        begin("TypeVar");
        addKind(var.getKind());
        field("name");
        sb.append(var.getName());
        if (var.hasSourceParam()) 
        {
            field("source");
            visit(var.getSourceParam());
        }
        final Set<TypeParam> unified = var.getUnifiedParams();
        if (!unified.isEmpty()) 
        {
            field("unified");
            listbegin();
            for (final TypeParam tp : unified)
            {
                listsep();
                visit(tp);
            }
            listend();
        }
        end();
        return null;
    }
}
