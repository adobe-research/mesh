//
// Benchmarking utilities
// TODO rationalize, extend, etc.
//

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
