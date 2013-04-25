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
package runtime.intrinsic;

import runtime.intrinsic.demo.*;
import runtime.intrinsic.demo.processing.*;
import runtime.intrinsic.demo.socket.Accept;
import runtime.intrinsic.demo.socket.Close;
import runtime.intrinsic.demo.socket.Closed;
import runtime.intrinsic.demo.socket.SSocket;
import runtime.intrinsic.log.LogDebug;
import runtime.intrinsic.log.LogError;
import runtime.intrinsic.log.LogInfo;
import runtime.intrinsic.log.LogWarning;
import runtime.intrinsic.test.*;
import runtime.intrinsic.tran.*;

/**
 * Backing class for {@link compile.module.intrinsic.IntrinsicModule}.
 * <p/>
 * Generated code hard-links to these, so Java member names
 * must match NAME fields in impl classes.
 *
 * @author Basil Hosmer
 */
@SuppressWarnings({"UnusedDeclaration"})
public final class Intrinsics
{
    private static final String runtimeClassName = 
        "runtime.intrinsic.Intrinsics";

    public static String getRuntimeNameFor(final String name)
    {
        return runtimeClassName + "._" + name;
    }

    // logic
    public static final And _and = new And();
    public static final Or _or = new Or();
    public static final Not _not = new Not();

    // int relops
    public static final EQ _eq = new EQ();
    public static final NE _ne = new NE();
    public static final GT _gt = new GT();
    public static final GE _ge = new GE();
    public static final LT _lt = new LT();
    public static final LE _le = new LE();

    // int arith ops
    public static final Plus _plus = new Plus();
    public static final Minus _minus = new Minus();
    public static final Times _times = new Times();
    public static final Div _div = new Div();
    public static final Max _max = new Max();
    public static final Min _min = new Min();
    public static final Mod _mod = new Mod();
    public static final Neg _neg = new Neg();
    public static final Pow _pow = new Pow();
    public static final Sign _sign = new Sign();

    // int bitwise ops
    public static final Band _band = new Band();
    public static final Bor _bor = new Bor();

    // long arith ops
    public static final LMinus _lminus = new LMinus();

    // fp relops
    public static final FGT _fgt = new FGT();
    public static final FGE _fge = new FGE();
    public static final FLT _flt = new FLT();
    public static final FLE _fle = new FLE();

    // fp arith ops
    public static final FDiv _fdiv = new FDiv();
    public static final FMinus _fminus = new FMinus();
    public static final FMod _fmod = new FMod();
    public static final FNeg _fneg = new FNeg();
    public static final FPow _fpow = new FPow();
    public static final FTimes _ftimes = new FTimes();

    // other math
    public static final ATan2 _atan2 = new ATan2();
    public static final Cos _cos = new Cos();
    public static final Draw _draw = new Draw();
    public static final Exp _exp = new Exp();
    public static final F2I _f2i = new F2I();
    public static final FRand _frand = new FRand();
    public static final Ln _ln = new Ln();
    public static final Rand _rand = new Rand();
    public static final Sin _sin = new Sin();
    public static final Sqrt _sqrt = new Sqrt();
    public static final Tan _tan = new Tan();

    // string
    public static final EndsWith _endswith = new EndsWith();
    public static final StartsWith _startswith = new StartsWith();
    public static final StrCat _strcat = new StrCat();
    public static final StrCmp _strcmp = new StrCmp();
    public static final StrCut _strcut = new StrCut();
    public static final StrDrop _strdrop = new StrDrop();
    public static final StrFind _strfind = new StrFind();
    public static final StrJoin _strjoin = new StrJoin();
    public static final StrLen _strlen = new StrLen();
    public static final StrSplit _strsplit = new StrSplit();
    public static final StrTake _strtake = new StrTake();
    public static final StrWhere _strwhere = new StrWhere();
    public static final Substr _substr = new Substr();
    public static final ToLower _tolower = new ToLower();
    public static final ToUpper _toupper = new ToUpper();

    // converters
    public static final B2I _b2i = new B2I();
    public static final F2S _f2s = new F2S();
    public static final I2B _i2b = new I2B();
    public static final I2F _i2f = new I2F();
    public static final I2S _i2s = new I2S();
    public static final L2F _l2f = new L2F();
    public static final L2I _l2i = new L2I();
    public static final L2S _l2s = new L2S();
    public static final S2F _s2f = new S2F();
    public static final S2I _s2i = new S2I();
    public static final S2L _s2l = new S2L();
    public static final S2Sym _s2sym = new S2Sym();
    public static final Sym2S _sym2s = new Sym2S();
    public static final ToStr _tostr = new ToStr();

