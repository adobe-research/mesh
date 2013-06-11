
import unittest;

// Example from Milner's 1978 paper
// http://www.classes.cs.uchicago.edu/archive/2007/spring/32001-1/papers/milner-type-poly.pdf

cross(f, g) {
    { x, y => (f(x), g(y)) }
};

pair(a) {
    { b => (a, b) }
};

// inferred type should be: <A, B, C> A -> ((B, C) -> ((A, B), (A, C)))
tagpair(a) {
    tag = pair(a);
    cross(tag, tag)
};

assert_equals({((0, "hey"), (0, true))}, {tagpair(0)("hey", true)});
