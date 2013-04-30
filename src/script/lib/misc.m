//
// Miscellaneous functions that don't belong anywhere else, or have too mamy dependencies 
// to go into a basic file
//

import * from list;
import * from mutate;
import * from math;
import tracen from loop;
import mapz from map;
import l2i,l2f from integer;

/**
 * Return a chunk size (stride) that will divide the given list
 * into the given number of chunks. Chunks will be evenly sized
 * except possibly for the final chunk, which may be smaller
 * than the rest (in cases where chunks > 1).
 * @param lst list
 * @param nchunks number of chunks to calculate stride for
 * @return chunk size as described above
 */
stride(lst, nchunks)
{
    s = size(lst);
    n = constrain(1, nchunks, s);
    s / n + sign(s % n)
};

/**
 * given list and number of chunks, return chunked list.
 */
chunks(lst, nchunks)
{
    ravel(lst, stride(lst, nchunks))
};

/**
 * TODO variadic a la zip, needs type-level Zip
 * @param f function.
 * @param g function.
 * @return a function that takes an arg of same type as f and g and returns a pair of results.
 */
fan(f, g)
{
    { (f($0), g($0)) }
};

/**
 * @param f function.
 * @param g function.
 * @return  a function that takes a pair of args and returns a pair of results.
 * 
 * TODO variadic a la zip, needs type-level Zip
 */
fuse(f, g)
{
    { (f($0), g($1)) }
};

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
 */
bench(b)
{
    t0 = millitime();
    result = b();
    (#time: l2i(lminus(millitime(), t0)), #result: result)
};

/**
 * run a block n times, return average elapsed time (no result).
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
    count(sign(extent) * n) | { start + $0 * stride };
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
 */
pmapn(lst, f, ntasks)
{
    flatten(chunks(lst, ntasks) |: { $0 | f })
};

/**
 * evaluate for(list, f) in parallel chunks of the given size.
 */
pforn(lst, f, ntasks)
{
    pfor(chunks(lst, ntasks), { for($0, f) })
};

/**
 * evaluate filter(lst, pred) using the given number of parallel tasks
 */
pfiltern(lst, pred, ntasks)
{
    flatten(chunks(lst, ntasks) |: { filter($0, pred) })
};

/**
 * evaluate where(lst, pred) using the given number of parallel tasks
 */
pwheren(lst, pred, ntasks)
{
    s = stride(lst, ntasks);
    chunks = ravel(lst, s);
    offsets = tracen(size(chunks), 0, { $0 + s });
    inputs = zip(chunks, offsets);
    results = inputs |: { chunk, offset => where(chunk, pred) | { $0 + offset } };
    flatten(results)
};

/**
 * partition, running partition function in parallel for each list element.
 */
ppart(vals, f) { group(vals |: f, vals) };

/**
 * partition, running partition function on list chunks of a given size.
 */
ppartn(vals, f, n)
{
    group(flatten(chunks(vals, n) |: { $0 | f }), vals)
};

/**
 * cut a list into equal-length sublists (last might be ragged)
 */
ravel(lst, n)
{
    cut(lst, countn(size(lst), n))
};

