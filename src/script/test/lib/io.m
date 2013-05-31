import * from std;
import * from unittest;


// utterly minimal file i/o--waiting for variants
// appendfile : (String, String) -> Bool = <intrinsic>
// TODO
// readfile : String -> String = <intrinsic>
// TODO
// writefile : (String, String) -> Bool = <intrinsic>
// TODO


// XML parsing--ditto
// parsexml : String -> XNode = <intrinsic>
// TODO

// primitive server sockets, used in tests/demos
// accept : (ServerSocket, String -> String) -> () = <intrinsic>
// TODO
// close : ServerSocket -> () = <intrinsic>
// TODO
// closed : ServerSocket -> Bool = <intrinsic>
// TODO
// ssocket : Int -> ServerSocket = <intrinsic>
// TODO

// primitive http, used in tests/demos
// httpget : String -> String = <intrinsic>
// TODO
// httphead : String -> ?(true: [String], false: String) = <intrinsic>
// TODO


// simple array hookup, used in some interop tests
// array : { <T> (Int, T) -> Array(T) => <intrinsic> }
assert_equals({ (true, true, true) }, { a = array(3, true); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Boolean
assert_equals({ (2, 2, 2) }, { a = array(3, 2); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Int
assert_equals({ (2L, 2L, 2L) }, { a = array(3, 2L); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Long
assert_equals({ (2.1, 2.1, 2.1) }, { a = array(3, 2.1); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Double
assert_equals({ ((1, 2), (1, 2), (1, 2)) }, { a = array(3, (1,2)); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Object
assert_equals({ (2, 2, 2) }, { a = apply(array, (3, 2)); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // apply


// aget : { <T> (Array(T), Int) -> T => <intrinsic> }
assert_equals({ true }, { a = array(1, true); aget(a, 0) }); // Boolean
assert_equals({ 2 }, { a = array(1, 2); aget(a, 0) }); // Int
assert_equals({ 2L }, { a = array(1, 2L); aget(a, 0) }); // Long
assert_equals({ 2.1 }, { a = array(1, 2.1); aget(a, 0) }); // Double
assert_equals({ (1, 2) }, { a = array(1, (1,2)); aget(a, 0) }); // Object
assert_equals({ 2 }, { a = array(1, 2); apply(aget, (a, 0)) }); // apply

// aset : { <T> (Array(T), Int, T) -> Array(T) => <intrinsic> }
assert_equals({ false }, { a = array(1, true); aset(a, 0, false); aget(a, 0) }); // Boolean
assert_equals({ 1 }, { a = array(1, 2); aset(a, 0, 1); aget(a, 0) }); // Int
assert_equals({ 1L }, { a = array(1, 2L); aset(a, 0, 1L); aget(a, 0) }); // Long
assert_equals({ 1.1 }, { a = array(1, 2.1); aset(a, 0, 1.1); aget(a, 0) }); // Double
assert_equals({ (0, 0) }, { a = array(1, (1,2)); aset(a, 0, (0,0)); aget(a, 0) }); // Object
assert_equals({ 1 }, { a = array(1, 2); apply(aset, (a, 0, 1)); aget(a, 0) }); // apply

// alen : { <T> Array(T) -> Int => <intrinsic> }
assert_equals({ 1 }, { a = array(1, true); alen(a) }); // Boolean
assert_equals({ 1 }, { a = array(1, 2); alen(a) }); // Int
assert_equals({ 1 }, { a = array(1, 2L); alen(a) }); // Long
assert_equals({ 1 }, { a = array(1, 2.1); alen(a) }); // Double
assert_equals({ 1 }, { a = array(1, (1,2)); alen(a) }); // Object
assert_equals({ 1 }, { a = array(1, 2); apply(alen, (a)) }); // apply
