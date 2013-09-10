//
// Core language support
//
// Mixture of intrinsic prototypes and full definitions.
// Includes all backing functions for operators, as noted
// in comments (though the actual linkage between operator
// lexemes and functions is elsewhere, per below to-do item).
// Note: interfaces will change the character of this a bit.
//
// TODO factor into chunks once we support import circularity.
// TODO declare operator backing functions in source.
// TODO groom comments and type annotations
//

// ------------------------------------------------------------------

//
// built-in types
// TODO add kind annotation to type decl syntax
//

// kind: *
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
intrinsic type Var;         // Var(<key type>, <type list>), sugar (k1!T1, k2!T2, ...)

// type transformers
intrinsic type TMap;        // type-level map: TMap(<type list>, <type constructor>)
intrinsic type Assoc;       // experimental
intrinsic type Cone;        // experimental

type Pred(T) = T -> Bool;


// ------------------------------------------------------------------

//
// conditional execution
//

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
 * If condition c is true, run block t, otherwise run block f.
 * Return the result in either case.
 * @param c boolean condition
 * @param t lambda to execute if c is true
 * @param f lambda to execute if c is false
 * @return t() if c is true, otherwise f()
 */
intrinsic <T> if(c : Bool, y : () -> T, z : () -> T) -> T;

/**
 * If-else combinator. Given a list of guard/action
 * pairs and a default action, run each guard in turn until one
 * returns true, then execute the corresponding action.
 * If no guard returns true, execute the default action.
 * Return the result of the executed action.
 * TODO enable inlining, which probably means making intrinsic
 * @param cases list of guard/action pairs
 * @param default default action
 * @return result of executed action
 */
<T> ifelse(cases : [(() -> Bool, () -> T)], default : () -> T) -> T
{
    n = size(cases);
    c = cycle(0, { i => i < n && { !cases[i].0() } }, inc);
    if(c == n, default, { cases[c].1() })
};

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
 * Conditional execution of a block of code iff predicate is true.
 * @param c predicate boolean condition
 * @param b block to execute
 */
intrinsic <T> when(c : Bool, b : () -> T) -> ();

/**
 * generalized conditional. sugared to infix ?
 */
intrinsic <K, V:[*], R>
    cond(sel : Var(Assoc(K, V)), cases : Rec(Assoc(K, Cone(V, R)))) -> R;

/**
 * runs the function in the map indicated by the selector,
 * and return the result.
 *
 * @param selector key indicating a selector.
 * @param cases map of selectors to lambdas.
 * @return value returned by the selected function
 * @code
 * switch(0, [ 0:{"0"}, 1:{"1"}]) //returns "0"
 * @endcode
 */
<K, V> switch(sel : K, cases : [ K : () -> V ]) { cases[sel]() };

// ------------------------------------------------------------------

//
// iteration, reduction, convergence etc.
//

/**
 * iterate an endofunction funv, starting with initial value init, until either initial value
 * or a fixed point is reached. Return penultimate value from func.
 *
 * E.g.
 *  converge({inc(<n>) % 10}, 0) == 9
 *
 *
 * @param func function.
 * @param init object.
 * @return penultimate value returned by func
 * @code converge({inc(<n>) % 10}, 0) //returns 9 @endcode
 * @code converge({1.0 + 1.0 /. <f>}, 1.0) //returns 1.618033988749895 @endcode
 * TODO: syntax of parms changing asap
 */
converge(func, init)
{
	diff(x, y) { x != y && { y != init } };
	next(x, y) { (y, func(y)) };
	cycle((init, func(init)), diff, next).0
};

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
intrinsic <T> cycle(v : T, p : T -> Bool, f : T -> T) -> T;

/**
 * Iterate on a function n times.
 *
 * @param v Initial value of v that will be passed to the f lambda.
 * @param n Limit of how many times f function will be called.
 * @param f Function that accepts passed in value T and returns the next value T that will
 *        be fed back into the function as long as upper limit has not been reached.
 * @return Value of v after the given number of iterations.
 */
intrinsic <T> cyclen(v : T, n : Int, f : T -> T) -> T;

/**
 * Starting with an initial value, "evolve" a new one by calling a
 * function on each element of an input list. The function calculates
 * a new answer. Evolve is reduce with reordered parameters, which
 * seems more readable in many situations.
 *
 * @param state initial state value.
 * @param tran transformer function, takes a state value and an input and returns
 *          a new state value
 * @param inputs list of inputs
 * @return final state value
 * @see reduce
 */
<A, B> evolve(state : A, tran : (A, B) -> A, inputs : [B]) -> A
{
    reduce(tran, state, inputs)
};

/**
 * Evolve until stop condition is met, or inputs
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
 * @param indexes list of index values
 * @param body function to be applied to each index value
 * @return
 */
intrinsic <A, B> for(indexes : [A], body : A -> B) -> ();

/**
 * iter is degenerate while. Execute the predicate until it produces a false value.
 * @param p predicate for empty while loop
 */
iter(p : () -> Bool) -> ()
{
    while(p, {()})
};

/**
 * Perform a functional reduction (foldl) using the given reducer,
 * initial value, list of arguments and mapping function. I.e.,
 * mapred(red, init, args, mapper) gives the same result as
 * reduce(red, init, args | mapper).
 *
 * @param f reducing function
 * @param v initial value
 * @param l list of arguments
 * @param m mapping function
 * @return value produced by calling f repeatedly with previously
 *         calculated value and mapped result of next item in the list
 */
intrinsic <A, B, C> mapred(f : (A, B) -> A, v : A, l : [C], m : C -> B) -> A;

/**
 * Evaluate filter(list, pred) in parallel chunks of the given size.
 * so pfiltern(list, pred, size(list)) gives the same result as
 * filter(list, pred)
 * @param list list of items
 * @param pre Predicate function to determine if list item should be returned.
 * @param n number of chunks to chop the list into in order to process in parallel
 * @return Sublist of x where predicate returned true.
 */
pfiltern(list, pred, n)
{
    flatten(chunks(list, n) |: { filter($0, pred) })
};

/**
 * Parallel-for: apply a function to each of a list of index
 * arguments in parallel, discarding any results.
 * @param x list of values
 * @param y function
 * @return
 */
intrinsic <X, Y> pfor(x : [X], y : X -> Y) -> ();

/**
 * evaluate for(list, f) in parallel chunks of the given size.
 * @param list list of items
 * @param f function to process each item in the list
 * @param n number of chunks to chop the list into in order to process in parallel
 */
<A, B> pforn(list : [A], f : A -> B, n : Int) -> ()
{
    pfor(chunks(list, n), { for($0, f) })
};

/**
 * Evaluate reduce(f, v, l) in parallel, using the given number of tasks.
 * Note that this reduce is constrained in two ways, only one of which
 * is enforced by the type system:
 * 1. reducer function f must take and return arguments of the same type.
 * 2. reducer function must be associative.
 * Computation proceeds by reducing sublists in parallel, then reducing
 * the results of those subreductions. Argument v is used as the initial
 * value of all reductions.
 * @param f reducer function
 * @param v initial value
 * @param l list of arguments
 * @return value produced by reducing in parallel as described above.
 */
<A> preducen(f : (A, A) -> A, v : A, l : [A], n : Int) -> A
{
    reduce(f, v, chunks(l, n) |: { reduce(f, v, $0) })
};

