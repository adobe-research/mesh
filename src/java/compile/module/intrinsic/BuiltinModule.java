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
package compile.module.intrinsic;

import compile.Loc;
import compile.analyze.KindChecker;
import compile.module.Module;
import compile.term.LetBinding;
import compile.term.TypeDef;
import compile.type.Type;
import compile.type.Types;
import runtime.intrinsic.*;
import runtime.intrinsic.demo.*;
import runtime.intrinsic.demo.processing.*;
import runtime.intrinsic.demo.socket.Accept;
import runtime.intrinsic.demo.socket.Close;
import runtime.intrinsic.demo.socket.Closed;
import runtime.intrinsic.demo.socket.SSocket;
import runtime.intrinsic.log.*;
import runtime.intrinsic.test.*;
import runtime.intrinsic.tran.*;

/**
 * Temporary: handwritten bindings to intrinsic types and functions.
 * The other moving parts of the current, temporary intrinsics setup
 * are {@link compile.gen.java.IntrinsicUnit}, which represents the
 * intermediate form of these definitions within the compiler,
 * and {@link runtime.intrinsic.Intrinsics}, the runtime definitions.
 * 
 * This class contains intrinsics which can be declared in source using
 * an "intrinsic" statement.  A few functions remain in IntrinsicModule
 * because their types are not yet expressible in source.
 * TODO: move those remaining functions from IntrinsicModule to here 
 * (or simply unify the modules).  Also figure out what to do with the 
 * typedefs.
 */
public final class BuiltinModule extends Module
{
    public static final String NAME = "Builtin";

    public static final BuiltinModule INSTANCE = new BuiltinModule();

    private BuiltinModule()
    {
        super(null, NAME, null, null);
        addLets();
    }

