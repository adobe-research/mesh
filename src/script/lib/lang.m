//
// Core language support
// TODO finish organizing
// TODO make type import completely automatic, then factor this into chunks
//
// - built-in types
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

// import * from types;
intrinsic type Bool;        // the usual...
intrinsic type Int;
intrinsic type Long;
intrinsic type Float;
intrinsic type Double;
intrinsic type String;
intrinsic type Symbol;      // #symbol
intrinsic type Opaque;      // used in conjunction with New for FFI hookups
intrinsic type Unit;        // singleton type with value (), sugar (also) ()

// kind: * -> *
intrinsic type Box;         // Box(T), sugar *T (for now)
intrinsic type New;         // type Nom = New(T) creates nominal type Nom over carrier T
intrinsic type List;        // List(T), sugar [T]

// kind: (*, *) -> *
intrinsic type Map;         // Map(K, V), sugar [K:V]
intrinsic type Fun;         // Fun(A, B), sugar A -> B

// kind: [*] -> *
intrinsic type Tup;         // Tup(<type list>), sugar (T1, T2, ...)

// kind: (*, [*]) -> *
intrinsic type Rec;         // Rec(<key type>, <type list>), sugar (k1:T1, k2:T2, ...)
intrinsic type Sum;         // Sum(<key type>, <type list>), sugar TBD

// type transformers
intrinsic type TMap;        // type-level map: TMap(<type list>, <type constructor>)
intrinsic type Index;       // experimental
intrinsic type Assoc;       // experimental

//
// conditional control flow
// TODO variants will change/augment these substantially
//

/**
 * Conditional execution: if condition c is true, run block t and
 * return the result, otherwise run block f and return the result.
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
// iteration/mapping
//

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
 * parallel-map: apply a function to each of a list of arguments
 * in parallel. Aliased to infix operator |:
 *
 * @param x list of values
 * @param y function that will be applied to each elemement of the list
 * @return list of values returned from calling y with each value in x.
 */
intrinsic <X, Y> pmap(x : [X], y : X -> Y) -> [Y];

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
// polymorphic equality/hash ops
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

/**
 * Hash code, defined over all values. Structural hash for everything
 * except lamdas and boxes, identity hash for those. Order-independent
 * hash for keyed values (maps and records).
 * @param x value
 * @return hash value
 */
intrinsic <T> hash(x : T) -> Int;

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
 * Constrained integer
 * @param lo minimum value
 * @param n value to constrain
 * @param hi maximum value
 * @return integer equal to max(lo, min(hi, n))
 */
constrain(lo, n, hi)
{
    max(lo, min(hi, n))
};

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
 * Throw an error with message if argument is false
 * @param x if value is false, throw error
 * @param y message
 */
intrinsic assert(x:Bool, y:String) -> ();

/**
 * async(f, cb) runs f asynchronously and calls cb with the result.
 */
async(f, cb) { spawn { cb(f()) } };

/**
 * @return Return number of available processors.
 */
intrinsic availprocs() -> Int;

/**
 * Create a box with an initial value.
 * @param x value to be boxed
 * @return boxed value
 */
intrinsic <T> box(x:T) -> *T;

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
 * Run a block in a transaction.
 */
intrinsic <T> do(x:(() -> T)) -> T;

/**
 * @param x Collection value
 * @returns true if collection or structure is empty.
 */
intrinsic <T> empty(x:T) -> Bool;

/**
 * Start running a block in a {@link Future} and return a lambda
 * that provides (blocking) access to the result.
 * @param x
 * @return lambda
 */
intrinsic <X> future(x:(() -> X)) -> (() -> X);

/**
 * @param x box
 * @return value of box
 */
intrinsic <T> get(x:*T) -> T; // unary *

/**
 * @param x tuple of boxes
 * @return tuple of values
 */
intrinsic <Ts:[*]> gets(x:Tup(Ts | Box)) -> Tup(Ts);

/**
 * identity function.
 * 
 */
id(v) { v };

/**
 * @param n int number.
 * @return incremented value of n.
 */
inc(n) { n + 1 };

/**
 * @return true if currently in a transaction, otherwise false.
 */
intrinsic intran() -> Bool;

/**
 * long addition
 * @param x long
 * @param y long
 * @return long value of x + y
 */
intrinsic <T> lplus(x:[T], y:[T]) -> [T];

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
intrinsic printstr(x:String) -> ();

/**
 * Set a box's value. Wraps itself in a transaction if none is running.
 * @param x box
 * @param y value
 */
intrinsic <T> put(x:*T, y:T) -> (); // :=

/**
 * Set a tuple of boxes values. Wraps itself in a transaction if none is running.
 * @param x tuple of boxes
 * @param y tuple of values
 */
intrinsic <T:[*]> puts(x:Tup(T | Box), y:Tup(T)) -> ();

/**
 * Create int list [start, ..., start + extent]
 * @param x start
 * @param y number of entries in the list
 * @return list of integers [start, ..., start + extent]
 */
intrinsic range(x:Int, y:Int) -> [Int];

/**
 * @param x Number of times to repeat item y.
 * @param y value
 * @return A list with the value y repeated x times.
 */
intrinsic <T> rep(x:Int, y:T) -> [T];

/**
 * @param b block.
 * @return the value returned by the block
 * @code run({"hello world"}) //returns "hello" @endcode
 */
run(b) { b() };

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

/**
 * print a string representation of any value.
 * goal is print/parse round trip for a well-defined
 * subset of values.
 * @param x value
 * @return string representation of the value x
 */
intrinsic <T> tostr(x:T) -> String;

/**
 * @param value 
 * @return a two-tuple containing "twins" of value.
 */
twin(v) { (v, v) };

/**
 * Run a block repeatedly while guard predicate returns true.
 * @param x predicate block
 * @param y function to execute while predicate is true.
 */
intrinsic <T> while(x:() -> Bool, y:() -> T) -> ();

//
// tuple service
//

<A, B> fst(p:(A, B)) { p.0 };
<A, B> snd(p:(A, B)) { p.1 };