/**
 * Evaluate where(list, pred) in parallel, using the given number of tasks.
 * @param list list of items
 * @param pred predicate over list items
 * @param ntasks number of parallel tasks to use when evaluating
 * @return list of indexes in the base list where the predicate function returned true
 */
<T> pwheren(list : [T], pred : T -> Bool, ntasks : Int) -> [Int]
{
    cps = cutpoints(list, ntasks);
    ixs = zip(cut(list, cps), cps) |: { where($0, pred) @+ $1 };
    flatten(ixs)
};

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
 * like evolve_while, except returns a list of all results produced
 * during iteration, not just the final one. if pred returns
 * false immediately the empty list is returned.
 *
 * @param pred predicate function. returns false when done.
 * @param init value.
 * @param f function that is called for each element of list
 * @param args args
 *
 */
scan_while(pred, init, f, args)
{
    result = evolve_while(last $ pred, [init], { as, b => append(as, f(last(as), b)) }, args);
    drop(1, result)
};

/**
 * traced version of converge: returns a list of results, starting
 * with the initial value init, and ending with the result.
 * @see converge
 *
 * @param func function.
 * @param init object.
 * @return list recording all values returned by func on way to convergence.
 */
tconverge(func, init)
{
    diff(accum, x, y) { x != y && { y != init } };
    next(accum, x, y) { (append(accum, y), y, func(y)) };
	  cycle(([init], init, func(init)), diff, next).0
};

/**
 * Traced version of {@link #cycle}:
 * <code>last(trace(v, p, f)) == cycle(v, p, f)</code>.
 * @param v initial value
 * @param p predicate function
 * @param f iterator function
 * @return list of results from each iteration, beginning with initial value v.
 */
trace(v, p, f)
{
    cycle([v], last $ p, { append($0, f(last($0))) })
};

/**
 * Traced version of {@link #cyclen}:
 * <code>last(tracen(v, n, f)) == cyclen(v, n, f)</code>.
 * @param v initial value
 * @param n number of iterations
 * @param f iterator function
 * @return list of results from each iteration, beginning with initial value v.
 */
tracen(v, n, f)
{
    cyclen([v], n, { append($0, f(last($0))) })
};

/**
 * Run a block repeatedly while guard predicate returns true.
 * @param pred predicate
 * @param body block to execute while predicate is true.
 * @return
 */
intrinsic <T> while(pred : () -> Bool, b : () -> T) -> ();

// ------------------------------------------------------------------

//
// map, compose, other combinators
//

/**
 * @param f function taking one parameter.
 * @param x parameter.
 * @return value returned by f when called with arg x
 * @code apply(round, 4.2) //returns 4 @endcode
 */
<A, B> apply(f : A -> B, x : A) -> B
{
    f(x)
};

/**
 * compose two functions. aliased to infix ($)
 *
 * @param f function.
 * @param g function.
 * @return a function g(f()), the composition of f and g
 * @code compose(fsq,round)(4.2) //returns 18 @endcode
 * @code apply(fsq$round,4.2)    //also returns 18 @endcode because compose is aliased to infix $
 */
<A, B, C> compose(f : A -> B, g : B -> C) -> (A -> C)
{
    { g(f($0)) }
};

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
 * Transformer for binary functions, returns a version of the
 * given function vectorized over its left (first) argument.
 * E.g. given <code>f : (X, Y) -> Z</code>, <code>eachleft(f)</code>
 * returns a function of type <code>([X], Y) -> [Z]</code>,
 * which returns the results of applying <code>f(x, y)</code>
 * for each <code>x : X</code>.
 * The prefix attribute <code>@</code> on infix operators desugars
 * to <code>eachleft</code>,
 * e.g. <code>xs @+ y</code> => <code>eachleft(+)(xs, y)</code>.
 * @param f binary function to transform
 * @return transformed function
 */
intrinsic <A, B, C> eachleft(f : (A, B) -> C) -> ([A], B) -> [C];

/**
 * Transformer for binary functions, returns a version of the
 * given function vectorized over its right (second) argument.
 * E.g. given <code>f : (X, Y) -> Z</code>, <code>eachright(f)</code>
 * returns a function of type <code>(X, [Y]) -> [Z]</code>,
 * which returns the results of applying <code>f(x, y)</code>
 * for each <code>y : Y</code>.
 * The postfix attribute <code>@</code> on infix operators desugars
 * to <code>eachright</code>,
 * e.g. <code>x +@ ys</code> => <code>eachright(+)(x, ys)</code>.
 * @param f binary function to transform
 * @return transformed function
 */
intrinsic <A, B, C> eachright(f : (A, B) -> C) -> (A, [B]) -> [C];

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

/**
 * evaluate (list | f) in parallel chunks of the given size:
 * pmapn(list, f, 1) == pmap(list, f) == f |: list
 * pmapn(list, f, size(list)) == map(list, f) == f | list.
 * @param list list of values
 * @param f function to process each value in the list
 * @param n number of chunks to chop the list into in order to process in parallel
 * @return list of values return from processing each item in original list with f
 */
pmapn(list, f, n)
{
    flatten(chunks(list, n) |: { $0 | f })
};

/**
 * @param b block.
 * @return the value returned by the block
 * @code run({"hello world"}) //returns "hello" @endcode
 */
run(b) { b() };

/**
 * identity function.
 * @param v value
 * @return returns the value v
 */
id(v) { v };

/**
 * Simple function memoizer. Results are stored in a simple
 * argument-indexed map, which grows unrestrictedly.
 * @param f a function
 * @return a function with the same signature as f, which saves
 * and reuses results.
 * TODO require f be a pure function once constraint is available
 */
memo(f)
{
    m = box([:]);

    { a =>
        if(iskey(*m, a), { (*m)[a] }, {
            v = f(a);
            m <- { mapset($0, a, v) };
            v
        })
    }
};

/**
 * TODO memoizer using variant-returning map lookup function
 *

// map get with variant result
// intrinsify
look(m, k) {
    if(iskey(m,k), {true ! m[k]}, {false ! ()})
};

memoize(f) {
    m = box([:]);
    { a =>
        look(*m, a) ? (true: id, false: {
            v = f(a);
            m <- { mapset($0, a, v) };
            v
        })
    }
};

 *
 */


// ------------------------------------------------------------------

//
// logic binops
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

// ------------------------------------------------------------------

//
// equality binops
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

// ------------------------------------------------------------------

//
// core poly funcs
//

/**
 * @param x value of any type
 * @returns true if value is empty in a type-specific sense
 */
intrinsic <T> empty(x : T) -> Bool;

/**
 * Hash code, defined over all values. Structural hash for everything
 * except lamdas and boxes, identity hash for those. Order-independent
 * hash for keyed values (maps and records).
 * @param x value
 * @return hash value
 */
intrinsic <T> hash(x : T) -> Int;

// ------------------------------------------------------------------

//
// int relops
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

// ------------------------------------------------------------------

//
// int arith ops
// (plus doesn't quite fit here, but see comment header)
//

/**
 * Integer division. Also aliased to infix operator /
 * @param x divisor
 * @param y denominator
 * @returns quotient
 */
intrinsic div(x : Int, y : Int) -> Int;

