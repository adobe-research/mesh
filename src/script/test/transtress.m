
import bench;
import unittest;

// Given a box mutator, an initial value and a terminal value,
// and counts for boxes, repetitions and tasks,
// create a list of boxes containing the initial value,
// and run mutator repeatedly over a shuffle of the box list
// in multiple parallel tasks.
// Return true if all boxes contain terminal value afterwards.
//
<T> harness(mut:*T->?, init:T, term:T, nboxes:Int, ntasks:Int, niters:Int)
{
    print(#nboxes: nboxes, #ntasks: ntasks, #niters: niters);

    // create per-task shuffles of common box list
    blist = repeat(nboxes, { box(init) });
    blists = repeat(ntasks, { shuffle(blist) });

    // in parallel, run mut repeatedly over shuffled box lists
    pfor(blists, { blist => repeat(niters, { for(blist, mut) }) });

    // afterwards, all boxes should contain term
    guard(all(blist, { *$0 == term }), true, {
        print("ERROR, expected all: ": term, "got: ": counts(blist));
        false
    })
};

// ------------------

// transactional mutators

// increment by getting and putting.
// vulnerable to retry as we transition from pinned (for get) to owned (for put)
inc_by_put_get(b) {
    do { put(b, inc(get(b))) }
};

// increment by owning and putting. inevitable after own()
inc_by_put_own(b) {
    do { put(b, inc(own(b))) }
};

// increment by updating. box is owned prior to inc invocation, so is inevitable
inc_by_update(b) {
    update(b, inc)
};

// append by getting and putting.
// vulnerable to retry as we transition from pinned (for get) to owned (for put)
append_by_put_get(b) {
    do { put(b, append(get(b), size(get(b)))) }
};

// append by owning and putting. inevitable after own()
append_by_put_own(b) {
    do { l = own(b); put(b, append(l, size(l))) }
};

// append by updating. box is owned prior to inc invocation, so is inevitable
append_by_update(b) {
    update(b, { l => append(l, size(l))})
};

// run each of our test mutators with the given number of boxes, iters, tasks
test(nboxes, ntasks, niters) {
    (n0, n1) = (0, ntasks * niters);
    print("1. increment by put(get)");
    res1 = bench({ harness(inc_by_put_get, n0, n1, nboxes, ntasks, niters) });
    print(res1);
    assert_equals({true}, {res1.result});
    print("2. increment by put(own)");
    res2 = bench({ harness(inc_by_put_own, n0, n1, nboxes, ntasks, niters) });
    print(res2);
    assert_equals({true}, {res2.result});
    print("3. increment by update");
    res3 = bench({ harness(inc_by_update, n0, n1, nboxes, ntasks, niters) });
    print(res3);
    assert_equals({true}, {res3.result});

    (l0, l1) = ([], count(n1));
    print("4. append by put(get)");
    res4 = bench({ harness(append_by_put_get, l0, l1, nboxes, ntasks, niters) });
    print(res4);
    assert_equals({true}, {res4.result});
    print("5. append by put(own)");
    res5= bench({ harness(append_by_put_own, l0, l1, nboxes, ntasks, niters) });
    print(res5);
    assert_equals({true}, {res5.result});
    print("6. append by update");
    res6 = bench({ harness(append_by_update, l0, l1, nboxes, ntasks, niters) });
    print(res6);
    assert_equals({true}, {res6.result});
};

// ---

// spawn so we can check things through console while it's running
// spawn {
//    test(10, 100, 3000)
// };

// smoke test: run inline
test(10, 100, 3000)

// overnights - paste into shell, will run until something goes wrong

// spawn { while({bench({harness(inc_by_put_get, 0, 20000, 1, 10, 2000)}).result}, {()}) }
// spawn { while({bench({harness(inc_by_put_own, 0, 20000, 1, 10, 2000)}).result}, {()}) }
// spawn { while({bench({harness(inc_by_update, 0, 20000, 1, 10, 2000)}).result}, {()}) }
// spawn { while({bench({harness(append_by_put_get, [], count(20000), 1, 10, 2000)}).result}, {()}) }
// spawn { while({bench({harness(append_by_put_own, [], count(20000), 1, 10, 2000)}).result}, {()}) }
// spawn { while({bench({harness(append_by_update, [], count(20000), 1, 10, 2000)}).result}, {()}) }

