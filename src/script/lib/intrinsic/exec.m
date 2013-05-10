//
// execution control (branching, iteration, composition, ...)
//

/** {@link types.m} */
import * from types;

//
// conditional execution
//

/**
 * If condition c is true, run block t, otherwise run block f.
 * Return the result in either case.
 * @param c boolean condition
 * @param t lambda to execute if c is true
 * @param f lambda to execute if c is false
 * @return t() if c is true, otherwise f()
 */
intrinsic <T> if(c : Bool, y : () -> T, z : () -> T) -> T;

/**
 * Conditional execution of a block of code iff predicate is true.
 * @param c predicate boolean condition
 * @param b block to execute
 */
intrinsic <T> when(c : Bool, b : () -> T) -> ();

/**
 * Guarded execution. If the given condition is true, return the
 * given value immediately, otherwise run the given block and
 * return its result.
 *
 * @param c condition
 * @param v immediate return value
 * @param b block to execute if c is false
 * @return v if c is true, otherwise b()
 */
intrinsic <T> guard(c : Bool, v : T, b: () -> T) -> T;

/**
 * Conditional value selection, aka immediate-if: if the
 * given condition is true, return value t, otherwise
 * return value f.
 * @param c boolean condition
 * @param t value to return if c is true
 * @param f value to return if c is false
 * @return t if c is true, otherwise f
 */
intrinsic <T> iif(c : Bool, t : T, f : T) -> T;

//
// iteration
//

/**
 * Iterate on an endofunction while guard predicate returns true.
 * Iteration is started with specified initial argument, which is
 * passed through the guard before being applied.
 *
 * @param p Predicate lambda that accepts the value of v, determines when cycle ends.
 * @param v Initial value of v that will be passed to the f lambda, value of v is applied
 *        to the predicate prior to being passed into the f lambda parameter.
 * @param f Function that accepts passed in value T and returns the next value T that will
 *        be fed back into the function if guard predicate is still true.
 * @return Value of v when predicate is false.
 */
intrinsic <T> cycle(p : T -> Bool, v : T, f : T -> T) -> T;

/**
 * Iterate on a function n times.
 *
 * @param n Limit of how many times f function will be called.
 * @param v Initial value of v that will be passed to the f lambda.
 * @param f Function that accepts passed in value T and returns the next value T that will
 *        be fed back into the function as long as upper limit has not been reached.
 * @return Value of v after the given number of iterations.
 */
intrinsic <T> cyclen(n : Int, v : T, f : T -> T) -> T;

/**
 * Evolve (reduce) until stop condition is met, or inputs
 * are exhausted.
 *
 * @param x Predicate lambda that accepts the value of y, determines when cycle ends.
 * @param y Initial value of y that will be passed as the first argument to the z lambda,
 *        value of y is applied to the predicate prior to being passed into the z lambda parameter.
 * @param z Function that accepts passed in value y and the next item in a. Returns new value of
 * @param a list of values that will be processed
 * @return value of T when predicate is false or all inputs have been processed
 */
intrinsic <A, B> evolve_while(x : A -> Bool, y : A, z : (A, B) -> A, a : [B]) -> A;

/**
 * apply a function to each of a list of index arguments,
 * discarding any results. Currently this is guaranteed
 * to run serially.
 *
 * @param x list of values
 * @param y function to be applied to each value in x
 * @return
 */
intrinsic <X, Y> for(x : [X], y : X -> Y) -> ();

/**
 * parallel-for: apply a function to each of a list of index
 * arguments in parallel, discarding any results.
 * @param x list of values
 * @param y function
 * @return
 */
intrinsic <X, Y> pfor(x : [X], y : X -> Y) -> ();

/**
 * Perform a functional reduction (foldl) using the given reducer,
 * initial value and list of arguments.
 * @param f reducing function
 * @param v initial value
 * @param l list of arguments
 * @return value produced by calling f repeatedly with previously
 *         calculated value and next item in the list.
 */
intrinsic <A, B> reduce(f : (A, B) -> A, v : A, l : [B]) -> A;

