//
// Misc toy I/O intrinsics, demo quality only.
// TODO beef these up once we have variants and interfaces.
// Then document, factor, replace intrinsic types, etc.
//

// utterly minimal file i/o--waiting for variants
intrinsic appendfile(x:String, y:String) -> Bool;
intrinsic readfile(x:String) -> String;
intrinsic writefile(x:String, y:String) -> Bool;

// XML parsing--ditto
intrinsic type XNode;   // a structural record type
intrinsic parsexml(x:String) -> XNode;

// primitive server sockets, used in tests/demos
intrinsic type ServerSocket;
intrinsic accept(x:ServerSocket, y:String -> String) -> ();
intrinsic close(x:ServerSocket) -> ();
intrinsic closed(x:ServerSocket) -> Bool;
intrinsic ssocket(x:Int) -> ServerSocket;

// primitive http, used in tests/demos
intrinsic httpget(x:String) -> String;
intrinsic httphead(x:String) -> [String];

// simple array hookup, used in some interop tests
intrinsic type Array;
intrinsic <T> array(x:Int, y:T) -> Array(T);
intrinsic <T> aget(x:Array(T), y:Int) -> T;
intrinsic <T> alen(x:Array(T)) -> Int;
intrinsic <T> aset(x:Array(T), y:Int, z:T) -> Array(T);

