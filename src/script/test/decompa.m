
import unittest;

// decomposing assignment tests

// to defeat constant propagation
ri = rand(10);

// combinations of
// LHS = list, map, tuple, record
// RHS = literal, expression

// list LHS

// var list = literal
{
    [x, y, z] = [ri + 0, ri + 1, ri + 2];
    assert_equals({[ri + 0, ri + 1, ri + 2]}, {[x, y, z]});
}();

// var list = expression
{
    l = range(ri, 3);
    [x, y, z] = l;
    assert_equals({l}, {[x, y, z]});
}();

// map LHS

// var map = literal (in order)
{
    [#a: x, #b: y, #c: z] = [#a: ri + 0, #b: ri + 1, #c: ri + 2];
    assert_equals({[#a: ri + 0, #b: ri + 1, #c: ri + 2]}, {[#a: x, #b: y, #c: z]});
}();

// var map = literal (out of order)
{
    [#a: x, #b: y, #c: z] = [#c: ri + 2, #b: ri + 1, #a: ri + 0];
    assert_equals({[#c: ri + 2, #b: ri + 1, #a: ri + 0]}, {[#a: x, #b: y, #c: z]});
}();

// var map = expression (in order)
{
    m = assoc([#a, #b, #c], range(ri, 3));
    [#a: x, #b: y, #c: z] = m;
    assert_equals({m}, {[#a: x, #b: y, #c: z]});
}();

// var map = expression (out of order)
{
    m = assoc([#c, #b, #a], range(ri, 3));
    [#a: x, #b: y, #c: z] = m;
    assert_equals({m}, {[#a: x, #b: y, #c: z]});
}();

// tuple LHS

// tuple = literal
{
    (x, y, z) = (ri, i2s(ri), ri == 0);
    assert_equals({(ri, i2s(ri), ri == 0)}, {(x, y, z)});
}();

// tuple = expression
{
    t = (ri, i2s(ri), ri == 0);
    (x, y, z) = t;
    assert_equals({t}, {(x, y, z)});
}();

// record LHS

// record = literal (in order)
{
    (#a: x, #b: y, #c: z) = (#a: ri, #b: i2s(ri), #c: ri == 0);
    assert_equals({(#a: ri, #b: i2s(ri), #c: ri == 0)}, {(#a: x, #b: y, #c: z)});
}();

// record = literal (out of order)
{
    (#a: x, #b: y, #c: z) = (#c: false, #b: "hey", #a: 0);
    assert_equals({(#c: false, #b: "hey", #a: 0)}, {(#a: x, #b: y, #c: z)});
}();

// record = expression (in order)
{
    r = (#a: ri, #b: i2s(ri), #c: ri == 0);
    (#a: x, #b: y, #c: z) = r;
    assert_equals({r}, {(#a: x, #b: y, #c: z)});
}();

// print("record = expression (out of order)
{
    r = (#c: ri == 0, #b: i2s(ri), #a: ri);
    (#a: x, #b: y, #c: z) = r;
    assert_equals({r}, {(#a: x, #b: y, #c: z)});
}();

// spot check nesting cases

// nested, list outer
{
    r0 = (#a: ri + 0, #b: ri + 1);
    r1 = (#c: ri + 2, #d: ri + 3);
    t0 = (r0, r1);
    t1 = (r0, r1);
    m0 = [#x: t0, #y: t1];
    m1 = [#x: t0, #y: t1];
    v = [m0, m1];

    [[#x: ((#a: a0, #b: b0), (#c: c0, #d: d0)),
        #y: ((#a: a1, #b: b1), (#c: c1, #d: d1))],
     [#x: ((#a: a2, #b: b2), (#c: c2, #d: d2)),
             #y: ((#a: a3, #b: b3), (#c: c3, #d: d3))]] = v;

    assert_equals({v}, 
        {[[#x: ((#a: a0, #b: b0), (#c: c0, #d: d0)),
           #y: ((#a: a1, #b: b1), (#c: c1, #d: d1))],
          [#x: ((#a: a2, #b: b2), (#c: c2, #d: d2)),
           #y: ((#a: a3, #b: b3), (#c: c3, #d: d3))]]});
}();

// nested, tuple outer
{
    lv = range(ri, 3);
    mv = assoc([#a, #b, #c], range(ri, 3));
    tv = (ri, i2s(ri), ri == 0);
    rv = (#a: ri, #b: i2s(ri), #c: ri == 0);
    v = (lv, mv, tv, rv);
    ([g, h, i], [#a: j, #b: k, #c: l], (a, b, c), (#a: d, #b: e, #c: f)) = v;

    assert_equals({v},
        {([g, h, i],
          [#a: j, #b: k, #c: l],
          (a, b, c),
          (#a: d, #b: e, #c: f))});
}();