/**
 * Integer difference. Also aliased to infix operator -
 * @param x int
 * @param y int
 * @return int value of x - y
 */
intrinsic minus(x : Int, y : Int) -> Int;

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
 * Integer pow(). Also aliased to infix operator ^
 * @param x int
 * @param y int
 * @return integer value of x raised to the power of y
 */
intrinsic pow(x : Int, y : Int) -> Int;

/**
 * Integer multiplication. Also aliased to infix operator *
 * @param x int
 * @param y int
 * @return int value of x * y
 */
intrinsic times(x : Int, y : Int) -> Int;

// ------------------------------------------------------------------

//
// other core int arithm
//

/**
 * @param n int number.
 * @return absolute value of n.
 */
abs(n)
{
    guard(n >= 0, n, {-n})
};

/**
 * Constrain an integer between two (inclusive) bounds
 * @param lo minimum value
 * @param n value to constrain
 * @param hi maximum value
 * @return integer equal to max(lo, min(hi, n))
 */
constrain(lo : Int, n : Int, hi : Int) -> Int
{
    max(lo, min(hi, n))
};

/**
 * @param n int number.
 * @return decremented value of n.
 */
dec(n) { n - 1 };

/**
 * (/) with divide-by-zero guard.
 *
 * @param n int number
 * @param d int divisor
 * @return n/d except when d equals 0 when it returns 0
 */
divz(n,d) { guard(d == 0, 0, {n / d}) };

/**
 *
 */
even(n) { n % 2 == 0 };

/**
 * @param n int number.
 * @return incremented value of n.
 */
inc(n) { n + 1 };

/**
 * @code
 * inrange(4, 2, 5) // (4 >= 2) && (4 < (2+5)) -> true
 * @endcode
 * @param x int number
 * @param base int base of range
 * @param extent int extent of range
 * @return x within range [base, base + extent).
 */
inrange(x, base, extent) { x >= base && { x < base + extent } };

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
 * (%) with divide-by-zero guard.
 *
 * @param n int number
 * @param d int divisor
 * @return n % d except when d equals 0 when it returns 0
 */
modz(n,d) { guard(d == 0, 0, {n % d}) };

/**
 *
 */
odd(n) { n % 2 != 0 };

/**
 * Integer sign.
 * @param x value
 * @return Returns 1 if x > 0, 0 if x == 0, otherwise -1.
 */
intrinsic sign(x : Int) -> Int;

/**
 * @param i int
 * @return int i * i
 */
sq(i) { i * i };

// ------------------------------------------------------------------

//
// long support -- such as it is currently
// TODO wait to flesh out until interfaces
//

/**
 * Long difference
 * @param x long
 * @param y long
 * @return long value of x - y
 */
intrinsic lminus(x : Long, y : Long) -> Long;

// ------------------------------------------------------------------

//
// floating-point (i.e., Double) relops
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

// ------------------------------------------------------------------

//
// floating-point arith
// note that fplus() is subsumed by plus() currently, but that'll change
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

// ------------------------------------------------------------------

//
// other core floating-point arith
//

/**
 * {@link Math#exp(double)}
 * @param x the exponent to raise e to
 * @return Euler's number number raised to the power of x.
 */
intrinsic exp(x:Double) -> Double;

/**
 * @param f float number.
 * @return float absolute value of f.
 */
fabs(f)
{
    guard(f >=. 0.0, f, {0.0 -. f})
};

/**
 * @code
 * finrange(4.0, 2.0, 5.0) // (4.0 >= 2.0) && (4.0 < (2.0+5.0)) -> true
 * @endcode
 * @param f float number
 * @param fbase float base of range
 * @param fextent float extent of range
 * @return float f within range [base, base + extent).
 */
finrange(f, fbase, fextent) { f >=. fbase && { f <. fbase + fextent } };

/**
 * @param x float number
 * @param y float number
 * @return maximum of x,y
 */
fmax(x,y) { iif(x >=. y, x, y) };

/**
 *
 * @param x float number
 * @param y float number
 * @return minimum of x,y
 */
fmin(x,y) { iif(x >=. y, y, x) };

/**
 * sign of float.
 * @param f float number.
 * @return 1 for all positive values larger than 0.0,
 * -1 for all negative values smaller than 0.0
 * 0 for 0.0 and -0.0
 */
fsign(f) { guard(f >. 0.0, 1, {iif(f <. 0.0, -1, 0)}) };

/**
 * float (/) with divide-by-zero guard.
 *
 * @param n float number
 * @param d float divisor
 * @return n/d except when d equals 0.0 when it returns 0.0
 */
fdivz(n,d) { guard(d == 0.0, 0.0, {n /. d}) };

/**
 *   float (%) with divide-by-zero guard.
 *
 * @param n float number
 * @param d float divisor
 * @return n % d except when d equals 0.0 when it returns 0.0
 */
fmodz(n,d) { guard(d == 0.0, 0.0, {n %. d}) };

/**
 * @param f float number
 * @return float f * f
 */
fsq(f) { f *. f };

/**
 * Natural log
 * @param x
 * @return the natural logarithm of x
 */
intrinsic ln(x:Double) -> Double;

/**
 * Log base b.
 * @param b floating-point base
 * #param n floating-point number
 * @return the log of n base b
 */
log(b, n)
{
    ln(n) /. ln(b)
};

/**
 * Integer log base 2.
 * #param n an integer
 * @return the log of n base 2
 */
intrinsic ilog2(n:Int) -> Int;

/**
 * round.
 *
 * @param f float number
 * @return f rounded to nearest int.
 */
round(f) { f2i(f + 0.5) };

/**
 * @param x value
 * @return square root of value.
 */
intrinsic sqrt(x : Double) -> Double;

// ----------------------------------------------------------------------------

//
// trig
//

/**
 * Returns the angle theta from the conversion of rectangular
 * coordinates (x, y) to polar coordinates (r, theta).
 * Wraps {@link Math#atan2(double, double)}.
 * @param y the ordinate coordinate
 * @param x the abscissa coordinate
 * @return
 */
intrinsic atan2(y : Double, x : Double) -> Double;

/**
 * Wraps {@link Math#cos(double)}.
 * @param x the angle
 * @return cosine of the angle
 */
intrinsic cos(x:Double) -> Double;

/**
 * Wraps {@link Math#sin(double)}.
 * @param x the angle
 * @return sine of the angle
 */
intrinsic sin(x:Double) -> Double;

/**
 * Wraps {@link Math#tan(double)}.
 * @param x angle
 * @return tangent of angle x
 */
intrinsic tan(x:Double) -> Double;

// ------------------------------------------------------------------

//
// bitwise ops
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

/**
 * Bitwise XOR
 * @param x
 * @param y
 * @return logical exclusive OR of each bit in x and y
 */
intrinsic bxor(x:Int, y:Int) -> Int;

/**
 * Bit shift right with sign extension
 * @param v
 * @param amt
 * @return v shifted right by amt with sign extension
 */
intrinsic shiftr(v:Int, amt:Int) -> Int;

/**
 * Bit shift right with zero fill
 * @param v
 * @param amt
 * @return v shifted right by amt, filled with 0
 */
intrinsic ushiftr(v:Int, amt:Int) -> Int;

