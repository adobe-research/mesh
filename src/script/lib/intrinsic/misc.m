//
// misc
//

/** {@link types.m} */
import * from types;

//
// misc poly utility funcs
//

/**
 * Hash code, defined over all values. Structural hash for everything
 * except lamdas and boxes, identity hash for those. Order-independent
 * hash for keyed values (maps and records).
 * @param x value
 * @return hash value
 */
intrinsic <T> hash(x : T) -> Int;

/**
 * @param x value of any type
 * @returns true if value is empty in a type-specific sense
 */
intrinsic <T> empty(x : T) -> Bool;

//
// converters
//

/**
 * Bool to Int conversion
 * @param x Bool value
 * @return Int representation of x
 */
intrinsic b2i(x:Bool) -> Int;

/**
 * Convert integer to boolean value.
 * @param x int value
 * @return boolean representation of x.
 */
intrinsic i2b(x:Int) -> Bool;

/**
 * Convert integer to float value.
 * @param x int value
 * @return float representation of x.
 */
intrinsic i2f(x:Int)-> Double;

/**
 * Convert integer to string value.
 * @param x int value
 * @return string representation of x.
 */
intrinsic i2s(x:Int)-> String;

/**
 * Convert long to float value.
 * @param x long value
 * @return float representation of x.
 */
intrinsic l2f(x:Long) -> Double;

/**
 * Convert long to integer value.
 * @param x long value
 * @return integer representation of x.
 */
intrinsic l2i(x:Long) -> Int;

/**
 * Convert long to string value.
 * @param x long value
 * @return string representation of x.
 */
intrinsic l2s(x:Long) -> String;

/**
 * Convert string to float value.
 * @param x string value
 * @return float representation of x.
 */
intrinsic s2f(x:String) -> Double;

/**
 * Convert string to integer value.
 * @param x string value
 * @return integer representation of x.
 */
intrinsic s2i(x:String) -> Int;

/**
 * Convert string to long value.
 * @param x string value
 * @return long representation of x.
 */
intrinsic s2l(x:String) -> Long;

/**
 * Convert string to a symbol.
 * @param x string value
 * @return symbol representated by x.
 */
intrinsic s2sym(x:String) -> Symbol;

/**
 * Returns a string representation of any value.
 * TODO print/parse round trip for a well-defined subset of values.
 * @param x value
 * @return string representation of the value x
 */
intrinsic <T> tostr(x : T) -> String;

