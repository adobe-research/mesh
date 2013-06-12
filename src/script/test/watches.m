// Test multiwaiters using watches/unwatches

import std;
import unittest;

// First do a quick little test to verify watch and watches can be applied
// multiple times

(b0, b1, b2, counter) = boxes(0, 0, 0, 0);

inc_counter(old,new) { counter <- inc };
inc_counter2(old,new) { counter <- inc };

watch(b0, inc_counter);
watch(b0, inc_counter);
watch(b0, inc_counter);

watches((b1, b2), inc_counter2);
watches((b1, b2), inc_counter2);
watches((b1,), inc_counter2);
watches((b1,), inc_counter2);
watches((b2,), inc_counter2);
watches((b2,), inc_counter2);
watches((b2,), inc_counter2);

b0 <- inc;
b1 <- inc;
b2 <- inc;

assert_equals( { 12 }, { *counter } );

unwatch(b0, inc_counter);
unwatches((b1,b2), inc_counter2);
unwatches((b1,), inc_counter2);
unwatches((b2,), inc_counter2);

b0 <- inc;
b1 <- inc;
b2 <- inc;

assert_equals( { 19 }, { *counter } );

unwatch(b0, inc_counter);
unwatch(b0, inc_counter);
unwatches((b1,b2), inc_counter2);
unwatches((b1,), inc_counter2);
unwatches((b1,), inc_counter2);
unwatches((b2,), inc_counter2);
unwatches((b2,), inc_counter2);

b0 <- inc;
b1 <- inc;
b2 <- inc;

assert_equals( { 19 }, { *counter } );


// Now the full watcher test

n = 4;

type EightInt = (Int, Int, Int, Int, Int, Int, Int, Int);

b = rep(n * 2, 0) | box;

toArray(x:EightInt) {
    [ x.0, x.1, x.2, x.3, x.4, x.5, x.6, x.7 ]
};

toTuple(a:[Box(Int)]) {
    ( a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7] )
};

watcher_invoke_count = box(0);

watcher(old, new) {
    watcher_invoke_count <- inc;
    a = toArray(new);

    // Each pair should be consistent
    for(count(n), { i =>
        // when(a[i * 2] != a[i * 2 + 1], { print a });
        assert_equals({ a[i * 2] }, { a[i * 2 + 1] })
    });
};

verify_invoke_count() {
    // Watcher should be called exactly once for each transaction
    even_sum = sum(mapll(count(n) @* 2, b | get));
    assert_equals( { even_sum }, { *watcher_invoke_count } );
};

mutate(rep_count) {
    pfor(count(n), { i =>
        repeat(rep_count, {
            do {
                b[i * 2] <- inc;
                sleep(rand(200));
                b[i * 2 + 1] <- inc;
            }
        })
    });
};

watches(toTuple(b), watcher);

puts((b[0], b[1]), (1,1));

mutate(100);
verify_invoke_count();

saved_invoke_count = *watcher_invoke_count;

// Create an 8-tuple containing only the odd values
odds = index(b) | { i => if(i % 2 == 0, {b[i + 1]}, {b[i]}) };

// Unwatch all the odd boxes
unwatches(toTuple(odds), watcher);

pfor(count(n), { i => repeat(10, { b[i * 2 + 1] <- inc }) });
pfor(count(n), { i => repeat(10, { b[i * 2 + 1] <- dec }) });

// verify that the watcher was not triggered
assert_equals({ saved_invoke_count }, { *watcher_invoke_count });

// Check that watcher is triggered when just one box is changed
mutate(10);
verify_invoke_count();

final_invoke_count = *watcher_invoke_count;

// Now unwatch the evens too
evens = index(b) | { i => if(i % 2 == 0, {b[i]}, {b[i - 1]}) };
unwatches(toTuple(evens), watcher);

mutate(10);
assert_equals({ final_invoke_count }, { *watcher_invoke_count });
print b;