/**
 * Bit shift left with zero fill
 * @param v
 * @param amt
 * @return v shifted left by amt, filled with 0
 */
intrinsic shiftl(v:Int, amt:Int) -> Int;

// ------------------------------------------------------------------

//
// String functions
//

/**
 * {@link String#endsWith(String)}
 * @param x base string
 * @param y suffix string
 * @return true if string x ends with string y
 */
intrinsic endswith(x:String, y:String) -> Bool;

/**
 * {@link String#startsWith(String)}
 * @param x base string
 * @param y prefix string
 * @return true if string x starts with string y
 */
intrinsic startswith(x:String, y:String) -> Bool;

/**
 * concatenate strings
 * @param x list of strings
 * @return a new string containing all of the strings in x concatenated together
 */
intrinsic strcat(x:[String]) -> String;

/**
 * compare strings lexicographically
 * {@link String#compareTo(String)}
 * @param x string
 * @param y string
 * @return Return 0 if strings are lexicographically equal, less than zero if string x
 *         is lexicographically less than string y, and greater than zero if string x
*          is lexicographically greater than string x.
*/
intrinsic strcmp(x:String, y:String) -> Int;

/**
 * @param x string value
 * @param y list in indexes
 * @return A list of strings based on original string x but sliced at provided index points.
 */
intrinsic strcut(x:String, y:[Int]) -> [String];

/**
 * Drops first x chars if x > 0, last -x if x < 0.
 * n is held to string size.
 *
 * @param x Number of chars to drop from the string.
 *          x > 0 drops first x chars
 *          x < 0 drops x chars from the end of the string
 *          x is held to the length of the string, x > y.length() will be treated as y.length()
 * @param y string
 * @return new string with specified amount of characters dropped
 */
intrinsic strdrop(x:Int, y:String) -> String;

/**
 * @param x base string
 * @param y search string
 * @return returns the location of the search string within the base string,
 *         otherwise returns the length of the base string if search string is not found.
 */
intrinsic strfind(x:String, y:String) -> Int;

/**
 * Joins a list of strings using the separator.
 * @param x list of strings
 * @param y separator
 * @return new string
 */
intrinsic strjoin(x:[String], y:String) -> String;

/**
 * @param x string
 * @return length of string
 */
intrinsic strlen(x:String) -> Int;

/**
 * @param x base string
 * @param y regular expression delimiter
 * @return List of strings based on splitting the base string using the regular expression
 */
intrinsic strsplit(x:String, y:String) -> [String];

/**
 * Takes first x chars if x > 0, last -x if x < 0.
 * If x > than y.length() then wraps around to begining of y.
 *
 * @param x Number of chars to take from the string.
 *          x > 0 takes first x chars
 *          x < 0 takes x chars from the end of the string
 *          If x > than y.length() then wraps around to begining of y.
 * @param y string
 * @return new string with specified number of chars from the original string
 */
intrinsic strtake(x:Int, y:String) -> String;

/**
 * @param x base string
 * @param y function that accepts each character of the string x and returns a boolean
 *           value for whether or not to return the position of the character in the string
 * @return List of indexes in the base string where the predicate function returned a true value.
 */
intrinsic strwhere(x:String, y:String -> Bool) -> [Int];

/**
 * @param x base string
 * @param y start index
 * @param z length
 * @return a new substring of the base string, starting at index y and an end point of y + z
 */
intrinsic substr(x:String, y:Int, z:Int) -> String;

/**
 * convert a Symbol to a string
 * @param x Symbol value
 * @return string representation of the symbol x.
 */
intrinsic sym2s(x:Symbol) -> String;

/**
 * @param x string to convert
 * @return the string x converted to lowercase
 */
intrinsic tolower(x:String) -> String;

/**
 * @param x string to convert
 * @return the string x converted to uppercase
 */
intrinsic toupper(x:String) -> String;

// ------------------------------------------------------------------

//
// primitive value converters
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
 * Convert double value to int
 * @param x double value
 * @return take floor of double, return int
 */
intrinsic f2i(x:Double) -> Int;

/**
 * Convert float to string value.
 * @param x double value
 * @return String representation of double.
 */
intrinsic f2s(x:Double) -> String;

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

// ------------------------------------------------------------------

//
// lists
//

/**
 * Append an item to the end of a list.
 * @param x list
 * @param y item to append to the list
 * @return New list with y appended to the end.
 */
intrinsic <T> append(x:[T], y:T) -> [T];

/**
 * Given list and desired number of chunks, return list of chunk sublists.
 * Returned number of chunks returned will equal requested number of chunks,
 * except when requested quantity is negative, or exceeds number of list items.
 * When requested count is zero, an empty list is returned.
 * When list size is a multiple of requested count, chunks will be of equal size.
 * Otherwise, chunks will be within 1 item of each other in size.
 * Items returned will be in original order, so that
 * <code>flatten(chunks(list, n)) == list</code> (when n > 0).
 * @param list list to chunk
 * @param nchunks desired number of chunks
 * @return list of chunk sublists.
 */
<T> chunks(list : [T], nchunks : Int) -> [[T]]
{
    cut(list, cutpoints(list, nchunks))
};

/**
 * @param list list of items to scan
 * @param item value to look for in the list of items
 * @return true if list contains given item.
 */
contains(list, item) { find(list, item) < size(list) };

/**
 * @param n natural number to count to. If a negative Int is passed,
 *      its absolute value will be used.
 * @return list of Ints [0, ..., n - 1]
 */
intrinsic count(n : Int) -> [Int];

/**
 * Cut the given list into sublist, at the given indexes.
 * Indexes must be between 0 and size(list), in ascending
 * order. Duplicate indexes are allowed and will produce
 * an empty sublist for the earlier instance(s) of the duplicate.
 * A final index equal to size(list) will produce an empty final
 * sublist.
 * @param list list of items
 * @param cutpoints list of index cut points
 * @return a list of sublists, as described above
 */
intrinsic <T> cut(list : [T], cutpoints : [Int]) -> [[T]];

/**
 * Given a list and a desired number of cutpoints, return list of cutpoints.
 * Returned number of cutpoints returned will equal requested number of cutpoints,
 * except when requested quantity is negative, or exceeds number of list items.
 * When requested count is zero, an empty list is returned.
 * When list size is a multiple of requested count, cutpoints will be equally spaced.
 * Otherwise, cutpoints will be within 1 unit of each other in spacing.
 * @param list list to cut
 * @param ncuts desired number of cutpoints
 * @return list of cutpoints
 */
<T> cutpoints(list : [T], ncuts : Int) -> [Int]
{
    s = size(list);
    n = constrain(0, ncuts, s);
    (c, r) = (divz(s, n), modz(s, n));
    sizes = rep(n - r, c) + rep(r, c + 1);
    starts(sizes)
};

/**
 * @param x List of items.
 * @return Sublist of unique items from list.
 */
intrinsic <T> distinct(x:[T]) -> [T];

/**
 * Generate a list of n random ints in the range 0..max - 1
 * @param x Size of list to return
 * @param y Upper limit of random value minus 1
 * @return List of n random ints in the range 0..max - 1
 */
intrinsic draw(x : Int, y : Int) -> [Int];

