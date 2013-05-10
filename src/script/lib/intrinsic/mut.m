//
// mutability: boxes, transactions etc.
//

/** {@link types.m} */
import * from types;

/**
 * Transactional wait/notify.
 * await(box, pred) puts current thread into wait state
 * until/unless pred(get(box)) returns true. pred() is
 * called each time a value is committed to box.
 * @param x box
 * @param y predicate
 */
intrinsic <T> await(x:*T, y:(T -> Bool)) -> ();

/**
 * Transactional wait/notify.
 * awaits((boxes), pred) puts current thread into wait state
 * until/unless pred(gets(boxes)) returns true. pred() is
 * called each time a value is committed to one of the boxes.
 * @param x tuple of boxes
 * @param y predicate
 */
intrinsic <T:[*]> awaits(x:Tup(T | Box), y:(Tup(T) -> Bool)) -> ();

/**
 * Create a box with an initial value.
 * @param x value to be boxed
 * @return boxed value
 */
intrinsic <T> box(x : T) -> *T;

/**
 * Run a block in a transaction.
 */
intrinsic <T> do(b : () -> T) -> T;

/**
 * @param x box
 * @return value of box
 */
intrinsic <T> get(x:*T) -> T; // unary *

/**
 * @param x tuple of boxes
 * @return tuple of values
 */
intrinsic <Ts:[*]> gets(t : Tup(Ts | Box)) -> Tup(Ts);

/**
 * @return true if currently in a transaction, otherwise false.
 */
intrinsic intran() -> Bool;

/**
 * Own a box. Valid only in a transaction. returns box value.
 * @param x box
 * @return box value
 */
intrinsic <T> own(x:*T) -> T;

/**
 * Own a tuple of boxes. Valid only in a transaction. returns tuple of box values.
 * @param x tuple of boxes
 * @return tuple of box values
 */
intrinsic <Ts:[*]> owns(x:Tup(Ts | Box)) -> Tup(Ts);

/**
 * Set a box's value. Wraps itself in a transaction if none is running.
 * Sugared to infix (:=)
 * @param b box
 * @param v value
 */
intrinsic <T> put(b : *T, v : T) -> ();

/**
 * Set a tuple of boxes values. Wraps itself in a transaction if none is running.
 * Sugared to infix (::=).
 * @param bs tuple of boxes
 * @param vs tuple of values
 */
intrinsic <T:[*]> puts(bs : Tup(T | Box), vs : Tup(T)) -> ();

/**
 * Snapshot the value held in a box.
 * @param x box
 * @return A snapshot of of the value in the box.
 */
intrinsic <T> snap(x:*T) -> T;

/**
 * Return a tuple of the values held in a tuple of boxes.
 * Atomic, so box reads all take place at the same moment
 * in program time. I.e., this function is equivalent to
 * performing individual gets on each box within a transaction
 * and returning a tuple of the results.
 * @param x tuple of boxes
 * @return tuple of values held in the boxes
 */
intrinsic <Ts:[*]> snaps(x:Tup(Ts | Box)) -> Tup(Ts);

/**
 * Within a transaction, runs the function on the input box and writes
 * the result to the output box. If the function doesn't attempt any box
 * operations requiring relationships outside those already established for
 * reading arguments and writing the result, then it is guaranteed to run
 * without retrying.
 *
 * @param x input box
 * @param y function
 * @param z output box
 */
intrinsic <A,B> transfer(x:*B, y:A -> B, z:*A) -> ();

/**
 * Within a transaction, runs the function on the input boxes and writes
 * the result to the output boxes. If the function doesn't attempt any box
 * operations requiring relationships outside those already established for
 * reading arguments and writing the result, then it is guaranteed to run
 * without retrying.
 *
 * @param x tuple of input boxes
 * @param y function
 * @param z tuple of output boxes
 */
intrinsic <Outs:[*], Ins:[*]> transfers(x:Tup(Outs | Box), y:(Tup(Ins) -> Tup(Outs)), z:Tup(Ins | Box)) -> ();

/**
 * Remove a watcher function from a box.
 * Function equality is identity, so you need to pass
 * the watcher function itself.
 * @param x box being watched
 * @param y function that was returned from {@link watch(x:*T, y:(T, T) -> X) -> ((T, T) -> X)}.
 * @return box
 */
intrinsic <T,X> unwatch(x:*T, y:(T, T) -> X) -> *T;

/**
 * Update a box with the result of a function that takes box's current value as input.
 * Wraps itself in a transaction if none is currently running.
 *
 * @param x box to be updated
 * @param y function that produces the new value for the box
 */
intrinsic <T> update(x:*T, y:T -> T) -> (); // <-

/**
 * Update a tuple of boxes with the result of a function
 * that takes tuple of values as input, returns new tuple
 * as output.
 * @param x tuple of boxes to be updated
 * @param y function that produces a new tuple of values for the boxes
 */
intrinsic <T:[*]> updates(x:Tup(T | Box), y:(Tup(T) -> Tup(T))) -> ();

/**
 * Add a watcher to a box, return watcher.
 * @param x box to be watched
 * @param y function to be executed when the box value changes
 * @return a watcher function
 */
intrinsic <T,X> watch(x:*T, y:(T, T) -> X) -> ((T, T) -> X);
