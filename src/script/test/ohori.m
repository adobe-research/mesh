//
// tests for ohori polymorphic recs/variants
//

import unittest;

// function should have inferred type
// <A:(#x: B, #y: B), B> A -> B
//
add_xy(r) { r.x + r.y };

assert_equals({ 20 }, { add_xy(#x: 10, #y: 10) });
assert_equals({ 20 }, { add_xy(#y: 10, #x: 10) });
assert_equals({ 20 }, { add_xy(#x: 10, #y: 10, #z: "hey") });
assert_equals({ 20 }, { add_xy(#z: "hey", #y: 10, #x: 10) });
assert_equals({ [1, 2, 3] }, { add_xy(#x: [1, 2], #y: [3]) });
assert_equals({ [1, 2, 3] }, { add_xy(#y: [3], #x: [1, 2]) });
assert_equals({ [1, 2, 3] }, { add_xy(#x: [1, 2], #y: [3], #z: "hey") });
assert_equals({ [1, 2, 3] }, { add_xy(#z: "hey", #y: [3], #x: [1, 2]) });

// function should have inferred type
// <A:(#x: B, #y: B, #z: B), B> A -> B
//
add_xyz(r) { r.x + r.y + r.z };

assert_equals({ 30 }, { add_xyz(#x: 10, #y: 10, #z: 10) });
assert_equals({ 30 }, { add_xyz(#x: 10, #z: 10, #y: 10) });
assert_equals({ 30 }, { add_xyz(#x: 10, #y: 10, #z: 10, #w: "hey") });
assert_equals({ 30 }, { add_xyz(#w: "hey", #x: 10, #z: 10, #y: 10) });
assert_equals({ [1, 2, 3, 4, 5] }, { add_xyz(#x: [1, 2], #y: [3], #z: [4, 5]) });
assert_equals({ [1, 2, 3, 4, 5] }, { add_xyz(#x: [1, 2], #z: [4, 5], #y: [3]) });
assert_equals({ [1, 2, 3, 4, 5] }, { add_xyz(#x: [1, 2], #y: [3], #z: [4, 5], #w: "hey") });
assert_equals({ [1, 2, 3, 4, 5] }, { add_xyz(#w: "hey", #x: [1, 2], #z: [4, 5], #y: [3]) });

// function should have inferred type
// <A:(#y: B, #x: C), B, C> A -> (B, C)
//
tup_yx(r) { (r.y, r.x) };

assert_equals({ ("hey", 10) }, { tup_yx(#x: 10, #y: "hey") });
assert_equals({ ("hey", 10) }, { tup_yx(#x: 10, #y: "hey", #z: 10) });
assert_equals({ ([3], [1, 2]) }, { tup_yx(#x: [1, 2], #y: [3]) });
assert_equals({ ([3], [1, 2]) }, { tup_yx(#x: [1, 2], #y: [3], #z: "hey") });

// function should have inferred type
// <A:(#x: B, #y: B, #z: B), B> A -> (B, (B, B))
//
add_tup(r) { (add_xyz(r), tup_yx(r)) };

assert_equals({ (60, (20, 10)) }, { add_tup(#x: 10, #y: 20, #z: 30) });
assert_equals({ (60, (20, 10)) }, { add_tup(#x: 10, #y: 20, #z: 30, #w: "hey") });
assert_equals({ ([1, 2, 3, 4, 5], ([3], [1, 2])) }, { add_tup(#x: [1, 2], #y: [3], #z: [4, 5]) });
assert_equals({ ([1, 2, 3, 4, 5], ([3], [1, 2])) }, { add_tup(#x: [1, 2], #y: [3], #z: [4, 5], #w: "hey") });
