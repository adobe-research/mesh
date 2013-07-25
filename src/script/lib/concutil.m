//
// concurrency utilities
// TODO more: polling frame, etc.
//

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
        awaits((c, q), (not, nf));
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
 * Attempt to pop first value v from q and run f(v)
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
        awaits((c, q), (not, ne));
        when(*c, {
            (b, l) = tau(q, ne, tail);
            when(b, { f(head(l)) })
        })
    })
};