    // each/compose
    public static final CompL _compl = new CompL();
    public static final CompM _compm = new CompM();
    public static final Map _map = new Map();
    public static final MapLL _mapll = new MapLL();
    public static final MapLM _maplm = new MapLM();
    public static final MapMF _mapmf = new MapMF();
    public static final MapML _mapml = new MapML();
    public static final MapMM _mapmm = new MapMM();
    public static final Mapz _mapz = new Mapz();

    // other h/o
    public static final Cycle _cycle = new Cycle();
    public static final CycleN _cyclen = new CycleN();
    public static final EvolveWhile _evolve_while = new EvolveWhile();
    public static final Filter _filter = new Filter();
    public static final For _for = new For();
    public static final Guard _guard = new Guard();
    public static final If _if = new If();
    public static final Iif _iif = new Iif();
    public static final PFor _pfor = new PFor();
    public static final Reduce _reduce = new Reduce();
    public static final Scan _scan = new Scan();
    public static final When _when = new When();
    public static final Where _where = new Where();
    public static final While _while = new While();

    // list
    public static final Append _append = new Append();
    public static final Count _count = new Count();
    public static final Cut _cut = new Cut();
    public static final Distinct _distinct = new Distinct();
    public static final Drop _drop = new Drop();
    public static final Find _find = new Find();
    public static final First _first = new First();
    public static final Flatten _flatten = new Flatten();
    public static final FromTo _fromto = new FromTo();
    public static final Group _group = new Group();
    public static final IsIndex _isindex = new IsIndex();
    public static final Last _last = new Last();
    public static final LPlus _lplus = new LPlus();
    public static final ListSet _listset = new ListSet();
    public static final ListSets _listsets = new ListSets();
    public static final Range _range = new Range();
    public static final Remove _remove = new Remove();
    public static final Rep _rep = new Rep();
    public static final Rest _rest = new Rest();
    public static final Shuffle _shuffle = new Shuffle();
    public static final Size _size = new Size();
    public static final Take _take = new Take();
    public static final Unique _unique = new Unique();
    public static final Unzip _unzip = new Unzip();
    public static final Zip _zip = new Zip();

    // map
    public static final Assoc _assoc = new Assoc();
    public static final Entries _entries = new Entries();
    public static final IsKey _iskey = new IsKey();
    public static final Keys _keys = new Keys();
    public static final MapDel _mapdel = new MapDel();
    public static final MapSet _mapset = new MapSet();
    public static final MapSets _mapsets = new MapSets();
    public static final MPlus _mplus = new MPlus();
    public static final Values _values = new Values();

    // misc poly
    public static final Empty _empty = new Empty();
    public static final Hash _hash = new Hash();

    // system
    public static final Assert _assert = new Assert();
    public static final AvailProcs _availprocs = new AvailProcs();
    public static final MilliTime _millitime = new MilliTime();
    public static final NanoTime _nanotime = new NanoTime();
    public static final Print _print = new Print();
    public static final PrintStr _printstr = new PrintStr();

    // logging
    public static final LogDebug _logdebug = new LogDebug();
    public static final LogInfo _loginfo = new LogInfo();
    public static final LogWarning _logwarning = new LogWarning();
    public static final LogError _logerror = new LogError();

    // threads
    public static final Future _future = new Future();
    public static final PMap _pmap = new PMap();
    public static final Sleep _sleep = new Sleep();
    public static final Spawn _spawn = new Spawn();
    public static final TaskId _taskid = new TaskId();

    // trans/boxes
    public static final Await _await = new Await();
    public static final Awaits _awaits = new Awaits();
    public static final BoxNew _box = new BoxNew();
    public static final Do _do = new Do();
    public static final Get _get = new Get();
    public static final Gets _gets = new Gets();
    public static final InTran _intran = new InTran();
    public static final Own _own = new Own();
    public static final Owns _owns = new Owns();
    public static final Put _put = new Put();
    public static final Puts _puts = new Puts();
    public static final Snap _snap = new Snap();
    public static final Snaps _snaps = new Snaps();
    public static final Transfer _transfer = new Transfer();
    public static final Transfers _transfers = new Transfers();
    public static final Unwatch _unwatch = new Unwatch();
    public static final Update _update = new Update();
    public static final Updates _updates = new Updates();
    public static final Watch _watch = new Watch();

    // DEMO SUPPORT

