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

import compile.gen.java.Constants;
import compile.type.Types;
import runtime.intrinsic.*;
import runtime.intrinsic.tran.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mesh operator precedence and associativity info.
 * Note: defined symbols need to agree with MeshParser.rats
 *
 * @author Basil Hosmer
 */
public class Ops
{
    /**
     * default binary op info, used by {@link ASTBuilder}.
     */
    static final BinopInfo DEFAULT_BINOP_INFO = new BinopInfo(0, Assoc.Left, null);

    // binary operator symbols

    final static String OR_SYM = "||";
    final static String AND_SYM = "&&";

    final static String EQ_SYM = "==";
    final static String NE_SYM = "!=";
    final static String GT_SYM = ">";
    final static String GE_SYM = ">=";
    final static String LE_SYM = "<=";
    final static String LT_SYM = "<";
    // TODO remove (overloading)
    final static String FGT_SYM = ">.";
    final static String FGE_SYM = ">=.";
    final static String FLE_SYM = "<=.";
    final static String FLT_SYM = "<.";

    final static String PLUS_SYM = "+";
    final static String MINUS_SYM = "-";
    final static String TIMES_SYM = "*";
    final static String DIV_SYM = "/";
    final static String MOD_SYM = "%";
    final static String EXP_SYM = "^";
    // TODO remove (overloading)
    final static String FPLUS_SYM = "+.";
    final static String FMINUS_SYM = "-.";
    final static String FTIMES_SYM = "*.";
    final static String FDIV_SYM = "/.";
    final static String FMOD_SYM = "%.";
    final static String FEXP_SYM = "^.";

    final static String SHR_SYM = ">>";
    final static String USHR_SYM = ">>>";
    final static String SHL_SYM = "<<";

    final static String PIPE_SYM = "|";
    final static String PPIPE_SYM = "|:";

    // from here down are experimental/unfinished

    final static String ARROW_SYM = "->";
    final static String COMPOSE_SYM = "$";

    final static String ASSIGN_SYM = ":=";
    final static String ASSIGNS_SYM = "::=";

    final static String UPDATE_SYM = "<-";
    final static String UPDATES_SYM = "<<-";

    final static String COND_SYM = "?";
    final static String VAR_SYM = "!";

    final static String DOUBLE_TIMES_SYM = "**";

    /**
     * binary (value) operator info table
     * entries with null BinopInfo.name are handled specially,
     * e.g. translated into special AST nodes, or give NYS error
     */
    public static final Map<String, BinopInfo> BINOP_INFO =
        new HashMap<String, BinopInfo>();