    /**
     * bind intrinsic values
     */
    private void addLets()
    {
        // logic
        addIntrinsicLet(And.NAME, And.TYPE);
        addIntrinsicLet(Or.NAME, Or.TYPE);
        addIntrinsicLet(Not.NAME, Not.TYPE);

        // int relops
        addIntrinsicLet(EQ.NAME, EQ.TYPE);
        addIntrinsicLet(NE.NAME, NE.TYPE);
        addIntrinsicLet(GT.NAME, GT.TYPE);
        addIntrinsicLet(GE.NAME, GE.TYPE);
        addIntrinsicLet(LT.NAME, LT.TYPE);
        addIntrinsicLet(LE.NAME, LE.TYPE);

        // int arith ops
        addIntrinsicLet(Plus.NAME, Plus.TYPE);
        addIntrinsicLet(Minus.NAME, Minus.TYPE);
        addIntrinsicLet(Times.NAME, Times.TYPE);
        addIntrinsicLet(Div.NAME, Div.TYPE);
        addIntrinsicLet(Max.NAME, Max.TYPE);
        addIntrinsicLet(Min.NAME, Min.TYPE);
        addIntrinsicLet(Mod.NAME, Mod.TYPE);
        addIntrinsicLet(Neg.NAME, Neg.TYPE);
        addIntrinsicLet(Pow.NAME, Pow.TYPE);
        addIntrinsicLet(Sign.NAME, Sign.TYPE);

        // int bitwise ops
        addIntrinsicLet(Band.NAME, Band.TYPE);
        addIntrinsicLet(Bor.NAME, Bor.TYPE);

        // long arith ops
        addIntrinsicLet(LMinus.NAME, LMinus.TYPE);

        // fp relops
        addIntrinsicLet(FGT.NAME, FGT.TYPE);
        addIntrinsicLet(FGE.NAME, FGE.TYPE);
        addIntrinsicLet(FLT.NAME, FLT.TYPE);
        addIntrinsicLet(FLE.NAME, FLE.TYPE);

        // fp arith ops
        addIntrinsicLet(FMinus.NAME, FMinus.TYPE);
        addIntrinsicLet(FTimes.NAME, FTimes.TYPE);
        addIntrinsicLet(FDiv.NAME, FDiv.TYPE);
        addIntrinsicLet(FMod.NAME, FMod.TYPE);
        addIntrinsicLet(FNeg.NAME, FNeg.TYPE);
        addIntrinsicLet(FPow.NAME, FPow.TYPE);

        // other math
        addIntrinsicLet(ATan2.NAME, ATan2.TYPE);
        addIntrinsicLet(Cos.NAME, Cos.TYPE);
        addIntrinsicLet(Draw.NAME, Draw.TYPE);
        addIntrinsicLet(Exp.NAME, Exp.TYPE);
        addIntrinsicLet(F2I.NAME, F2I.TYPE);
        addIntrinsicLet(FRand.NAME, FRand.TYPE);
        addIntrinsicLet(Ln.NAME, Ln.TYPE);
        addIntrinsicLet(Rand.NAME, Rand.TYPE);
        addIntrinsicLet(Sin.NAME, Sin.TYPE);
        addIntrinsicLet(Sqrt.NAME, Sqrt.TYPE);
        addIntrinsicLet(Tan.NAME, Tan.TYPE);

        // string
        addIntrinsicLet(EndsWith.NAME, EndsWith.TYPE);
        addIntrinsicLet(StartsWith.NAME, StartsWith.TYPE);
        addIntrinsicLet(StrCat.NAME, StrCat.TYPE);
        addIntrinsicLet(StrCmp.NAME, StrCmp.TYPE);
        addIntrinsicLet(StrCut.NAME, StrCut.TYPE);
        addIntrinsicLet(StrDrop.NAME, StrDrop.TYPE);
        addIntrinsicLet(StrFind.NAME, StrFind.TYPE);
        addIntrinsicLet(StrJoin.NAME, StrJoin.TYPE);
        addIntrinsicLet(StrLen.NAME, StrLen.TYPE);
        addIntrinsicLet(StrSplit.NAME, StrSplit.TYPE);
        addIntrinsicLet(StrTake.NAME, StrTake.TYPE);
        addIntrinsicLet(StrWhere.NAME, StrWhere.TYPE);
        addIntrinsicLet(Substr.NAME, Substr.TYPE);
        addIntrinsicLet(ToLower.NAME, ToLower.TYPE);
        addIntrinsicLet(ToUpper.NAME, ToUpper.TYPE);

        // converters
        addIntrinsicLet(B2I.NAME, B2I.TYPE);
        addIntrinsicLet(F2S.NAME, F2S.TYPE);
        addIntrinsicLet(I2B.NAME, I2B.TYPE);
        addIntrinsicLet(I2F.NAME, I2F.TYPE);
        addIntrinsicLet(I2S.NAME, I2S.TYPE);
        addIntrinsicLet(L2F.NAME, L2F.TYPE);
        addIntrinsicLet(L2I.NAME, L2I.TYPE);
        addIntrinsicLet(L2S.NAME, L2S.TYPE);
        addIntrinsicLet(S2F.NAME, S2F.TYPE);
        addIntrinsicLet(S2I.NAME, S2I.TYPE);
        addIntrinsicLet(S2L.NAME, S2L.TYPE);
        addIntrinsicLet(S2Sym.NAME, S2Sym.TYPE);
        addIntrinsicLet(Sym2S.NAME, Sym2S.TYPE);
        addIntrinsicLet(ToStr.NAME, ToStr.TYPE);

        // map/compose
        addIntrinsicLet(CompL.NAME, CompL.TYPE);
        addIntrinsicLet(CompM.NAME, CompM.TYPE);
        addIntrinsicLet(Map.NAME, Map.TYPE);
        addIntrinsicLet(MapLL.NAME, MapLL.TYPE);
        addIntrinsicLet(MapLM.NAME, MapLM.TYPE);
        addIntrinsicLet(MapMF.NAME, MapMF.TYPE);
        addIntrinsicLet(MapML.NAME, MapML.TYPE);
        addIntrinsicLet(MapMM.NAME, MapMM.TYPE);
        addIntrinsicLet(Mapz.NAME, Mapz.TYPE);

        // other h/o
        addIntrinsicLet(Cycle.NAME, Cycle.TYPE);
        addIntrinsicLet(CycleN.NAME, CycleN.TYPE);
        addIntrinsicLet(Filter.NAME, Filter.TYPE);
        addIntrinsicLet(For.NAME, For.TYPE);
        addIntrinsicLet(Guard.NAME, Guard.TYPE);
        addIntrinsicLet(If.NAME, If.TYPE);
        addIntrinsicLet(Iif.NAME, Iif.TYPE);
        addIntrinsicLet(PFor.NAME, PFor.TYPE);
        addIntrinsicLet(Reduce.NAME, Reduce.TYPE);
        addIntrinsicLet(EvolveWhile.NAME, EvolveWhile.TYPE);
        addIntrinsicLet(Scan.NAME, Scan.TYPE);
        addIntrinsicLet(When.NAME, When.TYPE);
        addIntrinsicLet(Where.NAME, Where.TYPE);
        addIntrinsicLet(While.NAME, While.TYPE);

        // list
        addIntrinsicLet(Append.NAME, Append.TYPE);
        addIntrinsicLet(Count.NAME, Count.TYPE);
        addIntrinsicLet(Cut.NAME, Cut.TYPE);
        addIntrinsicLet(Distinct.NAME, Distinct.TYPE);
        addIntrinsicLet(Drop.NAME, Drop.TYPE);
        addIntrinsicLet(Find.NAME, Find.TYPE);
        addIntrinsicLet(First.NAME, First.TYPE);
        addIntrinsicLet(Flatten.NAME, Flatten.TYPE);
        addIntrinsicLet(FromTo.NAME, FromTo.TYPE);
        addIntrinsicLet(Group.NAME, Group.TYPE);
        addIntrinsicLet(IsIndex.NAME, IsIndex.TYPE);
        addIntrinsicLet(Last.NAME, Last.TYPE);
        addIntrinsicLet(LPlus.NAME, LPlus.TYPE);
        addIntrinsicLet(Range.NAME, Range.TYPE);
        addIntrinsicLet(Remove.NAME, Remove.TYPE);
        addIntrinsicLet(Rep.NAME, Rep.TYPE);
        addIntrinsicLet(Rest.NAME, Rest.TYPE);
        addIntrinsicLet(ListSet.NAME, ListSet.TYPE);
        addIntrinsicLet(ListSets.NAME, ListSets.TYPE);
        addIntrinsicLet(Shuffle.NAME, Shuffle.TYPE);
        addIntrinsicLet(Size.NAME, Size.TYPE);
        addIntrinsicLet(Take.NAME, Take.TYPE);
        addIntrinsicLet(Unique.NAME, Unique.TYPE);
        addIntrinsicLet(Unzip.NAME, Unzip.TYPE);
        addIntrinsicLet(Zip.NAME, Zip.TYPE);

        // map
        addIntrinsicLet(Assoc.NAME, Assoc.TYPE);
        addIntrinsicLet(Entries.NAME, Entries.TYPE);
        addIntrinsicLet(IsKey.NAME, IsKey.TYPE);
        addIntrinsicLet(Keys.NAME, Keys.TYPE);
        addIntrinsicLet(MapSet.NAME, MapSet.TYPE);
        addIntrinsicLet(MapSets.NAME, MapSets.TYPE);
        addIntrinsicLet(MapDel.NAME, MapDel.TYPE);
        addIntrinsicLet(MPlus.NAME, MPlus.TYPE);
        addIntrinsicLet(Values.NAME, Values.TYPE);

        // misc poly
        addIntrinsicLet(Empty.NAME, Empty.TYPE);
        addIntrinsicLet(Hash.NAME, Hash.TYPE);

        // system
        addIntrinsicLet(Assert.NAME, Assert.TYPE);
        addIntrinsicLet(AvailProcs.NAME, AvailProcs.TYPE);
        addIntrinsicLet(MilliTime.NAME, MilliTime.TYPE);
        addIntrinsicLet(NanoTime.NAME, NanoTime.TYPE);
        addIntrinsicLet(Print.NAME, Print.TYPE);
        addIntrinsicLet(PrintStr.NAME, PrintStr.TYPE);

        // logging
        addIntrinsicLet(LogDebug.NAME, LogDebug.TYPE);
        addIntrinsicLet(LogInfo.NAME, LogInfo.TYPE);
        addIntrinsicLet(LogWarning.NAME, LogWarning.TYPE);
        addIntrinsicLet(LogError.NAME, LogError.TYPE);

        // threads
        addIntrinsicLet(Future.NAME, Future.TYPE);
        addIntrinsicLet(PMap.NAME, PMap.TYPE);
        addIntrinsicLet(Spawn.NAME, Spawn.TYPE);
        addIntrinsicLet(Sleep.NAME, Sleep.TYPE);
        addIntrinsicLet(TaskId.NAME, TaskId.TYPE);

        // trans/boxes
        addIntrinsicLet(Await.NAME, Await.TYPE);
        addIntrinsicLet(Awaits.NAME, Awaits.TYPE);
        addIntrinsicLet(BoxNew.NAME, BoxNew.TYPE);
        addIntrinsicLet(Do.NAME, Do.TYPE);
        addIntrinsicLet(Get.NAME, Get.TYPE);
        addIntrinsicLet(Gets.NAME, Gets.TYPE);
        addIntrinsicLet(InTran.NAME, InTran.TYPE);
        addIntrinsicLet(Own.NAME, Own.TYPE);
        addIntrinsicLet(Owns.NAME, Owns.TYPE);
        addIntrinsicLet(Put.NAME, Put.TYPE);
        addIntrinsicLet(Puts.NAME, Puts.TYPE);
        addIntrinsicLet(Snap.NAME, Snap.TYPE);
        addIntrinsicLet(Snaps.NAME, Snaps.TYPE);
        addIntrinsicLet(Transfer.NAME, Transfer.TYPE);
        addIntrinsicLet(Transfers.NAME, Transfers.TYPE);
        addIntrinsicLet(Unwatch.NAME, Unwatch.TYPE);
        addIntrinsicLet(Update.NAME, Update.TYPE);
        addIntrinsicLet(Updates.NAME, Updates.TYPE);
        addIntrinsicLet(Watch.NAME, Watch.TYPE);
        
        // DEMO SUPPORT

        // processing
        addIntrinsicLet(PrArc.NAME, PrArc.TYPE);
        addIntrinsicLet(PrBeginShape.NAME, PrBeginShape.TYPE);
        addIntrinsicLet(PrBeginShapeMode.NAME, PrBeginShapeMode.TYPE);
        addIntrinsicLet(PrBackground.NAME, PrBackground.TYPE);
        addIntrinsicLet(PrClose.NAME, PrClose.TYPE);
        addIntrinsicLet(PrColor.NAME, PrColor.TYPE);
        addIntrinsicLet(PrCreateFont.NAME, PrCreateFont.TYPE);
        addIntrinsicLet(PrCurveVertex.NAME, PrCurveVertex.TYPE);
        addIntrinsicLet(PrDirectionalLight.NAME, PrDirectionalLight.TYPE);
        addIntrinsicLet(PrEllipse.NAME, PrEllipse.TYPE);
        addIntrinsicLet(PrEndShape.NAME, PrEndShape.TYPE);
        addIntrinsicLet(PrFill.NAME, PrFill.TYPE);
        addIntrinsicLet(PrFillRGBA.NAME, PrFillRGBA.TYPE);
        addIntrinsicLet(PrImage.NAME, PrImage.TYPE);
        addIntrinsicLet(PrImageHeight.NAME, PrImageHeight.TYPE);
        addIntrinsicLet(PrImageWidth.NAME, PrImageWidth.TYPE);
        addIntrinsicLet(PrIsOpen.NAME, PrIsOpen.TYPE);
        addIntrinsicLet(PrKey.NAME, PrKey.TYPE);
        addIntrinsicLet(PrKeyCode.NAME, PrKeyCode.TYPE);
        addIntrinsicLet(PrLine3D.NAME, PrLine3D.TYPE);
        addIntrinsicLet(PrLights.NAME, PrLights.TYPE);
        addIntrinsicLet(PrLoadFont.NAME, PrLoadFont.TYPE);
        addIntrinsicLet(PrLoadImage.NAME, PrLoadImage.TYPE);
        addIntrinsicLet(PrLoop.NAME, PrLoop.TYPE);
        addIntrinsicLet(PrMatMult.NAME, PrMatMult.TYPE);
        addIntrinsicLet(PrMatRotateX.NAME, PrMatRotateX.TYPE);
        addIntrinsicLet(PrMatRotateY.NAME, PrMatRotateY.TYPE);
        addIntrinsicLet(PrMatRotateZ.NAME, PrMatRotateZ.TYPE);
        addIntrinsicLet(PrMatTranslate.NAME, PrMatTranslate.TYPE);
        addIntrinsicLet(PrMouse.NAME, PrMouse.TYPE);
        addIntrinsicLet(PrMouseButton.NAME, PrMouseButton.TYPE);
        addIntrinsicLet(PrMousePressed.NAME, PrMousePressed.TYPE);
        addIntrinsicLet(PrNoStroke.NAME, PrNoStroke.TYPE);
        addIntrinsicLet(PrOpen.NAME, PrOpen.TYPE);
        addIntrinsicLet(PrPMatrix3D.NAME, PrPMatrix3D.TYPE);
        addIntrinsicLet(PrPmouse.NAME, PrPmouse.TYPE);
        addIntrinsicLet(PrRect.NAME, PrRect.TYPE);
        addIntrinsicLet(PrRedraw.NAME, PrRedraw.TYPE);
        addIntrinsicLet(PrRotateX.NAME, PrRotateX.TYPE);
        addIntrinsicLet(PrRotateY.NAME, PrRotateY.TYPE);
        addIntrinsicLet(PrRotateZ.NAME, PrRotateZ.TYPE);
        addIntrinsicLet(PrScreenHeight.NAME, PrScreenHeight.TYPE);
        addIntrinsicLet(PrScreenWidth.NAME, PrScreenWidth.TYPE);
        addIntrinsicLet(PrSize.NAME, PrSize.TYPE);
        addIntrinsicLet(PrSizeMode.NAME, PrSizeMode.TYPE);
        addIntrinsicLet(PrSmooth.NAME, PrSmooth.TYPE);
        addIntrinsicLet(PrStroke.NAME, PrStroke.TYPE);
        addIntrinsicLet(PrStrokeWeight.NAME, PrStrokeWeight.TYPE);
        addIntrinsicLet(PrText.NAME, PrText.TYPE);
        addIntrinsicLet(PrTextFont.NAME, PrTextFont.TYPE);
        addIntrinsicLet(PrTextWidth.NAME, PrTextWidth.TYPE);
        addIntrinsicLet(PrTriangle.NAME, PrTriangle.TYPE);
        addIntrinsicLet(PrTranslate.NAME, PrTranslate.TYPE);
        addIntrinsicLet(PrVertex.NAME, PrVertex.TYPE);
        addIntrinsicLet(PrVertex3D.NAME, PrVertex3D.TYPE);

        // socket
        addIntrinsicLet(Accept.NAME, Accept.TYPE);
        addIntrinsicLet(Close.NAME,  Close.TYPE);
        addIntrinsicLet(Closed.NAME,  Closed.TYPE);
        addIntrinsicLet(SSocket.NAME,  SSocket.TYPE);

        // file
        addIntrinsicLet(AppendFile.NAME, AppendFile.TYPE);
        addIntrinsicLet(ReadFile.NAME, ReadFile.TYPE);
        addIntrinsicLet(WriteFile.NAME, WriteFile.TYPE);

        // misc
        addIntrinsicLet(HSB2RGB.NAME, HSB2RGB.TYPE);
        addIntrinsicLet(HttpHead.NAME, HttpHead.TYPE);
        addIntrinsicLet(HttpGet.NAME, HttpGet.TYPE);
        addIntrinsicLet(ParseXml.NAME, ParseXml.TYPE);
        addIntrinsicLet(RGB2HSB.NAME, RGB2HSB.TYPE);

        // TEST

        // foreign mutating resource
        addIntrinsicLet(runtime.intrinsic.test.Array.NAME, runtime.intrinsic.test.Array.TYPE);
        addIntrinsicLet(ALen.NAME, ALen.TYPE);
        addIntrinsicLet(AGet.NAME, AGet.TYPE);
        addIntrinsicLet(ASet.NAME, ASet.TYPE);
    }

    // helpers

    /**
     * This is just a shim that lets us give marginally more convenient
     * in-java type exprs than would otherwise be necessary. Basically
     * we can just give inline type params in place, rather than having
     * to wrap them in type refs, declare param lists etc.
     * <p/>
     * TODO remove once intrinsic decls are in source
     */
    private void addIntrinsicLet(final String name, final Type type)
    {
        final Type converted = TypeInlineConverter.convert(type);

        final Type prepped = prepType(converted);

        final LetBinding let = new LetBinding(Loc.INTRINSIC, name, prepped);

        super.addLet(let);
    }

    /**
     *
     */
    public static Type prepType(final Type type)
    {
        type.collectInlineParams();

        // names must be pre-resolved, so this is ok
        KindChecker.check(type);

        return type;
    }
}