/**
 * Drops first x if x > 0, last -x if x < 0 items from list.
 * n is held to list size.
 *
 * @param x Number of items to drop from the list.
 *          x > 0 drops first x items
 *          x < 0 drops x items from the end of the list
 *          x is held to the length of the list, x > size(y) will be treated as size(y)
 * @param y List of of values
 * @return Sublist of values.
 */
intrinsic <T> drop(x:Int, y:[T]) -> [T];

/**
 * evaluate f at each position of a list, using the value at that position
 * as the left argument, and the next value as the right argument. Begin with
 * f(init, head(list)).
 * @param init initial value for left argument to f
 * @param f function to be evaluated
 * @param args list of values
 * @param list of values returned by evaluating f over this list of values.
 */
eachpair(init, f, args)
{
    list = [init] + args;
    index(args) | { i => f(list[i], list[i + 1]) }
};

/**
 * returns indexes where list changes value
 * @param list list of values
 * @return returns list indexes where list changes value
 */
edges(list)
{
    [0] + filter(drop(1, index(list)), { list[$0 - 1] != list[$0] })
};

/**
 * create a singleton list from a value
 * @param v value
 * @return a list of one item which is the value v
 */
enlist(v) { [v] };

/**
 * Cut a list into sublists of the given length (last might be ragged).
 * If a length of zero is passed, an empty list is returned. If a length
 * greater than the length of the list is passed, the result is the same
 * as when a length equal to the length of the list is passed, i.e. a
 * singleton collection containing the entire list as a sublist. If
 * a negative length is passed, its absolute value is taken.
 * @param list list of items
 * @param len desired sublist length
 * @return list of equal-length sublists (last might be ragged)
 */
filet(list, len)
{
    s = size(list);
    n = guard(len >= 0, len, { -len });
    slices = divz(s, n) + sign(modz(s, n));
    cut(list, count(slices) @* n)
};

/**
 * Filter a list of items.
 * @param x list of items
 * @param y Predicate function to determine if list item should be returned.
 * @returns Sublist of x where predicate returned true.
 */
intrinsic <T> filter(x:[T], y:(T -> Bool)) -> [T];

/**
 *
 * @param x List of values.
 * @param y Value to search for.
 * @return Index of first occurence of item in list, or list size
 */
intrinsic <T> find(x:[T], y:T) -> Int;

/**
 * given a predicate and a list of values, return the position of the
 * first item that satisfies the predicate. If none does, return the
 * size of the list
 *
 * @param vals list of values.
 * @param pred predicate.
 */
first_where(pred, vals)
{
    n = size(vals);
    cycle(0, { i => i < n && { !pred(vals[i]) } }, inc)
};

/**
 * Concatenate multiple lists together.
 * @param x List of lists
 * @returns Concatenated lists.
 */
intrinsic <T> flatten(x:[[T]]) -> [T];

/**
 * Create list of ints [from..to], ascending or descending
 * @param x start value of the list, if x is greater than y then
 *          list will be in descending order.
 * @param y end value of the list
 * @return sequential list of int values.
 */
intrinsic fromto(x:Int, y:Int) -> [Int];

/**
 * group by - given a list of keys and a list of values,
 * returns a map from keys to collections of values.
 * Note that we roll over the key list.
 *
 * @param keys list of keys
 * @param vals list of values
 * @return A map from keys to collections of values.
 */
intrinsic <K, V> group(keys : [K], vals : [V]) -> [K : [V]];

/**
 * @param x List
 * @return head element of non-empty list.
 */
intrinsic <T> head(x:[T]) -> T;

/**
 * Create a list of indexes: index(list) == count(size(list)).
 * @param list
 * @return a list of the indexes of a list.
 */
intrinsic <T> index(list : [T]) -> [Int];

/**
 * Determine of value is a valid list index.
 * @param i int value
 * @param list list of values
 * @return true if i is a valid index of list, otherwise false.
 */
intrinsic <T> isindex(i : Int, list : [T]) -> Bool;

/**
 * TODO: empty list throws
 * @param list List
 * @return Last element of non-empty list.
 */
intrinsic <T> last(list : [T]) -> T;

/**
 * list concatenation
 * @param x long
 * @param y long
 * @return long value of x + y
 */
intrinsic <T> lplus(x:[T], y:[T]) -> [T];

/**
 * Return new list with original list's contents,
 * but with value at index replaced.
 * @param x list of values
 * @param y index
 * @param z new value
 * @return New list with original list's contents, but with value at index replaced.
 */
intrinsic <T> listset(x:[T], y:Int, z:T) -> [T];

/**
 * Return new list with original list's contents,
 * but with values at indexes replaced.
 * Note that we roll over the value list.
 * @param x list of values
 * @param y list of indexes
 * @param z list of new values
 * @return New list with original list's contents, but with values at indexes replaced.
 */
intrinsic <T> listsets(x:[T], y:[Int], z:[T]) -> [T];

/**
 * partition a list of items by the value of a function at those items
 * @param vals list of items to be partioned
 * @param f function that partions the list based on return value of this function
 * @return a map with keys that are the return values of f and value is a list of items
 *         from vals that produced the key value when passed into f.
 */
part(vals, f) { group(vals | f, vals) };

/**
 * partition, running partition function in parallel for each list element.
 * @param vals list of items to be partioned
 * @param f function that partions the list based on return value of this function
 * @return a map with keys that are the return values of f and value is a list of items
 *         from vals that produced the key value when passed into f.
 */
ppart(vals, f)
{
    group(vals |: f, vals)
};

/**
 * partition, running partition function on list chunks of a given size.
 * @param vals list of items to be partioned
 * @param f function that partions the list based on return value of this function
 * @param n number of chunks to chop the list into in order to process in parallel
 * @return a map with keys that are the return values of f and value is a list of items
 *         from vals that produced the key value when passed into f.
 */
ppartn(vals, f, n)
{
    group(flatten(chunks(vals, n) |: { $0 | f }), vals)
};

/**
 * Create int list [start, ..., start + extent]
 * @param x start
 * @param y number of entries in the list
 * @return list of integers [start, ..., start + extent]
 */
intrinsic range(x:Int, y:Int) -> [Int];

/**
 *
 * @param n number.
 * @param f function.
 * @return a list containing the result of evaluating f() n times.
 */
repeat(n, f) { rep(n, ()) | f };

/**
 * Removes all occurences of an item from a list.
 * @param x list of items
 * @param y item to be removed
 * @return new list with all occurences of item y removed.
 */
intrinsic <T> remove(x:[T], y:T) -> [T];

/**
 * @param x Number of times to repeat item y.
 * @param y value
 * @return A list with the value y repeated x times.
 */
intrinsic <T> rep(x:Int, y:T) -> [T];

/**
 * list reverse
 * @param list list of items
 * @return a new list with the items in reverse order
 */
reverse(list) {
    n = size(list);
    index(list) | { list[n - 1 - $0] }
};

/**
 * rotate list
 * @param list list of items
 * @param n number of positons to shift the list, positive value shifts to the right
 *          negative numbers shift to the left
 * @return new list with items rotated
 */
rotate(list, n)
{
    if(n >= 0,
        {take(-n, list) + drop(-n, list)},
        {drop(-n, list) + take(-n, list)})
};

/**
 * return list containing lengths of runs of equal values
 * @param list list of values
 * @return list containing lengths of runs of equal values within the list
 */
