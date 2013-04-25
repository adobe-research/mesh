//
// Integer (Int & Long) utilties and operations
//

/**
 * Bool to Int conversion
 * @param x Bool value
 * @return Int representation of x
 */
intrinsic b2i(x:Bool) -> Int;

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

