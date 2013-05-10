//
// operator backing functions
//

/** {@link types.m} */
import * from types;

//
// logic
//

/**
 * Shortcutting and(). Also aliased to infix operator &&.
 * @param b Bool value
 * @param p predicate
 * @return true iff b and p() are true. if b is false, p is not called
 */
intrinsic and(b : Bool, p : () -> Bool) -> Bool;

/**
 * Shortcutting or(). Also aliased to infix operator ||.
 * @param b Bool value
 * @param p predicate
 * @return true if either b or p() are true. if b is true, p is not called
 */
intrinsic or(b : Bool, p : () -> Bool) -> Bool;

/**
 * Logical not(). Also aliased to prefix operator !
 * @param b Bool value
 * @return Logical complement of b.
 */
intrinsic not(b : Bool) -> Bool;

//
// bitwise ops
// TODO shifts
//

/**
 * Bitwise AND
 * @param x
 * @param y
 * @return logical AND of each bit in x and y
 */
intrinsic band(x:Int, y:Int) -> Int;

/**
 * Bitwise OR
 * @param x
 * @param y
 * @return logical OR of each bit in x and y
 */
intrinsic bor(x:Int, y:Int) -> Int;

//
// equality
//

/**
 * All values can be tested within type for equality. Equality is
 * deep value equality (impl uses hashes where possible) for everything
 * except functions and boxes, which are discrete (identity equal).
 * Also aliased to infix operator ==
 * @param x value
 * @param Y value
 * @return true iff x and y are equal by above rules
 */
intrinsic <T> eq(x : T, y : T) -> Bool;

/**
 * Not equals. Inverse of {@link #eq}.
 * Also aliased to infix operator !=
 * @param x value
 * @param y value
 * @return true iff !(x == y) is true
 */
intrinsic <T> ne(x : T, y : T) -> Bool;

//
// int rel
//

/**
 * Integer greater-than. Also aliased to infix operator >
 * @param x int
 * @param y int
 * @return true iff x is greater than y
 */
intrinsic gt(x : Int, y : Int) -> Bool;

/**
 * Integer greater-than-or-equal. Also aliased to infix operator >=
 * @param x int
 * @param y int
 * @return true iff x is greater than or equal to y
 */
intrinsic ge(x : Int, y : Int) -> Bool;

/**
 * Integer less-than. Also aliased to infix operator <
 * @param x int
 * @param y int
 * @return true iff x is less than y
 */
intrinsic lt(x : Int, y : Int) -> Bool;

/**
 * Integer less-than-or-equal. Also aliased to infix operator <=
 * @param x int
 * @param y int
 * @return true iff x is less than or equal to y
 */
intrinsic le(x : Int, y : Int) -> Bool;

//
// int arith (plus doesn't quite fit here, but see comment header)
//

/**
 * plus() is currently typed over all types, as a convenience stopgap
 * until the typeclass/interface functionality comes online. Results
 * are as follows currently:
 * - Int, Long, Float, Double: arithmetic addition
 * - Boolean: or
 * - String: concatenation
 * - List: {@link #lplus} = concatenation
 * - Map: {@link #mplus} = directional union (right operand wins)
 *
 * Also aliased to infix +
 * @param x value
 * @param y value
 * @return x add to y
 */
intrinsic <T> plus(x : T, y : T) -> T;

/**
 * Integer difference. Also aliased to infix operator -
 * @param x int
 * @param y int
 * @return int value of x - y
 */
intrinsic minus(x : Int, y : Int) -> Int;

/**
 * Integer multiplication. Also aliased to infix operator *
 * @param x int
 * @param y int
 * @return int value of x * y
 */
intrinsic times(x : Int, y : Int) -> Int;

/**
 * Integer division. Also aliased to infix operator /
 * @param x divisor
 * @param y denominator
 * @returns quotient
 */
intrinsic div(x : Int, y : Int) -> Int;

/**
 * Integer modulo. Also aliased to infix operator %
 * @param x int
 * @param y int
 * @return int value of x % y
 */
intrinsic mod(x : Int, y : Int) -> Int;

/**
 * Integer negation. Also aliased to prefix unary operator -
 * @param x int
 * @return integer value -x
 */
intrinsic neg(x : Int) -> Int;

/**
 * Integer pow(). Also aliased to infix operator ^
 * @param x int
 * @param y int
 * @return integer value of x raised to the power of y
 */
intrinsic pow(x : Int, y : Int) -> Int;

/**
 * Max value of two integers
 * @param x int value
 * @param y int value
 * @return larger value of x and y
 */
intrinsic max(x:Int, y:Int) -> Int;

/**
 * Min value of two integers
 * @param x int value
 * @param y int value
 * @return smaller value of x and y
 */
intrinsic min(x:Int, y:Int) -> Int;

/**
 * Integer sign.
 * @param x value
 * @return Returns 1 if x > 0, 0 if x == 0, otherwise -1.
 */
intrinsic sign(x : Int) -> Int;

//
// long arith, such as it is
// TODO flesh out under interfaces
//

/**
 * Long difference
 * @param x long
 * @param y long
 * @return long value of x - y
 */
intrinsic lminus(x : Long, y : Long) -> Long;

//
// floating-point (i.e., Double) rel
//

/**
 * Double greater-than. Aliased to infix operator >., for now
 * @param x float
 * @param y float
 * @return true iff x is greater than y
 */
intrinsic fgt(x : Double, y : Double) -> Bool;

/**
 * Double greater-than-or-equal. Aliased to infix operator >=., for now
 * @param x float
 * @param y float
 * @return true iff x is greater than or equal to y
 *
 */
intrinsic fge(x : Double, y : Double) -> Bool;

/**
 * Double less-than. Aliased to infix operator <., for now
 * @param x float
 * @param y float
 * @return true iff x is less than y
 */
intrinsic flt(x : Double, y : Double) -> Bool;

/**
 * Double less-than-or-equal. Aliased to infix operator <=., for now
 * @param x float
 * @param y float
 * @return true iff x is less than or equal to y
 */
intrinsic fle(x : Double, y : Double) -> Bool;

//
// floating-point arith
// note that fplus() is subsumed by plus() already
//

/**
 * Double difference. Aliased to infix operator -. for now
 * @param x float
 * @param y float
 * @return float value of x - y
 */
intrinsic fminus(x : Double, y : Double) -> Double;

/**
 * Double multiplication. Aliased to infix operator *. for now
 * @param x float value
 * @param y float value
 * @return x multiplied with y.
 */
intrinsic ftimes(x : Double, y : Double) -> Double;

/**
 * Double division. Aliased to infix operator /. for now
 * @param x divisor
 * @param y denominator
 * @return quotient
 */
intrinsic fdiv(x : Double, y : Double) -> Double;

/**
 * Double modulo. Aliased to infix operator %. for now
 * @param x float
 * @param y float
 * @return float value of x % y
 */
intrinsic fmod(x : Double, y : Double) -> Double;

/**
 * Doudble negation. Alaised to prefix unary operator -. for now
 * @param x float
 * @return float value -x
 */
intrinsic fneg(x : Double) -> Double;

/**
 * Double pow(). Aliased to infix operator ^. for now
 * @param x float
 * @param y float
 * @return float value of x raised to the power of y
 */
intrinsic fpow(x : Double, y : Double) -> Double;