runlens(list)
{
    eachpair(0, { $1 - $0 }, drop(1, append(edges(list), size(list))))
};

/**
 * group list into runs of adjacent equal items
 * @param list list of values
 * @return list of lists of adjacent equal items that are within the original list
 * @code runs([1,1,4,2,2,2]) returns [[1, 1], [4], [2, 2, 2]] @endcode
 */
runs(list)
{
    cut(list, edges(list))
};

/**
 * @param x list
 * @return Shuffled list
 */
intrinsic <T> shuffle(x:[T]) -> [T];

/**
 * @param x list of values
 * @return the number of items in the list
 */
intrinsic <T> size(x:[T]) -> Int;

/**
 * Given a list of sizes, return a list of starting points.
 * E.g. <code>starts([1, 2, 3]) == [0, 1, 3]</code>.
 * @param sizes list of sizes
 * @return list of starting points
 */
starts(sizes : [Int]) -> [Int]
{
    drop(-1, scan((+), 0, sizes))
};

/**
 * Note: empty list throws, currently
 * @param x list of items
 * @return A new list of items x, minus the first value.
 */
intrinsic <T> tail(x:[T]) -> [T];

/**
 * Takes first x items from list if x > 0, last -x if x < 0.
 * If x > than size(y) then wraps around to begining of y.
 *
 * @param x Number of items to take from the list.
 *          x > 0 takes first x items
 *          x < 0 takes x items from the end of the list
 *          If x > than size(y) then wraps around to begining of y.
 * @param y list of items
 * @return new list with specified number of items from the original list
 */
intrinsic <T> take(x:Int, y:[T]) -> [T];

/**
 * Return sublist of unique items from list.
 * @param x list of items
 * @return list of unique items in list x
 */
intrinsic <T> unique(x:[T]) -> [T];

/**
 *
 * @param x list of tuples
 * @return tuple of lists
 */
intrinsic <Ts:[*]> unzip(x:[Tup(Ts)]) -> Tup(Ts | List);

/**
 * @param x base list of values
 * @param y function that accepts each item in the list x and returns a boolean
 *           value for whether or not to return the position of the item in the list
 * @return List of indexes in the base list where the predicate function returned a true value.
 */
intrinsic <T> where(x:[T], y:T -> Bool) -> [Int];

/**
 *
 * @param x tuple of lists of values
 * @return list of tuples
 */
intrinsic <Ts:[*]> zip(x:Tup(Ts | List)) -> [Tup(Ts)];

// --------------------------------------------------------------------------

//
// int lists
//

/**
 * @param lst list of int
 * @return arith mean over list of ints, 0 for empty list.
 * @code avg([2,3,4]) //returns 3 @endcode
 */
avg(lst)
{
    divz(sum(lst), size(lst))
};

/**
 * Multiply a list of integers. E.g.,
 * <code>product([2, 3, 4]) == 24</code>
 * @param is list of integers
 * @return product of elements in list
 */
product(is : [Int]) -> Int
{
    reduce((*), 1, is)
};

/**
 * Running total of a list of integers. E.g.,
 * <code>runtot([1, 2, 3]) == [1, 3, 6]</code>
 * @param is list of integers
 * @return list of running total of input
 */
runtot(is : [Int]) -> [Int]
{
    drop(1, scan((+), 0, is))
};

/**
 * Sum of a list of integers. E.g.,
 * <code>sum([1, 2, 3]) == 6</code>
 * @param is list of integers
 * @return sum of list elements
 */
sum(is : [Int]) -> Int
{
    reduce((+), 0, is)
};

// --------------------------------------------------------------------------

//
// float lists
//

/**
 * @param lst list of float
 * @return arith mean over list of floats, 0 for empty list.
 * @code favg([2.0,3.0,4.0]) //returns 3.0 @endcode
 */
favg(lst)
{
    fdivz(fsum(lst), i2f(size(lst)))
};

/**
 * Multiply a list of doubles. E.g.,
 * <code>product([2.0, 3.0, 4.0]) == 24.0</code>
 * @param fs list of doubles
 * @return product of elements in list
 */
fproduct(fs : [Double]) -> Double
{
    reduce((*.), 1.0, fs)
};

/**
 * Running total of a list of doubles. E.g.,
 * <code>fruntot([1.0, 2.0, 3.0]) == [1.0, 3.0, 6.0]</code>
 * @param fs list of doubles
 * @return list of running total of input
 */
fruntot(fs : [Double]) -> [Double]
{
    drop(1, scan((+), 0.0, fs))
};

/**
 * Sum a list of doubles. E.g.,
 * <code>fsum([1.0, 2.0, 3.0]) == 6.0</code>
 * @param fs list of doubles
 * @return sum of list elements
 */
fsum(fs : [Double]) -> Double
{
    reduce((+.), 0.0, fs)
};

// ------------------------------------------------------------------

//
// set oeperations on lists
//

/**
 * Is predicate true for any value in a list?
 *
 * @code
 * any([1, 2, 1], { $0 == 2 }) // returns true
 * any([1, 2, 1], { $0 > 2 }) // returns false
 * @endcode
 *
 * @param vals list of values.
 * @param pred predicate.
 * @return true iff any value in the given list
 * satisfies the given predicate (shortcuts).
 */
any(vals, pred)
{
    evolve_while(not, false, { _, v => pred(v) }, vals)
};

/**
 *  Is predicate true for all values in a list?
 *
 * @code
 * all([1, 2, 1], { $0 >= 1 }) // returns true
 * all([1, 2, 1], { $0 == 1 }) // returns false
 * @endcode
 *
 * @param vals list of values.
 * @param pred predicate.
 * @return true iff all values in the given list
 * satisfy the given predicate (shortcuts).
 */
all(vals, pred)
{
    evolve_while(id, true, { _, v => pred(v) }, vals)
};

/**
 * set intersection on lists
 * @param list1 list of values
 * @param list2 list of values
 * @return list containing common values in both lists
 */
intersection(list1, list2)
{
    map2 = assoc(list2, [()]);
    unique(filter(list1, { iskey(map2, $0) }))
};

/**
 * @param a list of values
 * @param b list of values
 * @return true if lists a and b are permutations of each other
 */
isperm(a, b) { counts(a) == counts(b) };

/**
 * directional set difference on lists
 * @param list1 list of values
 * @param list2 list of values
 * @return list containing values that are unique to each list
 */
difference(list1, list2)
{
    map2 = assoc(list2, [()]);
    unique(filter(list1, { !iskey(map2, $0) }))
};

/**
 * set union on lists
 * @param list1 list of values
 * @param list2 list of values
 * @return list of values consisting of the union of both lists
 */
union(list1, list2)
{
    unique(list1 + list2)
};

// ------------------------------------------------------------------

//
// maps
//

/**
 * Create map from key and value lists.
 * For duplicate keys, last instance wins.
 * Equal list length is not required, value
 * list is cycled over if necessary.
 * @param x list of keys
 * @param y list of values
 * @return new map
 */
intrinsic <K,V> assoc(x:[K], y:[V]) -> [K : V];

/**
 * Return the values of a map as a list.
 * @param x map
 * @return List of values in map.
 */
intrinsic <K,V> entries(x:[K : V]) -> [(K, V)];

