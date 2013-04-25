//
// Processing library intrinsics
//

// processing FFI types
// TODO use New(Opaque) once type matching is in
intrinsic type Font;
intrinsic type Image;
intrinsic type Matrix3D;

intrinsic prarc(x:Double, y:Double, z:Double, a:Double, b:Double, c:Double) -> ();
intrinsic prbackground(x:Int) -> ();
intrinsic prbeginshape() -> ();
intrinsic prbeginshapemode(x:Symbol) -> ();
intrinsic prclose() -> ();
intrinsic prcolor(x:Int, y:Int, z:Int, a:Int) -> Int;
intrinsic prcreatefont(x:String, y:Int) -> Font;
intrinsic prcurvevertex(x:Double, y:Double) -> ();
intrinsic prdirectionallight(x:Double, y:Double, z:Double, a:Double, b:Double, c:Double) -> ();
intrinsic prellipse(x:Double, y:Double, z:Double, a:Double) -> ();
intrinsic prendshape() -> ();
intrinsic prfill(x:Int) -> ();
intrinsic prfillrgba(x:Int, y:Int, z:Int, a:Int) -> ();
intrinsic primage(x:Image, y:Double, z:Double, a:Double, b:Double) -> ();
intrinsic primageheight(x:Image) -> Int;
intrinsic primagewidth(x:Image) -> Int;
intrinsic prisopen() -> Bool;
intrinsic prkey() -> String;
intrinsic prkeycode() -> Int;
intrinsic prlights() -> ();
intrinsic prline3d(x:Double, y:Double, z:Double, a:Double, b:Double, c:Double) -> ();
intrinsic prloadfont(x:String) -> Font;
intrinsic prloadimage(x:String) -> Image;
intrinsic prloop(x:Bool) -> ();
intrinsic prmatmult(x:Matrix3D, y:Double, z:Double, a:Double) -> (Double, Double, Double);
intrinsic prmatrotatex(x:Matrix3D, y:Double) -> Matrix3D;
intrinsic prmatrotatey(x:Matrix3D, y:Double) -> Matrix3D;
intrinsic prmatrotatez(x:Matrix3D, y:Double) -> Matrix3D;
intrinsic prmattranslate(x:Matrix3D, y:Double, z:Double, a:Double) -> Matrix3D;
intrinsic prmouse() -> (Int, Int);
intrinsic prmousebutton() -> Symbol;
intrinsic prmousepressed() -> Bool;
intrinsic prnostroke() -> ();
intrinsic propen(x:String, y:[Symbol : (() -> ())]) -> ();
intrinsic prpmatrix3d() -> Matrix3D;
intrinsic prpmouse() -> (Int, Int);
intrinsic prrect(x:Double, y:Double, z:Double, a:Double) -> ();
intrinsic prredraw() -> ();
intrinsic prrotatex(x:Double) -> ();
intrinsic prrotatey(x:Double) -> ();
intrinsic prrotatez(x:Double) -> ();
intrinsic prscreenheight() -> Int;
intrinsic prscreenwidth() -> Int;
intrinsic prsize(x:Int, y:Int) -> ();
intrinsic prsizemode(x:Int, y:Int, z:Symbol) -> ();
intrinsic prsmooth() -> ();
intrinsic prstroke(x:Int, y:Int, z:Int) -> ();
intrinsic prstrokeweight(x:Int) -> ();
intrinsic prtext(x:String, y:Double, z:Double) -> ();
intrinsic prtextfont(x:Font) -> ();
intrinsic prtextwidth(x:String) -> Double;
intrinsic prtranslate(x:Double, y:Double) -> ();
intrinsic prtriangle(x:Double, y:Double, z:Double, a:Double, b:Double, c:Double) -> ();
intrinsic prvertex(x:Double, y:Double) -> ();
intrinsic prvertex3d(x:Double, y:Double, z:Double) -> ();
