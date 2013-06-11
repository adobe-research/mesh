
import unittest;

// inline param syntax
//
// Inline params are an alternative to ordinary named params.
// An inline param name consists of a series of one or more
// dollar signs ($) followed by a nonnegative whole number.
//
// The number denotes the parameter's position, with 0
// denoting the first. Gaps are allowed: if a lambda
// mentions contains inline parameter refs $0 and $2,
// an unused param $1 is inferred and given polymorphic
// type.
//
// The quantity of dollar signs denotes the depth of the
// reference, with a single dollar sign denoting a ref to
// the lambda in which the reference immediately occurs.
// Additional dollar signs denote lambdas enclosing the
// reference more and more remotely. The lambda to which
// the reference resolves must also use inline parameters,
// not named parameters.
//

// simple cases

a = { $0 };
assert_equals({ 0 }, { a(0) });

b = { [$0, $1] };
assert_equals({ [0, 1] }, { b(0, 1) });

// twisted
c = { [$1, $0] };
assert_equals({ [0, 1] }, { c(1, 0) });

// gap
d = { [$0, $2] };
assert_equals({ [0, 1] }, { d(0, "hey", 1) });

// nested ref to previously mentioned param
e = { $0 > 0 && { $$0 < 10 } };
assert_equals({ true }, { e(5) });

// nested ref to otherwise-unmentioned param
f = { $0 > 0 && { $$1 < 10 } };
assert_equals({ true }, { f(5, 5) });

// nested refs to params at same position but
// different nesting levels
g = { { { $$$0($$0) } } };
assert_equals({ 2 }, { g(inc)(1)() });