/**
 * Determine of value is a valid key in a map.
 * @param x map
 * @param y key
 * @return true if y is a valid key in y, otherwise false.
 */
intrinsic <K,V> iskey(x:[K : V], y:K) -> Bool;

/**
 * @param x map
 * @return keyset of a map as a list
 */
intrinsic <K,V> keys(x:[K : V]) -> [K];

/**
 * create a new map with original's contents, minus the given key
 * @param x map
 * @param y key to be removed
 * @return new map with original's contents, minus the given key
 */
intrinsic <K,V> mapdel(x:[K : V], y:K) -> [K : V];

/**
 * Return new map with original's contents, with given key
 * now associated with value.
 * @param x map
 * @param y key
 * @param z new value
 * @return New map with original's contents, with given key  now associated with value.
 */
intrinsic <K, V> mapset(x:[K : V], y:K, z:V) -> [K : V];

/**
 * Create a new map with original's contents, but with
 * given keys associated with values.
 * Note that we roll over the value list.
 * @param x map
 * @param y list of keys
 * @param z list of new values
 * @return New new map with original's contents, but with given keys associated with values.
 */
intrinsic <K,V> mapsets(x:[K : V], y:[K], z:[V]) -> [K : V];

/**
 * Merge two maps. Right map wins where keysets overlap.
 * @param x map
 * @param y map
 * @return New merged map of x and y.
 */
intrinsic <K,V> mplus(x:[K : V], y:[K : V]) -> [K : V];

/**
 * returns the values of a map as a list
 * @param x map
 * @return list containing all of the values from the map
 */
intrinsic <K,V> values(x:[K : V]) -> [V];

/**
 * given a list, returns a map from items to counts
 * @param list list of values
 * @return a map with keys that are the unique values in the list and the
 *         key values are count of times the value appeared in the list
 */
counts(list)
{
    inckey(map, key) { mapset(map, key, 1 + mapgetd(map, key, 0)) };
    reduce(inckey, [:], list)
};

/**
 * map lookup with default.
 * @param map map data
 * @param key key to look up in map
 * @param default default value to return if key is not in map
 * @return value in map at the specified key if the map contains the key, otherwise return default value.
 */
mapgetd(map, key, default)
{
    guard(!iskey(map, key), default, {map[key]})
};

// ------------------------------------------------------------------

//
// tuples
//

/**
 * Build the cross (tuple product) of two lists
 * @param xs list of values
 * @param ys list of values
 * @return cross product of two lists:
 * [(x0, y0), ..., (xN, y0), (x0, y1), ... (xN, yN)]
 */
cross(xs, ys)
{
    xn = size(xs);
    flatten(ys | { zip(xs, rep(xn, $0)) })
};

/**
 * Given two functions that share a common input type, produce a function
 * that takes an input of that type and returns a pair of results, built
 * from the return values of the given functions.
 * TODO variadic a la zip, needs type-level Zip
 * @param f function.
 * @param g function.
 * @return a function that takes an input of same type as f and g and
 * returns a pair of results.
 */
<A, B, C> fan(f : A -> B, g : A -> C) -> A -> (B, C)
{
    { (f($0), g($0)) }
};

/**
 * @param p tuple value
 * @return returns the value in the tuple at position 0
 */
<A, B> fst(p:(A, B)) -> A
{
    p.0
};

/**
 * Given two functions, produce a function that takes a pair of inputs
 * and returns a pair of results, built from the return values of the
 * given functions.
 * TODO variadic a la zip, needs type-level Zip
 * @param f function.
 * @param g function.
 * @return a function that takes a pair of inputs and returns a pair of
 * results.
 */
<A, B, C, D> fuse(f : A -> B, g : C -> D) -> (A, C) -> (B, D)
{
    { (f($0), g($1)) }
};

/**
 * @param p tuple value
 * @return returns the value in the tuple at position 1
 */
<A, B> snd(p:(A, B)) -> B
{
    p.1
};

/**
 * @param value
 * @return a two-tuple containing "twins" of value.
 */
<A> twin(v : A) -> (A, A)
{
    (v, v)
};

// ------------------------------------------------------------------

//
// boxes and transactions
//

/**
 * Create a box from an initial value.
 * @param v value to be boxed
 * @return boxed value
 */
intrinsic <T> box(v : T) -> *T;

/**
 * Create a tuple of boxes from a tuple of initial values.
 * @param x value to be boxed
 * @return boxed value
 */
intrinsic <Ts:[*]> boxes(vs : Tup(Ts)) -> Tup(Ts | Box);

/**
 * Run a block in a transaction.
 */
intrinsic <T> do(b : () -> T) -> T;

/**
 * Return the current value of a box.
 * @param b box
 * @return value of box
 */
intrinsic <T> get(b : *T) -> T; // unary *

/**
 * Return a tuple of the current values of a tuple of boxes.
 * Atomic, so box reads all take place at the same moment
 * in program time. I.e., this function is equivalent to
 * performing individual gets on each box within a transaction
 * and returning a tuple of the results.
 * @param x tuple of boxes
 * @return tuple of values
 */
intrinsic <Ts:[*]> gets(bs : Tup(Ts | Box)) -> Tup(Ts);

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

// --------------------------------------------------------------

//
// actions on boxed values
//

/**
 * Perform a generalized action on a boxed variable,
 * updating its state and returning the action's result.
 * Parameterized by an action function that takes a state
 * value and returns a pair containing the new state and
 * a result value.
 *
 * @param b boxed state variable
 * @param f action function on a state value of type S.
 * returning a pair of (new state, action result).
 * @return result of action on b's state
 */
<S, T> act(b : *S, f : S -> (S, T)) -> T
{
    do {
        (next, result) = f(own(b));
        b := next;
        result
    }
};

/**
 * Atomically update a box to new value and return its prior value.
 * Atomic here means that we're guaranteed not to step on an update
 * that happens between retrieving the prior value and setting the
 * new one.
 *
 * Note: equivalent to <code>{ b, v => act(b, { (v, $0) }) }</code>
 *
 * @param b box to modify
 * @param v new value to place in the box
 * @return original value in the box
 */
getput(b, v)
{
    do {
        prior = own(b);
        b := v;
        prior
    }
};

/**
 * Run update function f on current value of box b.
 * Update b with the result, and also return it.
 *
 * Note: equivalent to <code>{ act(b, { v = f($0); (v, v) }) }</code>
 *
 * @param b box to update
 * @param f update function
 * @return box's new value
 */
preupdate(b, f)
{
    do {
        new = f(own(b));
        b := new;
        new
    }
};

/**
 * Atomically run update function f on current value of box b.
 * Update b with the result, and return b's original
 * value.
 *
 * Note: equivalent to <code>{ act(b, { (f($0), $0) }) }</code>
 *
 * @param b box to update
 * @param f update function
 * @return box's prior value
 */
postupdate(b, f)
{
    do {
        prior = own(b);
        b := f(prior);
        prior
    }
};

/**
 * Atomic pre-increment.
 * @param b box to increment
 * @return incremented value
 */
preinc(b)
{
    preupdate(b, inc)
};

/**
 * Atomic post-increment.
 * @param b box to increment
 * @return box's original value
 */
postinc(b)
{
    postupdate(b, inc)
};

/**
 * Atomic pre-decrement.
 * @param b box to decrement
 * @return decremented value
 */