    // processing
    public static final PrArc _prarc = new PrArc();
    public static final PrBackground _prbackground = new PrBackground();
    public static final PrBeginShape _prbeginshape = new PrBeginShape();
    public static final PrBeginShapeMode _prbeginshapemode = new PrBeginShapeMode();
    public static final PrClose _prclose = new PrClose();
    public static final PrColor _prcolor = new PrColor();
    public static final PrCreateFont _prcreatefont = new PrCreateFont();
    public static final PrCurveVertex _prcurvevertex = new PrCurveVertex();
    public static final PrDirectionalLight _prdirectionallight = new PrDirectionalLight();
    public static final PrEllipse _prellipse = new PrEllipse();
    public static final PrEndShape _prendshape = new PrEndShape();
    public static final PrFill _prfill = new PrFill();
    public static final PrFillRGBA _prfillrgba = new PrFillRGBA();
    public static final PrImage _primage = new PrImage();
    public static final PrImageHeight _primageheight = new PrImageHeight();
    public static final PrImageWidth _primagewidth = new PrImageWidth();
    public static final PrIsOpen _prisopen = new PrIsOpen();
    public static final PrKey _prkey = new PrKey();
    public static final PrKeyCode _prkeycode = new PrKeyCode();
    public static final PrLights _prlights = new PrLights();
    public static final PrLine3D _prline3d = new PrLine3D();
    public static final PrLoadFont _prloadfont = new PrLoadFont();
    public static final PrLoadImage _prloadimage = new PrLoadImage();
    public static final PrLoop _prloop = new PrLoop();
    public static final PrMatMult _prmatmult = new PrMatMult();
    public static final PrMatRotateX _prmatrotatex = new PrMatRotateX();
    public static final PrMatRotateY _prmatrotatey = new PrMatRotateY();
    public static final PrMatRotateZ _prmatrotatez = new PrMatRotateZ();
    public static final PrMatTranslate _prmattranslate = new PrMatTranslate();
    public static final PrMouse _prmouse = new PrMouse();
    public static final PrMouseButton _prmousebutton = new PrMouseButton();
    public static final PrMousePressed _prmousepressed = new PrMousePressed();
    public static final PrPMatrix3D _prpmatrix3d = new PrPMatrix3D();
    public static final PrPmouse _prpmouse = new PrPmouse();
    public static final PrNoStroke _prnostroke = new PrNoStroke();
    public static final PrOpen _propen = new PrOpen();
    public static final PrRect _prrect = new PrRect();
    public static final PrRedraw _prredraw = new PrRedraw();
    public static final PrRotateX _prrotatex = new PrRotateX();
    public static final PrRotateY _prrotatey = new PrRotateY();
    public static final PrRotateZ _prrotatez = new PrRotateZ();
    public static final PrScreenHeight _prscreenheight = new PrScreenHeight();
    public static final PrScreenWidth _prscreenwidth = new PrScreenWidth();
    public static final PrSize _prsize = new PrSize();
    public static final PrSizeMode _prsizemode = new PrSizeMode();
    public static final PrSmooth _prsmooth = new PrSmooth();
    public static final PrStroke _prstroke = new PrStroke();
    public static final PrStrokeWeight _prstrokeweight = new PrStrokeWeight();
    public static final PrText _prtext = new PrText();
    public static final PrTextFont _prtextfont = new PrTextFont();
    public static final PrTextWidth _prtextwidth = new PrTextWidth();
    public static final PrTriangle _prtriangle = new PrTriangle();
    public static final PrTranslate _prtranslate = new PrTranslate();
    public static final PrVertex _prvertex = new PrVertex();
    public static final PrVertex3D _prvertex3d = new PrVertex3D();

    // socket
    public static final Accept _accept = new Accept();
    public static final Close _close = new Close();
    public static final Closed _closed = new Closed();
    public static final SSocket _ssocket = new SSocket();

    // file
    public static final AppendFile _appendfile = new AppendFile();
    public static final ReadFile _readfile = new ReadFile();
    public static final WriteFile _writefile = new WriteFile();

    // misc
    public static final HSB2RGB _hsb2rgb = new HSB2RGB();
    public static final HttpGet _httpget = new HttpGet();
    public static final HttpHead _httphead = new HttpHead();
    public static final ParseXml _parsexml = new ParseXml();
    public static final RGB2HSB _rgb2hsb = new RGB2HSB();

    // TEST

    // foreign interface
    public static final runtime.intrinsic.test.Array _array = new runtime.intrinsic.test.Array();
    public static final ALen _alen = new ALen();
    public static final AGet _aget = new AGet();
    public static final ASet _aset = new ASet();
}