/**
 * Traced version of {@link #reduce}:
 * <code>last(scan(f, v, l)) == reduce(f, v, l)</code>
 * @param f reducing function
 * @param v initial value
 * @param l list of arguments
 * @return list of values produced by calling f repeatedly with
 *         previously calculated value and next item in the list.
 */
intrinsic <A, B> scan(f: (A, B) -> A, v : A, l : [B]) -> [A];

/**
 * Run a block repeatedly while guard predicate returns true.
 * @param x predicate block
 * @param y function to execute while predicate is true.
 */
intrinsic <T> while(x : () -> Bool, y : () -> T) -> ();

//
// map/compose
//

/**
 * Compose a function with a list, yielding a composite function
 * that uses the original function's result to index the list.
 * @code
 * f = compl({ $0 % 3 }, ["One", "Two", "Three"])
 * f(100)
 * "Two"
 * @endcode
 *
 * @param x function to determine which list index
 * @param y list of values
 * @return Value from list y whose index was determined by function x.
 */
intrinsic <X, Y> compl(x : X -> Int, y : [Y]) -> X -> Y;

/**
 * Compose a function with a map, yielding a composite function
 * that uses the original function's result to index the map.
 * @code
 * f = compm({ iif($0, #ok, #err) }, [#ok: "OK", #err: "ERR"])
 * f(false)
 * "ERR"
 * @endcode
 *
 * @param x function to determine which map key will be used to access map y
 * @param y map
 * @return Value from map y whose key was determined by function x.
 */
intrinsic <X, K, Y> compm(x : X -> K, y : [K : Y]) -> X -> Y;

/**
 * functional map: apply a function to each of a list of arguments,
 * yielding a congruent list of results. Currently this is
 * guaranteed to run serially. Aliased to infix operator |
 *
 * @param x list of values
 * @param y function that will be applied to each elemement of the list
 * @return list of values returned from calling y with each value in x.
 */
intrinsic <X, Y> map(x : [X], y : X -> Y) -> [Y];

/**
 * Apply a list of indexes to a list of items, yielding a selection list.
 * @param list of indexes
 * @param list of values
 * @return New list of values based on selected indexes.
 */
intrinsic <T> mapll(x : [Int], y : [T]) -> [T];

/**
 * Apply a list of keys to a map of items, yielding a selection list.
 * @param list of keys
 * @param map
 * @return New list of values based on selected keys.
 */
intrinsic <K,V> maplm(x:[K], y:[K : V]) -> [V];

/**
 * Apply a map of arguments to a function, yielding map of results
 * @param x map of arguments
 * @param y function that will be passed map value arguments
 * @return map of results after calling the function y with each map value.
 */
intrinsic <X,Y,Z> mapmf(x:[X : Y], y:(Y -> Z)) -> [X : Z];

/**
 * Apply a map of keys to a map of values, yielding a map of selected values.
 * @param x map of keys to be selected
 * @param y map
 * @return map of selected items from the map y
 */
intrinsic <K,V> mapml(x:[K : Int], y:[V]) -> [K : V];

/**
 * Apply a map of indexes to a list of items, yielding a map of selected items.
 * @param x map of indexes to be selected
 * @param y list of values
 * @return map of selected items from the list y
 */
intrinsic <X,Y,Z> mapmm(x:[X : Y], y:[Y : Z]) -> [X : Z];

/**
 * mapz(lists, f) == map(zip(lists), f), but doesn't
 * create the intermediate list of tuples.
 * @param x tuple of lists
 * @param y function
 */
intrinsic <T:[*], X> mapz(x:Tup(T | List), y:(Tup(T) -> X)) -> [X];

/**
 * parallel-map: apply a function to each of a list of arguments
 * in parallel. Aliased to infix operator |:
 *
 * @param x list of values
 * @param y function that will be applied to each elemement of the list
 * @return list of values returned from calling y with each value in x.
 */
intrinsic <X, Y> pmap(x : [X], y : X -> Y) -> [Y];

