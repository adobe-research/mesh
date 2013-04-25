
import * from std;
import * from unittest;

// test fixed-shape transaction funcs
// note: tests high-level logic only, not atomicity.
// TODO stress tuplized shape funcs in ttran.m
// TODO use asserteq()

(bi, bs, bb) = (box(0), box("hey"), box(true));

//
// get, gets
//

assert_equals({get(bi)}, {*bi});
assert_equals({0}, {*bi});

assert_equals({gets(bi, bs, bb)}, {**(bi, bs, bb)});
assert_equals({(0, "hey", true)}, {**(bi, bs, bb)});

//
// put, puts
//

bi := 1;                            // put(bi, 1)
assert_equals({1}, {*bi});


(bi, bs, bb) ::= (2, "hey!", false);    // puts((bi, bs, bb), (2, "hey!", false))

assert_equals({(2, "hey!", false)}, {**(bi, bs, bb)});

//
// update, updates
//

bi <- inc;                      // update(bi, inc);
assert_equals({3}, {*bi});

uf(i, s, b) { (inc(i), s + "!", !b) };

(bi, bs, bb) <<- uf;            // updates((bi, bs, bb), uf)
assert_equals({(4, "hey!!", true)}, {**(bi, bs, bb)});

//
// transfer, transfers
//

transfer(bi, strlen, bs);

assert_equals({5}, {*bi});

tf(s, i) {
    (strlen(s) + 1, i2s(i) + "!", s == "hey!!")
};

transfers((bi, bs, bb), tf, (bs, bi));

assert_equals({(6, "5!", true)}, {**(bi, bs, bb)});
