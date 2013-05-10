//
// Core language support
// TODO finish organizing
//
// - functions that implement unary and binary operators
// - functions that implement control flow combinators
// - other canonical language functions
//
// Most of these are intrinsics, but not necessarily.
//
// Note: a major incomplete language feature will provide
// typeclass-like ad hoc polymorphism. Until that's complete,
// overloading is unavailable and we have differently-named
// functions (as well as diffeent operators, ML-style) for
// e.g. Int and Double relational ops, as well as many missing
// variations (e.g., Long and Float support is mostly missing.)
//

/** {@link intrinsics.m} */
import * from intrinsics;

/** {@link types.m} */
import * from types;

//
// control
//

/**
 * runs the function in the map indicated by the selector,
 * and return the result.
 * TODO retype using keyset selector and rectype for cases
 *
 * @param selector key indicating a selector.
 * @param cases map of selectors to lambdas.
 * @return value returned by the selected function
 * @code
 * switch(0, [ 0:{"0"}, 1:{"1"}]) //returns "0"
 * @endcode
 */
<K, V> switch(sel : K, cases : [ K : () -> V ]) { cases[sel]() };

//
// math
//

/**
 * Constrained integer
 * @param lo minimum value
 * @param n value to constrain
 * @param hi maximum value
 * @return integer equal to max(lo, min(hi, n))
 */
constrain(lo : Int, n : Int, hi : Int) -> Int
{
    max(lo, min(hi, n))
};

// END SEMI-GROOMED PART

// ------------------------------------------------------------

/**
 * @param f function taking one parameter.
 * @param x parameter.
 * @return value returned by f when called with arg x
 * @code apply(round, 4.2) //returns 4 @endcode
 */
apply(f, x) { f(x) };

/**
 * async(f, cb) runs f asynchronously and calls cb with the result.
 * @param f first function to run
 * @param cb second function to run using the result from f
 */
async(f, cb) { spawn { cb(f()) } };

/**
 * compose two functions. aliased to infix ($)
 * 
 * @param f function.
 * @param g function.
 * @return a function g(f()), the composition of f and g
 * @code compose(fsq,round)(4.2) //returns 18 @endcode
 * @code apply(fsq$round,4.2)    //also returns 18 @endcode because compose is aliased to infix $
 */
compose(f, g) { { x => g(f(x)) } }; // $

/**
 * @param n int number.
 * @return decremented value of n.
 */
dec(n) { n - 1 };

/**
 * identity function.
 * @param v value
 * @return returns the value v
 */
id(v) { v };

/**
 * @param n int number.
 * @return incremented value of n.
 */
inc(n) { n + 1 };

/**
 * @param b block.
 * @return the value returned by the block
 * @code run({"hello world"}) //returns "hello" @endcode
 */
run(b) { b() };

/**
 * @param value 
 * @return a two-tuple containing "twins" of value.
 */
twin(v) { (v, v) };

//
// tuple service
//

/**
 * @param p tuple value
 * @return returns the value in the tuple at position 0
 */
<A, B> fst(p:(A, B)) { p.0 };

/**
 * @param p tuple value
 * @return returns the value in the tuple at position 1
 */
<A, B> snd(p:(A, B)) { p.1 };

