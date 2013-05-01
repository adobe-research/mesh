//
// Miscellaneous functions that don't belong anywhere else, or have too mamy dependencies 
// to go into a basic file
//

import * from list;
import * from mutate;
import * from math;
import l2i,l2f from integer;

/**
 * Given list and #chunks, return chunked list. Only the last chunk of the list can
 * be ragged which may cause the number of chunks returned to be less than what is requested.
 * TODO: guard against i==0, or declare that i==0 will throw
 * @param lst list of values
 * @param i number of chunks to return
 * @return a list containing the chunks
 */
chunks(lst, i)
{
    n = size(lst);
    ravel(lst, n / i + sign(n % i))
};


/**
 * TODO variadic a la zip, needs type-level Zip
 * @param f function.
 * @param g function.
 * @return a function that takes an arg of same type as f and g and returns a pair of results.
 */
fan(f, g) { { (f($0), g($0)) } };

/**
 * @param f function.
 * @param g function.
 * @return  a function that takes a pair of args and returns a pair of results.
 * 
 * TODO variadic a la zip, needs type-level Zip
 */
fuse(f, g) { { (f($0), g($1)) } };

/**
 * @return current time in millis.
 */
intrinsic millitime() -> Long;

/**
 * @return current time in nanos.
 */
intrinsic nanotime() -> Long;

/**
 *
 */
intrinsic rgb2hsb(x:Int) -> (Double, Double, Double);

/**
 * sleep the current thread a given number of millis
 * @param x number of milliseconds to sleep
 */
intrinsic sleep(x:Int) -> ();

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
 * @param start  start of the range numbers are chosen from
 * @param extent extent of the range numbers are chosen from
 * @param stepsize step size (sign is disregarded)
 * @return a list of numbers from start to start + extent - 1
 * in steps of stepsize:
 * (start + extent - stepsize) <= (final value) < (start + extent).
 * 
 * TODO describe descending usage
 */
rangen(start, extent, step)
{
    stride = abs(step);
    n = divz(abs(extent) + stride - 1, stride);
    count(sign(extent) * n) | { start + $0 * stride }
};

/**
 * @param extent extent of the range numbers are chosen from
 * @param stepsize step size
 * @return a list of numbers from 0 to extent - 1 in steps of stepsize:
 * (extent - stepsize) <= (final value) < extent, if stepsize > 0.
 * 
 * TODO describe descending usage
 */
countn(extent, stepsize) { rangen(0, extent, stepsize) };

/**
 * @param x start of the range
 * @param y end of the range
 * @param n step size
 * @return a list of numbers from x to y - 1 in steps of n:
 * (y - n) <= (final) < y.
 * 
 * TODO describe descending usage
 */
fromton(x, y, n) { rangen(x, y - x, n) };

/**
 *
 */
intrinsic hsb2rgb(x:Double, y:Double, z:Double) -> Int;

/**
 * evaluate (lst | f) in parallel chunks of the given size:
 * pmapn(lst, f, 1) == pmap(lst, f) == f |: lst
 * pmapn(lst, f, size(lst)) == map(lst, f) == f | lst.
 * @param lst list of values
 * @param f function to process each value in the list
 * @param n number of chunks to chop the list into in order to process in parallel
 * @return list of values return from processing each item in original list with f
 */
pmapn(lst, f, n)
{
    flatten(chunks(lst, n) |: { $0 | f })
};

/**
 * evaluate for(list, f) in parallel chunks of the given size.
 * @param list list of items
 * @param f function to process each item in the list
 * @param n number of chunks to chop the list into in order to process in parallel
 */
pforn(lst, f, n)
{
    pfor(chunks(lst, n), { for($0, f) })
};

/**
 * evaluate filter(lst, pred) in parallel chunks of the given size.
 * so pfiltern(lst, pred, size(lst)) gives the same result as
 * filter(lst, pred)
 * @param list list of items
 * @param pre Predicate function to determine if list item should be returned.
 * @param n number of chunks to chop the list into in order to process in parallel
 * @return Sublist of x where predicate returned true.
 */
pfiltern(lst, pred, n)
{
    flatten(chunks(lst, n) |: { filter($0, pred) })
};

/**
 * evaluate where(lst, pred) in parallel chunks of the given size.
 * @param list list of items
 * @param pred function that accepts each item in the list x and returns a boolean value for whether or not to return the position of the item in the list
 * @param n number of chunks to chop the list into in order to process in parallel
 * @return List of indexes in the base list where the predicate function returned a true value.
 */
pwheren(lst, pred, n)
{
    flatten(chunks(lst, n) |: { where($0, pred) })
};

/**
 * partition, running partition function in parallel for each list element.
 * @param vals list of items to be partioned
 * @param f function that partions the list based on return value of this function
 * @return a map with keys that are the return values of f and value is a list of items
 *         from vals that produced the key value when passed into f.
 */
ppart(vals, f) { group(vals |: f, vals) };

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
 * cut a list into equal-length sublists (last might be ragged)
 * @param list list of items
 * @param n length of returned sublist
 * @return list of equal-length sublists (last might be ragged)
 */
ravel(lst, n) { cut(lst, countn(size(lst), n)) };