    // populate BINOP_INFO. Entries are added in low-to-high precedence order.
    // Ops that desugar to intrinsics have BinopInfo name entries that refer
    // to the static NAME field of the implementing class; others desugar to
    // Mesh functions currently defined in lang.m and have name fields that
    // are defined in Constants.
    //
    static
    {
        BINOP_INFO.put(VAR_SYM, new BinopInfo(1, Assoc.Right, VAR_SYM));

        BINOP_INFO.put(ASSIGN_SYM, new BinopInfo(5, Assoc.Right, _put.INSTANCE.getName()));
        BINOP_INFO.put(ASSIGNS_SYM, new BinopInfo(5, Assoc.Right, _puts.INSTANCE.getName()));

        BINOP_INFO.put(UPDATE_SYM, new BinopInfo(5, Assoc.Right, _update.INSTANCE.getName()));
        BINOP_INFO.put(UPDATES_SYM, new BinopInfo(5, Assoc.Right, _updates.INSTANCE.getName()));

        BINOP_INFO.put(OR_SYM, new BinopInfo(20, Assoc.Left, _or.INSTANCE.getName()));
        BINOP_INFO.put(AND_SYM, new BinopInfo(25, Assoc.Left, _and.INSTANCE.getName()));

        BINOP_INFO.put(EQ_SYM, new BinopInfo(40, Assoc.Left, _eq.INSTANCE.getName()));
        BINOP_INFO.put(NE_SYM, new BinopInfo(40, Assoc.Left, _ne.INSTANCE.getName()));

        BINOP_INFO.put(GT_SYM, new BinopInfo(50, Assoc.Left, _gt.INSTANCE.getName()));
        BINOP_INFO.put(GE_SYM, new BinopInfo(50, Assoc.Left, _ge.INSTANCE.getName()));
        BINOP_INFO.put(LE_SYM, new BinopInfo(50, Assoc.Left, _le.INSTANCE.getName()));
        BINOP_INFO.put(LT_SYM, new BinopInfo(50, Assoc.Left, _lt.INSTANCE.getName()));

        // TODO remove
        BINOP_INFO.put(FGT_SYM, new BinopInfo(50, Assoc.Left, _fgt.INSTANCE.getName()));
        BINOP_INFO.put(FGE_SYM, new BinopInfo(50, Assoc.Left, _fge.INSTANCE.getName()));
        BINOP_INFO.put(FLE_SYM, new BinopInfo(50, Assoc.Left, _fle.INSTANCE.getName()));
        BINOP_INFO.put(FLT_SYM, new BinopInfo(50, Assoc.Left, _flt.INSTANCE.getName()));

        BINOP_INFO.put(SHR_SYM, new BinopInfo(55, Assoc.Left, _shiftr.INSTANCE.getName()));
        BINOP_INFO.put(USHR_SYM, new BinopInfo(55, Assoc.Left, _ushiftr.INSTANCE.getName()));
        BINOP_INFO.put(SHL_SYM, new BinopInfo(55, Assoc.Left, _shiftl.INSTANCE.getName()));

        BINOP_INFO.put(PLUS_SYM, new BinopInfo(60, Assoc.Left, _plus.INSTANCE.getName()));
        BINOP_INFO.put(MINUS_SYM, new BinopInfo(60, Assoc.Left, _minus.INSTANCE.getName()));

        // TODO remove
        BINOP_INFO.put(FPLUS_SYM, new BinopInfo(60, Assoc.Left, _plus.INSTANCE.getName()));
        BINOP_INFO.put(FMINUS_SYM, new BinopInfo(60, Assoc.Left, _fminus.INSTANCE.getName()));

        BINOP_INFO.put(TIMES_SYM, new BinopInfo(70, Assoc.Left, _times.INSTANCE.getName()));
        BINOP_INFO.put(DIV_SYM, new BinopInfo(70, Assoc.Left, _div.INSTANCE.getName()));
        BINOP_INFO.put(MOD_SYM, new BinopInfo(70, Assoc.Left, _mod.INSTANCE.getName()));

        // TODO remove
        BINOP_INFO.put(FTIMES_SYM, new BinopInfo(70, Assoc.Left, _ftimes.INSTANCE.getName()));
        BINOP_INFO.put(FDIV_SYM, new BinopInfo(70, Assoc.Left, _fdiv.INSTANCE.getName()));
        BINOP_INFO.put(FMOD_SYM, new BinopInfo(70, Assoc.Left, _fmod.INSTANCE.getName()));

        BINOP_INFO.put(EXP_SYM, new BinopInfo(80, Assoc.Left, _pow.INSTANCE.getName()));

        // TODO remove
        BINOP_INFO.put(FEXP_SYM, new BinopInfo(80, Assoc.Left, _fpow.INSTANCE.getName()));

        BINOP_INFO.put(PIPE_SYM, new BinopInfo(90, Assoc.Left, _map.INSTANCE.getName()));
        BINOP_INFO.put(PPIPE_SYM, new BinopInfo(90, Assoc.Left, _pmap.INSTANCE.getName()));

        // compose is tighter than map, so list | f $ g == list | (f $ g)
        BINOP_INFO.put(COMPOSE_SYM, new BinopInfo(100, Assoc.Left, Constants.COMPOSE_FN));
    }

    /**
     * binary type operator info table
     */
    public static final Map<String, BinopInfo> TYPE_BINOP_INFO =
        new HashMap<String, BinopInfo>();

    static
    {
        TYPE_BINOP_INFO.put(ARROW_SYM, new BinopInfo(50, Assoc.Right, Types.FUN.name));
        TYPE_BINOP_INFO.put(PIPE_SYM, new BinopInfo(100, Assoc.Left, Types.TMAP.name));
    }

    // unary operator symbols

    final static String GET_SYM = "*";

    final static String UMINUS_SYM = "-";
    final static String NOT_SYM = "!";

    // TODO remove
    final static String FUMINUS_SYM = "-.";

    // unary (value) operator info

    public static final Map<String, String> UNOP_INFO = new HashMap<String, String>();

    static
    {
        UNOP_INFO.put(TIMES_SYM, _get.INSTANCE.getName());
        UNOP_INFO.put(DOUBLE_TIMES_SYM, _gets.INSTANCE.getName());

        UNOP_INFO.put(UMINUS_SYM, _neg.INSTANCE.getName());
        UNOP_INFO.put(NOT_SYM, _not.INSTANCE.getName());

        // TODO remove
        UNOP_INFO.put(FUMINUS_SYM, _fneg.INSTANCE.getName());
    }

    // unary type operator info

    public static final Map<String, String> TYPE_UNOP_INFO = new HashMap<String, String>();

    static
    {
        TYPE_UNOP_INFO.put(COND_SYM, Types.SUM.name);
        TYPE_UNOP_INFO.put(TIMES_SYM, Types.BOX.name);
    }
}
