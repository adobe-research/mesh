//
// system
//

/** {@link types.m} */
import * from types;

//
// concurrency
//

/**
 * Start running a block in a {@link Future} and return a lambda
 * that provides (blocking) access to the result.
 * @param b block to run
 * @return lambda
 */
intrinsic <X> future(b : () -> X) -> () -> X;

/**
 * Runs block asynchronously in a new thread.
 * @param x function
 * @return
 */
intrinsic <T> spawn(x:(() -> T)) -> ();

/**
 * @return returns current task id
 */
intrinsic taskid() -> Long;

//
// logging
//

/**
 *
 */
intrinsic <T> logdebug(x:T) -> ();

/**
 *
 */
intrinsic <T> logerror(x:T) -> ();

/**
 *
 */
intrinsic <T> loginfo(x:T) -> ();

/**
 *
 */
intrinsic <T> logwarning(x:T) -> ();

//
// misc
//

/**
 * Throw an error with message if argument is false
 * @param x if value is false, throw error
 * @param y message
 */
intrinsic assert(x:Bool, y:String) -> ();

/**
 * @return Return number of available processors.
 */
intrinsic availprocs() -> Int;

/**
 * @return current time in millis.
 */
intrinsic millitime() -> Long;

/**
 * @return current time in nanos.
 */
intrinsic nanotime() -> Long;

/**
 * print(x) == {@link PrintStr printstr}({@link ToStr tostr}(x))
 * @param x value to be printed
 * @return
 */
intrinsic <T> print(x:T) -> ();

/**
 * print string to System.out.
 * @param x string to be printed
 * @ return
 */
intrinsic printstr(s : String) -> ();

