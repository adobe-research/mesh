
import unittest;

// quick tests of various application syntax

// TODO will need to track changes to type system
// for avoiding list/map, tuple/record overloading

// ------------------------------

//
// Function application
//

// Ordinary function call syntax is juxtaposition. Multiple
// args are tupled, so call sites with a tuple literal argument
// look conventional. It's idiomatic to parenthesize the argument
// even when not needed. Also, calls which apply list literals
// must parenthesize their arguments, to avoid using collection
// indexing syntax: f([x, y]) instead of f [x, y].

// func app 0 args
one() { 1 };
assert_equals({1}, {one()});

// func app 1 arg, no parens (legal but not idiomatic)
assert_equals({1}, {(inc 0)});

// func app 1 arg, parens
assert_equals({1}, {inc(0)});

// func app 2 args (tuple literal)
assert_equals({4}, {plus(2, 2)});

// func app 3 args (tuple literal)
assert_equals( {"no"}, {if(false, {"yes"}, {"no"})});

// function taking 1-tuple -- edge case
onetup(t:(Int,)) { t.0 };
assert_equals({3}, {onetup(3,)});

// ------------------------------

//
// Collection indexing
//

// Lists and maps are collections, and share indexing syntax
// in which the key(s) or position(s) are enclosed in square
// brackets, e.g. list[position],  map[key].

// list indexing
list = reverse(count(10));   // [9, 8, 7, ..., 0]
assert_equals({9}, {list[0]});

// map indexing
map = [#a: 5, #b: 4, #c: 3, #d: 2, #e: 1, #f: 0];
assert_equals({5}, {map[#a]});

// ------------------------------

//
// Structure addressing
//

// Tuples and records are structures, and share addressing syntax
// in which the structure and address are separated by an infix dot.
// The address must be constant, or must dereference to one. In the
// special case of records addressed by symbol literals, the backquote
// of the addressing symbol may be omitted, giving the conventional
// record syntax. This means that let-bound constant addresses must
// be enclosed in parens.

// addressing tuple by int literal
tup = (0, "hey", false);
assert_equals({0}, {tup.0});

// addressing a tuple by a let-bound Int constant
// Note parens to avoid symbol-addressed record syntax.
ONE = 1;
assert_equals({"hey"}, {tup.(ONE)});

// addressing a record by a symbol literal, sugared
rec = (age: 30, name: "bob");
assert_equals({30}, {rec.age});

// addressing a record by a symbol literal, unsugared
assert_equals({30}, {rec.(#age)});

// addressing a record by a string literal
rec2 = ("age": 30, "name": "bob");
assert_equals({30}, {rec2."age"});


