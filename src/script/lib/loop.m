//
// Looping constructs
//

import * from list;

/**
 * Iterate on an endofunction while guard predicate returns true.
 * Iteration is started with specified initial argument, which is
 * passed through the guard before being applied.
 *
 * @param x Predicate lambda that accepts the value of y, determines when cycle ends.
 * @param y Initial value of y that will be passed to the z lambda, value of y is applied
 *        to the predicate prior to being passed into the z lambda parameter.
 * @param z Function that accepts passed in value T and returns the next value T that will
 *        be fed back into the function if guard predicate is still true.
 * @return Value of y when predicate is false.
 */
intrinsic <T> cycle(x:(T -> Bool), y:T, z:(T -> T)) -> T;

/**
 * Iterate on an endofunction fox x number of times
 *
 * @param x Limit of how many times z function will be called.
 * @param y Initial value of y that will be passed to the z lambda.
 * @param z Function that accepts passed in value T and returns the next value T that will
 *        be fed back into the function as long as upper limit has not been reached.
 * @return Value of y when predicate is false.
 */
intrinsic <T> cyclen(x:Int, y:T, z:(T -> T)) -> T;

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
intrinsic <A,B> evolve_while(x:(A -> Bool), y:A, z:((A, B) -> A), a:[B]) -> A;

/**
 * Reduce a set of values with a combining function.
 * @param x combining function
 * @param y initial value
 * @param z list of values
 * @return Value produced by calling x recursively with previsouly 
 *         calculated value and next item in the list.
 */
intrinsic <A,B> reduce(x:((A, B) -> A), y:A, z:[B]) -> A;

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
    evolve_while(not, false, snd $ pred, vals)
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
    evolve_while(id, true, snd $ pred, vals)
};

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
	cycle(diff, (init, func(init)), next).0
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
trace(func, init)
{
    diff(accum, x, y) { x != y && { y != init } };
    next(accum, x, y) { (append(accum, y), y, func(y)) };
	  cycle(diff, ([init], init, func(init)), next).0
};

/**
 * Starting with an initial value, "evolve" a new one by calling a
 * function on each element of an input list. The function calculates
 * a new answer. Evolve is reduce with reordered parameters, which
 * is more readable in many situations.
 * 
 * TODO: rewrite this in the new type syntax lingo asap.
 * TODO: when there is a place to document reduce, this comment reduced to a ref.
 * 
 * @param initial value. Suppose it is Type A
 * @param func function which takes the result returned by the previous
 * call of func (or initial the first time)
 * and one element of list (type B) and calculates the new result
 * (Type A). Hence func is of type (A
 * @param list list of inputs. Suppose each element is of type B.
 * @return the result of the last call of func (Type A)
 *
 * @code
 * //an arithmetic example. Add up the elements in the input list.
 * evolve(0,   {<accumulator>+<element>}, [1,2,3])        //returns 6
 * 
 * //a filtering example. Collect the even elements of a list.
   evolve( [],
         {list,ele => if(eq(mod(ele, 2), 0), { append(list, ele) }, { list }) },
         [1,2,3,4,5,6,7]
	     )  //returns [2,4,6] 
 * @endcode
 * @see reduce
 */
evolve(init, f, list) { reduce(f, init, list) };

/**  like evolve_while, except returns a list of all results produced
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
 * given a predicate and a list of values, return the position of the
 * first item that satisfies the predicate. If none does, return the
 * size of the list
 * 
 * @param vals list of values.
 * @param pred predicate.
 */
firstwhere(pred, vals)
{
    n = size(vals);
    cycle({ i => i < n && { !pred(vals[i]) } }, 0, inc)
};

/**
 * given a list of ascending values and a single value of the same type,
 * return the insertion point for the single value. Useful for range-based
 * partitioning and finding next-closest values in sparse sorted lists.
 * 
 * TODO 1. have a version that insists on sortedness, and make the algorithm
 * TODO binary (not linear) under that restriction.
 * TODO 2. item type T should be instance of TO class which specifies (<=)
 */
inspt(list, val) { firstwhere({ val <= $0 }, list) };
finspt(list, val) { firstwhere({ val <=. $0 }, list) };

/**
 * Reduce a set of values with a combining function. Return all intermediate values.
 * @param x combining function
 * @param y initial value
 * @param z list of values
 * @return A list of values produced by calling x recursively with previsouly 
 *         calculated value and next item in the list.
 */
intrinsic <A,B> scan(x:(A, B) -> A, y:A, z:[B]) -> [A];

/**
 * iter is degenerate while.
 */
iter(p) {
    while(p, {()})
};
