
import * from std;
import * from unittest;

// type annotation syntax tests
// not exhaustive, since intrinsic declarations stress this already.
// currently only testing the more esoteric stuff.

// MO is take an example intrinsic with a tricky type,
// create (a) an annotated let, (b) an annotated wrapper function,
// and verify that they produce usable definitions.

//
// gets is tuplized get:
// takes a tuple of boxes and produces the corresponding tuple of values
//
gets_bind : <Ts:[*]> Tup(Ts | Box) -> Tup(Ts) = gets;
assert_equals({gets(box(0), box("hey"))}, {gets_bind(box(0), box("hey"))});

<Ts:[*]> gets_wrap(boxes: Tup(Ts | Box)) -> Tup(Ts) { gets(boxes) };
assert_equals({gets(box(0), box("hey"))}, {gets_wrap(box(0), box("hey"))});

//
// updates is tuplized update:
// takes a tuple of boxes and an endofunction on the corresponding
// tuple of values, and applies the update function to the boxes en masse
//
updates_bind : <T:[*]> (Tup(T | Box), Tup(T) -> Tup(T)) -> () = updates;
assert_equals(
    {updates((box(0), box(1)), fuse(inc, dec))},
    {updates_bind((box(0), box(1)), fuse(inc, dec))});

<T:[*]> updates_wrap(boxes: Tup(T | Box), func: Tup(T) -> Tup(T)) -> () {
    updates(boxes, func)
};

assert_equals(
    {updates((box(0), box(1)), fuse(inc, dec))},
    {updates_wrap((box(0), box(1)), fuse(inc, dec))});

//
// transfers is tuplized transfer:
// takes a tuple of output boxes, a function from input value tuple
// to output value tuple, and a tuple of input boxes, and
// writes to the output boxes the result of running the function
// on values read from the input boxes.
//
transfers_bind
    : <Outs:[*], Ins:[*]> (Tup(Outs | Box), Tup(Ins) -> Tup(Outs), Tup(Ins | Box)) -> ()
    = transfers;

assert_equals(
    {transfers((box(0), box(1)), fuse(plus, times), (box(2, 2), box(4, 4)))},
    {transfers_bind((box(0), box(1)), fuse(plus, times), (box(2, 2), box(4, 4)))});

<Outs:[*], Ins:[*]> transfers_wrap(
    outs: Tup(Outs | Box),
    func: Tup(Ins) -> Tup(Outs),
    ins: Tup(Ins | Box)) -> ()
{
    transfers(outs, func, ins)
};

assert_equals(
    {transfers((box(0), box(1)), fuse(plus, times), (box(2, 2), box(4, 4)))},
    {transfers_wrap((box(0), box(1)), fuse(plus, times), (box(2, 2), box(4, 4)))});

//
// zip takes a tuple of lists, and produces a list of tuples
//
zip_bind : <Ts:[*]> Tup(Ts | List) -> [Tup(Ts)] = zip;
assert_equals(
    {zip([0, 1, 2], ["zero", "one", "two"], [true, false, true])},
    {zip_bind([0, 1, 2], ["zero", "one", "two"], [true, false, true])});

<Ts:[*]> zip_wrap(lists: Tup(Ts | List)) -> [Tup(Ts)] { zip(lists) };
assert_equals(
    {zip([0, 1, 2], ["zero", "one", "two"], [true, false, true])},
    {zip_wrap([0, 1, 2], ["zero", "one", "two"], [true, false, true])});

//
// unzip is the inverse of zip
//
unzip_bind : <Ts:[*]> [Tup(Ts)] -> Tup(Ts | List) = unzip;
assert_equals(
    {unzip([(0, "zero", true), (1, "one", false), (2, "two", true)])},
    {unzip_bind([(0, "zero", true), (1, "one", false), (2, "two", true)])});

<Ts:[*]> unzip_wrap(tups: [Tup(Ts)]) -> Tup(Ts | List) { unzip(tups) };
assert_equals(
    {unzip([(0, "zero", true), (1, "one", false), (2, "two", true)])},
    {unzip_wrap([(0, "zero", true), (1, "one", false), (2, "two", true)])});

//
// mapz is a transform of functional map for use with functions that take
// tupled arguments: instead of taking a list of argument tuples on the left,
// it takes a tuple of argument lists, and does tupling on the fly.
// I.e. mapz(arglist, f) == map(zip(arglist), f)
// a.k.a. mapz == fuse(zip, id) $ map
//
mapz_bind : <T:[*], X> (Tup(T | List), Tup(T) -> X) -> [X] = mapz;
assert_equals(
    {mapz(([0, 1, 2], [3, 4, 5]), (+))},
    {mapz_bind(([0, 1, 2], [3, 4, 5]), (+))});

<T:[*], X> mapz_wrap(arglists: Tup(T | List), func: Tup(T) -> X) -> [X] {
    mapz(arglists, func)
};

assert_equals(
    {mapz(([0, 1, 2], [3, 4, 5]), (+))},
    {mapz_wrap(([0, 1, 2], [3, 4, 5]), (+))});
