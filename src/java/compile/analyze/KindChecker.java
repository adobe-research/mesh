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
import compile.type.*;
import compile.type.kind.ArrowKind;
import compile.type.kind.Kind;
import compile.type.kind.Kinds;
import compile.type.kind.TupleKind;
import compile.type.visit.TypeVisitorBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Type expression analyzer. {@link #check} method derives kinds
 * of variable-kinded type expressions, and checks agreement and
 * sets kinds of type applications.
 *
 * @author Basil Hosmer
 */
public final class KindChecker extends TypeVisitorBase<Object>
{
    private static KindChecker INSTANCE = new KindChecker();

    public static void check(final Type type)
    {
        INSTANCE.visitType(type);
    }

    /**
     * check kind agreement, set result kind
     */
    @Override
    public Object visit(final TypeApp app)
    {
        visitType(app.getBase());
        final Type base = app.getBase().deref();
        final Kind baseKind = base.getKind();

        if (baseKind instanceof ArrowKind)
        {
            final ArrowKind arrowKind = (ArrowKind)baseKind;
            final Kind paramKind = arrowKind.getParamKind();
            final Kind resultKind = arrowKind.getResultKind();

            // NOTE: set app kind optimistically, in case of cycles
            app.setKind(resultKind);

            visitType(app.getArg());

            final Type arg = app.getArg();
            final Kind argKind = arg.getKind();

            // TODO fix
            if (arg instanceof TypeRef && ((TypeRef)arg).getBinding() == null)
            {
                // Unresolved reference.  Don't know the kind yet so we can't check it
                return null;
            }

            if (!argKind.equals(paramKind))
            {
                Session.error(app.getLoc(),
                    "type constructor {0} takes type param of kind {1}, but argument {2} has kind {3}",
                    base.dump(), paramKind.dump(), arg.dump(), argKind.dump());
            }
        }
        else
        {
            Session.error(base.getLoc(),
                "{0} is not a type constructor, cannot have arguments applied to it",
                base.dump());

            app.setKind(Kinds.STAR);
        }

        return null;
    }

    /**
     * set kind after visiting members
     */
    @Override
    public Object visit(final TypeTuple tuple)
    {
        super.visit(tuple);

        final List<Type> members = tuple.getMembers();
        final List<Kind> memberKinds = new ArrayList<Kind>(members.size());

        for (final Type member : members)
            memberKinds.add(member.getKind());

        tuple.setKind(new TupleKind(tuple.getLoc(), memberKinds));

        return null;
    }
}
