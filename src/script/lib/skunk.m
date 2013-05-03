//
// ideas pile
//

/**
 * Lifted guard: given a predicate and a function, builds a
 * deferred guard expression that takes a value, returns it
 * if the predicate is satisfied, and otherwise returns the
 * result of applying the function.
 *
 * @param p predicate
 * @param f processing function
 * @return lambda that takes a value v, returns v iff p(v) is
 * true, and f(v) otherwise
 */
shunt(p, f)
{
    { v => guard(p(v), v, { f(v) }) }
};

