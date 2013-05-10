//
// Miscellaneous functions that don't belong anywhere else, or have too mamy dependencies 
// to go into a basic file
//

import * from list;
import * from mutate;
import * from math;
import scan, tracen, cycle from loop;
import mapz from map;

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
 *
 */
intrinsic rgb2hsb(x:Int) -> (Double, Double, Double);

/**
 * sleep the current thread a given number of millis
 * @param x number of milliseconds to sleep
 */
intrinsic sleep(millis : Int) -> ();

//
// producer/consumer
//

/**
 * Driver function for a producer task.
 * Normally asynchronous: spawn { produce(...) }.
 * Given
 * 
 * - boxed boolean c, a circuit breaker
 * - boxed list q, a work queue
 * - int n, a max size for q
 * - function f, a producer
 * 
 * Attempt to run f() and push the result onto q
 * when *q is not full, until/unless *c is false.
 * 
 * Note: values produced but not enqueued, due to
 * c having been concurrently set false during
 * production, are discarded.
 */
produce(c, q, n, f)
{
    nf(l) { size(l) < n };
    while({*c}, {
        awaits((c, q), { b, l => !b || {nf(l)} });
        when(*c, {
            v = f();
            while({*c && {
                !tau(q, nf, { append($0, v) }).0
            }}, {()})
        })
    })
};

/**
 * Driver function for a consumer task.
 * Normally asynchronous: spawn { consume(...) }.
 * Given:
 * 
 * - boxed boolean c, a circuit breaker
 * - boxed list q, a work queue
 * - function f, a consumer
 * 
 * Attempt to pop head value v from q and run f(v)
 * when *q is not empty, unless/until *c is false.
 * 
 * Note: values dequeued but not consumed, due to c
 * having been concurrently set false during
 * dequeueing, are discarded.
 */
consume(c, q, f)
{
    ne(l) { size(l) > 0 };
    while({*c}, {
        awaits((c, q), { b, l => !b || {ne(l)} });
        when(*c, {
            (b, l) = tau(q, ne, tail);
            when(b, { f(head(l)) })
        })
    })
};

// Benchmarking

/**
 * run a block, return elapsed time and result.
 * @param b block of code to benchmark
 * @return record containing #time, milliseconds taken to execute the block of code,
 *         #result, the result of the block of code.
 */
bench(b)
{
    t0 = millitime();
    result = b();
    (#time: l2i(lminus(millitime(), t0)), #result: result)
};

/**
 * run a block n times, return average elapsed time (no result).
 * @param b block of code to benchmark
 * @param n number of times to execute the block of code
 * @return return average elapsed time
 */
benchn(n, f)
{
    timer()
    {
        start = millitime();
        f();
        l2i(lminus(millitime(), start))
    };

    avg(repeat(n, timer))
};

/**
 * instrument a function --
 * returns the instrumented function, and a boxed elapsed-time float value.
 * 
 * TODO: instead of boxed elapsed value, return an object.
 * this would allow a richer API, and importantly, privatize the
 * boxed value, enabling transactionaless updating in the future.
 */
instr(f)
{
    num_recs = box(0L);
    avg_time = box(0.0);

    wrapped_f(x)
    {
        t0 = nanotime();

        y = f(x);

        updates((num_recs, avg_time), { num, avg =>
            elapsed = lminus(nanotime(), t0);
            new_num = num + 1L;
            new_avg = ((avg *. l2f(num)) + l2f(elapsed)) /. l2f(new_num);
            (new_num, new_avg)
        });

        y
    };

    (wrapped_f, (#num: num_recs, #avg: avg_time))
};

/**
 *
 */
intrinsic hsb2rgb(x:Double, y:Double, z:Double) -> Int;

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
 * evaluate for(list, f) in parallel chunks of the given size.
 * @param list list of items
 * @param f function to process each item in the list
 * @param n number of chunks to chop the list into in order to process in parallel
 */
pforn(list, f, n)
{
    pfor(chunks(list, n), { for($0, f) })
};

/**
 * evaluate filter(list, pred) in parallel chunks of the given size.
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
 * Evaluate where(list, pred) in parallel, using the given number of tasks.
 * @param list list of items
 * @param pred predicate over list items
 * @param ntasks number of parallel tasks to use when evaluating
 * @return list of indexes in the base list where the predicate function returned true
 */
<T> pwheren(list : [T], pred : T -> Bool, ntasks : Int) -> [Int]
{
    cps = cutpoints(list, ntasks);
    ixs = zip(cut(list, cps), cps) |: { where($0, pred) ~+ $1 };
    flatten(ixs)
};

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
 * Cut a list into sublists of the given length (last might be ragged).
 * If a length of zero is passed, an empty list is returned. If a length
 * greater than the length of the list is passed, the result is the same
 * as when a length equal to the length of the list is passed, i.e. a
 * singleton collection containing the entire list as a sublist.
 * @param list list of items
 * @param n desired sublist length
 * @return list of equal-length sublists (last might be ragged)
 */
filet(list, n)
{
    s = size(list);
    slices = divz(s, n) + sign(modz(s, n));
    cut(list, count(slices) ~* n)
};

/**
 * Deferred guard: given a predicate p and a function f, builds
 * and returns a guard function which takes a value v, returns it
 * if p(v), and otherwise returns f(v).
 * @param p predicate
 * @param f shunting function
 * @return parameterized guard function
 */
shunt(p, f)
{
    { v => guard(p(v), v, { f(v) }) }
};

/**
 * Transformer for binary functions, returns a version of the
 * given function vectorized over its left (first) argument.
 * E.g. given <code>f : (X, Y) -> Z</code>, <code>eachleft(f)</code>
 * returns a function of type <code>([X], Y) -> [Z]</code>,
 * which returns the results of applying <code>f(x, y)</code>
 * for each <code>x : X</code>.
 * The prefix attribute <code>~</code> on infix operators desugars
 * to <code>eachleft</code>,
 * e.g. <code>xs ~+ y</code> => <code>eachleft(+)(xs, y)</code>.
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
 * The postfix attribute <code>~</code> on infix operators desugars
 * to <code>eachright</code>,
 * e.g. <code>x +~ ys</code> => <code>eachright(+)(x, ys)</code>.
 * @param f binary function to transform
 * @return transformed function
 */
intrinsic <A, B, C> eachright(f : (A, B) -> C) -> (A, [B]) -> [C];

/**
 * Deferred if-else combinator. Given a list of parameterized
 * predicate/action pairs and default action, produces a function
 * which takes an argument and runs each predicate on it in turn
 * until a predicate returns true, then passes it to the corresponding
 * action and returns the value. If no predicate returns true, the
 * argument is passed to the default action. E.g.
 * <code>ifelse(cases, default) == cascade(cases, default)()</code>
 * @param cases list of guard/action pairs
 * @param default default action
 * @return result of executed action
 */
<A, B> cascade(cases : [(A -> Bool, A -> B)], default : A -> B) -> A -> B
{
    n = size(cases);
    { a =>
        c = cycle({ i => i < n && { !cases[i].0(a) } }, 0, inc);
        if(c < n, { cases[c].1(a) }, { default(a) })
    }
};

/**
 * If-else combinator. Given a list of guard/action
 * pairs and a default action, run each guard in turn until one
 * returns true, then execute the corresponding action.
 * If no guard returns true, execute the default action.
 * Return the result of the executed action.
 * @param cases list of guard/action pairs
 * @param default default action
 * @return result of executed action
 */
<T> ifelse(cases : [(() -> Bool, () -> T)], default : () -> T) -> T
{
    cascade(cases, default)()
};