predec(b)
{
    preupdate(b, dec)
};

/**
 * Atomic post-decrement.
 * @param b box to increment
 * @return box's original value
 */
postdec(b)
{
    postupdate(b, dec)
};

// --------------------------------------------------------------

//
// CAS and variations.
// In all cases, boxes are owned up front, making the
// tail of the transaction inevitable, mod (a) outer
// transactions and (b) box acquisition in passed
// functions.
//

/**
 * compare and swap. returns success
 * @param b boxed value
 * @param o old value
 * @param n new value
 * @return success
 */
cas(b, o, n)
{
    do {
        own(b) == o && { b := n; true }
    }
};

/**
 * tuplized compare and swap
 * @param bs tuple of boxes
 * @param os tuple of old values to compare
 * @param ns tuple of new values
 * @return success
 */
cast(bs, os, ns)
{
    do {
        owns(bs) == os && { bs ::= ns; true }
    }
};

/**
 * compare and update. cau is to update as cas is to put
 * @param b boxed value
 * @param o old value
 * @param f function to update the box
 * @return success
 */
cau(b, o, f)
{
    do {
        own(b) == o && { b := f(o); true }
    }
};

/**
 * tuplized compare and update
 * @param bs tuple of boxes
 * @param os tuple of old values to compare
 * @param f function to update the tuple values
 * @return success
 */
caut(bs, os, f)
{
    do {
        owns(bs) == os && { bs ::= f(os); true }
    }
};

/**
 * test and swap. returns success paired with old value
 * @param b boxed value
 * @param p test function
 * @param n new value
 * @return pair of success and old value (TODO variant)
 */
tas(b, p, n)
{
    do {
        o = own(b);
        (p(o) && { b := n; true }, o)
    }
};

/**
 * test and update. returns success paired with old value
 * @param b boxed value
 * @param p test function
 * @param n function to update the box
 * @return pair of success and old value (TODO variant)
 */
tau(b, p, f)
{
    do {
        o = own(b);
        (p(o) && { b := f(o); true }, o)
    }
};

/**
 * tuplized test and swap
 * @param bs tuple of boxes
 * @param p function to test the tuple to see if swap should proceeed
 * @param ns tuple of new values
 * @return pair of success and old value (TODO variant)
 */
tast(bs, p, ns)
{
    do {
        os = owns(bs);
        (p(os) && { bs ::= ns; true }, os)
    }
};

/**
 * tuplized test and update
 * @param bs tuple of boxes
 * @param p function to test the tuple to see if update should proceeed
 * @param f function to update the tuple values
 * @return pair of success and old value (TODO variant)
  */
taut(bs, p, f)
{
    do {
        os = owns(bs);
        (p(os) && { bs ::= f(os); true }, os)
    }
};

// ----------------------------------------------------------------------

//
// dependencies
//

/**
 * create and return a box whose value tracks the value
 * of the passed box, mapped through the passed function.
 * E.g. c = dep(b, id); c tracks the value of b.
 * @param src box to track
 * @param f function that will be invoked with the new value of src when it is altered
 */
dep(src, f)
{
    sink = box(f(get(src)));
    react(src, { put(sink, f($0)) });
    sink
};

// ------------------------------------------------------------------

//
// concurrent tasks
//

/**
 * async(f, cb) runs f asynchronously and calls cb with the result.
 * @param f first function to run
 * @param cb second function to run using the result from f
 */
<A, B> async(f : () -> A, cb : A -> B) -> ()
{
    spawn {
        cb(f())
    }
};

/**
 * @return Return number of available processors.
 */
intrinsic availprocs() -> Int;

/**
 * Start running a block in a {@link Future} and return a lambda
 * that provides (blocking) access to the result.
 * @param b block to run
 * @return lambda
 */
intrinsic <T> future(b : () -> T) -> () -> T;

/**
 * sleep the current task a given number of millis
 * @param x number of milliseconds to sleep
 */
intrinsic sleep(millis : Int) -> ();

/**
 * Runs block asynchronously as a new task.
 * @param b function
 * @return
 */
intrinsic <T> spawn(b : () -> T) -> ();

/**
 * @return returns current task id
 */
intrinsic taskid() -> Long;

// ------------------------------------------------------------------

//
// reactivity
//

/**
 * Transactional wait/notify.
 * await(box, pred) puts current task into wait state until/unless
 * pred(*box) returns true. pred is called exactly once for each
 * value committed to box.
 *
 * Note that in the presence of concurrent modifications to b, there
 * is no guarantee that *b  == v remains true at the time p(v) runs.
 * I.e., the call to p(v) occurs *after* the transaction that commits
 * v to b.
 *
 * @param b box
 * @param p predicate
 */
intrinsic <T> await(b : *T, p : T -> Bool) -> ();

/**
 * Transactional wait/notify on multiple boxes.
 * awaits(boxes, preds) puts current thread into wait state
 * until/unless p(*b) returns true for some b in boxes and
 * corresponding p in preds. p(*b) is called exactly once
 * for each value committed to b.
 *
 * TODO describe (non)guarantee
 *
 * @param bs tuple of boxes
 * @param p predicate
 */
intrinsic <Ts:[*]> awaits(bs : Tup(Ts | Box), p : Tup(Ts | Pred)) -> ();

/**
 * Attach a reactor to a box. reactor is called whenever a value is
 * committed to the box. Note:
 * 1. A particular function r may be associated with a box b only once.
 * repeated calls to react(b, r) have no effect.
 * 2. To detach a reactor r from box b, call unreact(b, r).
 * @param b box to attach reactor to
 * @param r reactor function that will be invoked with the new value in box
 * @return reactor function r
 */
intrinsic <T, X> react(b : *T, r : T -> X) -> (T -> X);

/**
 * Detach a reactor function from a box.
 * Note that function equality is identity.
 * If passed r is not currently attached to b, call will have no effect.
 * @param b box
 * @param r reactor function e.g. as returned from {@link react}.
 * @return b
 */
intrinsic <T, X> unreact(b : *T, r : T -> X) -> *T;

// ------------------------------------------------------------------

//
// system functions
// just a little random assortment at present.
// TODO lots of system functionality once we have variants and interfaces
//

/**
 * Returns a list of command line arguments
 * @return a possibly empty list of strings that were passed via the
 * commandline
 */
intrinsic args() -> [String];

/**
 * Throw an error with message if argument is false
 * @param c if value is false, throw error
 * @param m message
 */
intrinsic assert(c : Bool, m : String) -> ();

/**
 * {@link Math#random()}
 * @return float value greater than or equal to 0.0 and less than 1.0.
 */
intrinsic frand() -> Double;

/**
 * Return the value of an evironment variable.
 * @param name the name of the environment variable to query
 * @return the value of the environment varaible, or an empty string
 * if the named value does not exist
 */
intrinsic getenv(name:String) -> String;

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
intrinsic <T> print(x : T) -> ();

/**
 * print a string to System.out.
 * @param x string to be printed
 * @ return
 */
intrinsic printstr(s : String) -> ();

/**
 * Generate a random integer between 0 and x - 1
 * @param x max value
 * @return An integer between 0 and x - 1
 */
intrinsic rand(x:Int) -> Int;

// ------------------------------------------------------------------

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
type ServerSocket = New(Opaque);
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

