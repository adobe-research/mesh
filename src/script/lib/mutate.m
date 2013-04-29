//
// Box and mutation related functionality
//

import * from list;

/**
 * Transactional wait/notify.
 * await(box, pred) puts current thread into wait state
 * until/unless pred(get(box)) returns true. pred() is
 * called each time a value is committed to box.
 * @param x box
 * @param y predicate
 */
intrinsic <T> await(x:*T, y:(T -> Bool)) -> ();

/**
 * Transactional wait/notify.
 * awaits((boxes), pred) puts current thread into wait state
 * until/unless pred(gets(boxes)) returns true. pred() is
 * called each time a value is committed to one of the boxes.
 * @param x tuple of boxes
 * @param y predicate
 */
intrinsic <T:[*]> awaits(x:Tup(T | Box), y:(Tup(T) -> Bool)) -> ();

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
 * Remove a watcher function from a box.
 * Function equality is identity, so you need to pass
 * the watcher function itself.
 * @param x box being watched
 * @param y function that was returned from {@link watch(x:*T, y:(T, T) -> X) -> ((T, T) -> X)}.
 * @return box
 */
intrinsic <T,X> unwatch(x:*T, y:(T, T) -> X) -> *T;

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

/**
 * Add a watcher to a box, return watcher.
 * @param x box to be watched
 * @param y function to be executed when the box value changes
 * @return a watcher function
 */
intrinsic <T,X> watch(x:*T, y:(T, T) -> X) -> ((T, T) -> X);

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
 */
postupdate(b, f) { act(b, { (f($0), $0) }) };

/**
 * Atomic pre-decrement.
 */
predec(b) { preupdate(b, dec) };

/**
 * Atomic pre-increment.
 */
preinc(b) { preupdate(b, inc) };

/**
 * Atomic post-increment.
 */
postinc(b) { postupdate(b, inc) };

/**
 * Atomic post-decrement.
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
 */
cas(b, o, n)
{
    do {
        own(b) == o && { b := n; true }
    }
};

/**
 * compare and update. cau is to update as cas is to put
 */
cau(b, o, f)
{
    do {
        own(b) == o && { b := f(o); true }
    }
};

/**
 * test and swap. returns success paired with old value
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
 */
wtau(b, p, f)
{
    await(b, p);
    tau(b, p, f)
};

/**
 * tuplized compare and swap
 */
cast(bs, os, ns)
{
    do {
        owns(bs) == os && { bs ::= ns; true }
    }
};

/**
 * tuplized compare and update
 */
caut(bs, os, f)
{
    do {
        owns(bs) == os && { bs ::= f(os); true }
    }
};

/**
 * tuplized test and swap
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
 */
wtaut(bs, p, f)
{
    awaits(bs, p);
    taut(bs, p, f)
};

/**
 * push v onto boxed list as deque
 */
pushfront(blst, v) { blst <- { [v] + $0 } };
pushback(blst, v) { blst <- { append($0, v) } };

popfront(blst) { act(blst, { (rest($0), first($0)) }) };
popback(blst) { act(blst, { (drop(-1, $0), last($0)) }) };

peekfront(blst) { first(*blst) };
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

swap(s, v)
{
    act(s, { lst => (append(drop(-1, lst), v), last(lst)) })
};

//
// reactivity
//

/**
 * react is like watch, but with only new value passed to watcher.
 */
react(b, f) { watch(b, { old, new => f(new) }) };

/**
 * create and return a box whose value tracks the value
 * of the passed box, mapped through the passed function.
 * E.g. c = dep(b, id); c tracks the value of b.
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
 */
deps(sources, f)
{
    sink = box(f(sources | get));
    updater(v) { do { put(sink, f(sources | get)) } };
    sources | { react($0, updater) };
    sink
};
