//
// Box and mutation related functionality
//

import * from list;

/**
 * Perform an action on a boxed variable, updating its state
 * and returning the action's result.
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
 * Update box to new value, return its prior value.
 * @param b box to modify
 * @param v new value to place in the box
 * @return original value in the box
 */
postput(b, v) { act(b, { (v, $0) }) };

/**
 * Run update function f on current value of box b.
 * Update b with the result, and also return it.
 * Note: action function argument to act() is
 * equivalent to <code>f $ twin</code>.
 */
preupdate(b, f) { act(b, { v = f($0); (v, v) }) };

/**
 * Run update function f on current value of box b.
 * Update b with the result, and return b's original
 * value.
 * @param b box to update
 * @param f update function
 * @return box's prior value
 */
postupdate(b, f) { act(b, { (f($0), $0) }) };

/**
 * Atomic pre-decrement.
 * @param b box to decrement
 * @return decremented value
 */
predec(b) { preupdate(b, dec) };

/**
 * Atomic pre-increment.
 * @param b box to increment
 * @return incremented value
 */
preinc(b) { preupdate(b, inc) };

/**
 * Atomic post-increment.
 * @param b box to increment
 * @return box's original value
 */
postinc(b) { postupdate(b, inc) };

/**
 * Atomic post-decrement.
 * @param b box to increment
 * @return box's original value
 */
postdec(b) { postupdate(b, dec) };

//
// cas and variations.
// In all cases, boxes are owned up front, making the
// rest of the transaction inevitable, mod (a) outer
// transactions and (b) box acquisition in passed
// functions.
//

/**
 * compare and swap. returns success
 * @param b boxed value
 * @param o old value
 * @param n new value
 * @return returns success
 */
cas(b, o, n)
{
    do {
        own(b) == o && { b := n; true }
    }
};

/**
 * compare and update. cau is to update as cas is to put
 * @param b boxed value
 * @param o old value
 * @param f function to update the box
 * @return returns success
 */
cau(b, o, f)
{
    do {
        own(b) == o && { b := f(o); true }
    }
};

/**
 * test and swap. returns success paired with old value
 * @param b boxed value
 * @param p test function
 * @param n new value
 * @return returns tuple of success and old value
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
 * @return returns tuple of success and old value
 */
tau(b, p, f)
{
    do {
        o = own(b);
        (p(o) && { b := f(o); true }, o)
    }
};

/**
 * wait, then test and update using the same (box, pred)
 * for wait and test. returns success paired with old value
 * @param b boxed value
 * @param p predicate
 * @param f function to update the box
 * @return returns tuple of success and old value
 */
wtau(b, p, f)
{
    await(b, p);
    tau(b, p, f)
};

/**
 * tuplized compare and swap
 * @param bs tuple of boxes
 * @param os tuple of old values to compare
 * @param ns tuple of new values
 * @return returns success
 */
cast(bs, os, ns)
{
    do {
        owns(bs) == os && { bs ::= ns; true }
    }
};

/**
 * tuplized compare and update
 * @param bs tuple of boxes
 * @param os tuple of old values to compare
 * @param f function to update the tuple values
 * @return returns success
 */
caut(bs, os, f)
{
    do {
        owns(bs) == os && { bs ::= f(os); true }
    }
};

/**
 * tuplized test and swap
 * @param bs tuple of boxes
 * @param p function to test the tuple to see if swap should proceeed
 * @param ns tuple of new values
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
  */
taut(bs, p, f)
{
    do {
        os = owns(bs);
        (p(os) && { bs ::= f(os); true }, os)
    }
};

/**
 * multiwait, then test and update using the same (boxes, pred)
 * for wait and test. returns success paired with old values
 * @param bs tuple of boxed values
 * @param p predicate 
 * @param f function to update the tuple values
 */
wtaut(bs, p, f)
{
    awaits(bs, p);
    taut(bs, p, f)
};

/**
 * push v onto front of boxed list, enqueue
 * @param blst list of boxed values
 * @param v value
 */
pushfront(blst, v) { blst <- { [v] + $0 } };

/**
 * push v onto back of boxed list, enqueue
 * @param blst list of boxed values
 * @param v value
 */
pushback(blst, v) { blst <- { append($0, v) } };

/**
 * pop value from front of boxed list, deque
 * @param blst list of boxed values
 * @return value that was popped off of the boxed list
 */
popfront(blst) { act(blst, { (rest($0), first($0)) }) };

/**
 * pop value from back of boxed list, deque
 * @param blst list of boxed values
 * @return value that was popped off of the boxed list
 */
popback(blst) { act(blst, { (drop(-1, $0), last($0)) }) };

/**
 * return the first value in a boxed list
 * @param blst list of boxed values
 * @return first value in the list
 */
peekfront(blst) { first(*blst) };

/**
 * return the last value in a boxed list
 * @param blst list of boxed values
 * @return last value in the list
 */
peekback(blst) { last(*blst) };

//
// boxed list as queue
//
pushq = pushback;
popq = popfront;
peekq = peekfront;

//
// boxed list as stack
//
push = pushback;
pop = popback;
peek = peekback;

/**
 * swap the last value in a boxed list with a new value
 * @param s boxed list
 * @param v new value
 * @return previous value at the end of boxed list
 */
swap(s, v)
{
    act(s, { lst => (append(drop(-1, lst), v), last(lst)) })
};

//
// reactivity
//

/**
 * react is like watch, but with only new value passed to watcher.
 * @param b box to watch
 * @param f function that will be invoked with the new value in box
 */
react(b, f) { watch(b, { old, new => f(new) }) };

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

/**
 * create and return a box whose value tracks the value
 * of the passed boxes, mapped through the passed function.
 * E.g. x = box(0); y = box(1); z = deps([x, y], sum);
 * 
 * TODO over non-uniform boxes
 * @param sources list of boxes to track
 * @param f function that will be invoked with the new values of sources when they are altered
 */
deps(sources, f)
{
    sink = box(f(sources | get));
    updater(v) { do { put(sink, f(sources | get)) } };
    sources | { react($0, updater) };
    sink
};
