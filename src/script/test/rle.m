
import unittest;

// encode list of T into sequence of (#occs, T) pairs
encode(list) { runs(list) | { l => (size(l), head(l)) } };

// decode (#occs, T) pairs into list
decode(runs) { reduce({ l, r => l + rep(r) }, [], runs) };

// -------------------------

// round trip
rt = encode $ decode;

// test
test(list) {
    enc = encode(list);
    dec = decode(enc);
    print(list);
    print("=>");
    print(enc);
    print("=>");
    print(dec);
    assert_equals({list}, {dec});
};

nlist = draw(25, 3);
test(nlist);

slist = mapll(nlist, ["here", "we", "go"]);
test(slist);

// ------------------------------------------------

/*

10 / 5    // 2

{<n>/5} @ count(10) // == [0,0,0,0,0,1,1,1,1,1]

count(10) / {<n>/5}  // == [0: [0,1,2,3,4], 1: [5,6,7,8,9]]

// invariant: for maps m:T->Num and func f = {<x>/n},
// size(m / f) == size(m) / f



*/
