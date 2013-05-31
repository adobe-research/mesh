import * from std;
import * from unittest;


// cas = { b, o, n => do({ and(eq(own(b), o), { put(b, n); true }) }) }
assert_equals({ true }, { x = box(1); cas(x, 1, 3) });
assert_equals({ 3 }, { x = box(1); cas(x, 1, 3); *x });
assert_equals({ false }, { x = box(1); cas(x, 2, 3) });
assert_equals({ 1 }, { x = box(1); cas(x, 2, 3); *x });

// cau = { b, o, f => do({ and(eq(own(b), o), { put(b, f(o)); true }) }) }
assert_equals({ true }, { x = box(1); cau(x, 1, inc) });
assert_equals({ 2 }, { x = box(1); cau(x, 1, inc); *x });
assert_equals({ false }, { x = box(1); cau(x, 2, inc) });
assert_equals({ 1 }, { x = box(1); cau(x, 2, inc); *x });

// tas = { b, p, n => do({ o = own(b); (and(p(o), { put(b, n); true }), o) }) }
assert_equals({ (true, 2) }, { x = box(2); tas(x, { $0 > 1 }, 3) });
assert_equals({ 3 }, { x = box(2); tas(x, { $0 > 1 }, 3); *x });
assert_equals({ (false, 1) }, { x = box(1); tas(x, { $0 > 1 }, 3) });
assert_equals({ 1 }, { x = box(1); tas(x, { $0 > 1 }, 3); *x });

// tau = { b, p, f => do({ o = own(b); (and(p(o), { put(b, f(o)); true }), o) }) }
assert_equals({ (true, 2) }, { x = box(2); tau(x, { $0 > 1 }, inc) });
assert_equals({ 3 }, { x = box(2); tau(x, { $0 > 1 }, inc); *x });
assert_equals({ (false, 1) }, { x = box(1); tau(x, { $0 > 1 }, inc) });
assert_equals({ 1 }, { x = box(1); tau(x, { $0 > 1 }, inc); *x });

// wtau = { b, p, f => await(b, p); tau(b, p, f) }
assert_equals({ 10 }, {
                    done = box(false);
                    x = box(2);
                    // once x is > 3, then set the value of x to 10
                    spawn{ wtau(x, {$0 >3 }, { $0; 10; }); done := true; };
                    x <- inc; // x == 3
                    x <- inc; // x == 4, and now wtau() will complete and set to 10
                    await(done, { eq(true, $0) }); // need to give the spawned thread a second to complete
                    *x;
                    });

// cast = { bs, os, ns => do({ and(eq(owns(bs), os), { puts(bs, ns); true }) }) }
assert_equals({ true }, { x = box(3); y = box(6); cast((x,y), (3,6), (4,7)) });
assert_equals({ (4, 7) }, { x = box(3); y = box(6); cast((x,y), (3,6), (4,7)); (*x, *y) });
assert_equals({ false }, { x = box(3); y = box(6); cast((x,y), (1,1), (4,7)) });
assert_equals({ (3, 6) }, { x = box(3); y = box(6); cast((x,y), (1,1), (4,7)); (*x, *y) });


// caut = { bs, os, f => do({ and(eq(owns(bs), os), { puts(bs, f(os)); true }) }) }
assert_equals({ true }, { x = box(3); y = box(6); caut((x,y), (3,6), {a,b=> (inc(a), inc(b))}) });
assert_equals({ (4, 7) }, { x = box(3); y = box(6); caut((x,y), (3,6), {a,b=> (inc(a), inc(b))}); (*x, *y) });
assert_equals({ false }, { x = box(3); y = box(6); caut((x,y), (1,1), {a,b=> (inc(a), inc(b))}) });
assert_equals({ (3, 6) }, { x = box(3); y = box(6); caut((x,y), (1,1), {a,b=> (inc(a), inc(b))}); (*x, *y) });

// swap = { s, v => act(s, { lst => (list:append(list:drop(-1, lst), v), list:last(lst)) }) }
assert_equals({ 3 }, { x = box([1,2,3]); swap(x, 2); });
assert_equals({ [1,2,2] }, { x = box([1,2,3]); swap(x, 2); *x });

// tast = { bs, p, ns => do({ os = owns(bs); (and(p(os), { puts(bs, ns); true }), os) }) }
assert_equals({ (true, (2, 3)) }, { x = box(2); y = box(3); tast((x, y), { a:(Int, Int) => a.0 > 1 && {a.1 > 2} }, (7, 8)) });
assert_equals({ (7, 8) }, { x = box(2); y = box(3); tast((x, y), { a:(Int, Int) => a.0 > 1 && {a.1 > 2} }, (7, 8)); (*x, *y) });
assert_equals({ (false, (2, 3)) }, { x = box(2); y = box(3); tast((x, y), { a:(Int, Int) => a.0 < 1 && {a.1 > 2} }, (7, 8)) });
assert_equals({ (2, 3) }, { x = box(2); y = box(3); tast((x, y), { a:(Int, Int) => a.0 < 1 && {a.1 > 2} }, (7, 8)); (*x, *y) });

// taut = { bs, p, f => do({ os = owns(bs); (and(p(os), { puts(bs, f(os)); true }), os) }) }
assert_equals({ (true, (2, 3)) }, {
                                    x = box(2);
                                    y = box(3);
                                    taut((x, y), { a:(Int, Int) => a.0 > 1 && {a.1 > 2} }, { a:(Int, Int) => (11, 22)})
                                   });

assert_equals({ (11, 22) }, {
                            x = box(2);
                            y = box(3);
                            taut((x, y), { a:(Int, Int) => a.0 > 1 && {a.1 > 2} }, { a:(Int, Int) => (11, 22)});
                            (*x, *y)
                            });

assert_equals({ (false, (2, 3)) }, {
                                    x = box(2);
                                    y = box(3);
                                    taut((x, y), { a:(Int, Int) => a.0 < 1 && {a.1 > 2} }, { a:(Int, Int) => (11, 22)})
                                   });

assert_equals({ (2, 3) }, {
                            x = box(2);
                            y = box(3);
                            taut((x, y), { a:(Int, Int) => a.0 < 1 && {a.1 > 2} }, { a:(Int, Int) => (11, 22)});
                            (*x, *y)
                            });


// wtaut = { bs, p, f => awaits(bs, p); taut(bs, p, f) }
assert_equals({ (11, 22) }, {
                    done = box(false);
                    x = box(2);
                    y = box(3);
                    // once x is > 3, then set the value of x to 10
                    spawn{ taut((x, y), { a:(Int, Int) => a.0 > 1 && {a.1 > 2} }, { a:(Int, Int) => (11, 22)}); done := true; };
                    x <- inc; // x == 3
                    x <- inc; // x == 4, and now wtau() will complete and set to 10
                    await(done, { eq(true, $0) }); // need to give the spawned thread a second to complete
                    (*x, *y)
                    });

// produce = { c, q, n, f => nf = { l => lt(list:size(l), n) }; while({ get(c) }, { mutate:awaits((c, q), { b, l => or(not(b), { nf(l) }) }); loop:when(get(c), { v = f(); while({ and(get(c), { not(mutate:tau(q, nf, { $0_90_36 => list:append($0_90_36, v) }).0) }) }, { () }); () }); () }); () }
// TODO

// consume = { c, q, f => ne = { l => gt(list:size(l), 0) }; while({ get(c) }, { mutate:awaits((c, q), { b, l => or(not(b), { ne(l) }) }); loop:when(get(c), { _118_13 = mutate:tau(q, ne, list:rest); b = _118_13.0; l = _118_13.1; loop:when(b, { f(list:first(l)) }); () }); () }); () }
// TODO

// act = <s, r> { b: *s, f: s -> (s, r) => do({ _94_9 = f(own(b)); next = _94_9.0; result = _94_9.1; put(b, next); result }) }
// first part of tuple is the new value for the box and the other value is the value returned
assert_equals({ 2 }, { x = box(2); act(x, {(inc($0), $0)}) });
assert_equals({ 3 }, { x = box(2); act(x, {(inc($0), $0)}); *x });


// postput = { b, v => act(b, { $0_103_24 => (v, $0_103_24) }) }
assert_equals({ 2 }, { x = box(2); postput(x, 12) });
assert_equals({ 12 }, { x = box(2); postput(x, 12); *x; });

// preupdate : { b, f => act(b, { $0_269_26 => v = f($0_269_26); (v, v) }) }

// postupdate = { b, f => act(b, { $0_108_27 => (f($0_108_27), $0_108_27) }) }
assert_equals({ 2 }, { x = box(2); postupdate(x, inc) });
assert_equals({ 3 }, { x = box(2); postupdate(x, inc); *x; });


// predec = { b => act(b, compose(dec, twin)) }
assert_equals({ (2, 1, 1) }, { x = box(2); (*x, predec(x), *x) });

// preinc = { b => act(b, compose(inc, twin)) }
assert_equals({ (2, 3, 3) }, { x = box(2); (*x, preinc(x), *x) });


// postdec = { b => postupdate(b, dec) }
assert_equals({ (2, 2, 1) }, { x = box(2); (*x, postdec(x), *x) });

// postinc = { b => postupdate(b, inc) }
assert_equals({ (2, 2, 3) }, { x = box(2); (*x, postinc(x), *x) });

// pushfront = { blst, v => update(blst, { $0_245_30 => plus([v], $0_245_30) }); () }
assert_equals({ [4,1,2,3] }, { x = box([1,2,3]); pushfront(x, 4); *x });

// pushback = { blst, v => update(blst, { $0_246_29 => list:append($0_246_29, v) }); () }
assert_equals({ [1,2,3,4] }, { x = box([1,2,3]); pushback(x, 4); *x });


// popfront = { blst => act(blst, { $0_248_28 => (list:rest($0_248_28), list:first($0_248_28)) }) }
assert_equals({ 1 }, { x = box([1,2,3,4]); popfront(x) });
assert_equals({ [2,3,4] }, { x = box([1,2,3,4]); popfront(x); *x }); // make sure that the box is modified

// popback = { blst => act(blst, { $0_249_27 => (list:drop(-1, $0_249_27), list:last($0_249_27)) }) }
assert_equals({ 4 }, { x = box([1,2,3,4]); popback(x) });
assert_equals({ [1,2,3] }, { x = box([1,2,3,4]); popback(x); *x }); // make sure that the box is modified

// peekfront = { blst => list:first(get(blst)) }
assert_equals({ 1 }, { peekfront(box([1,2,3])) });

// peekback = { blst => list:last(get(blst)) }
assert_equals({ 3 }, { peekback(box([1,2,3])) });

// dep = { src, f => sink = box(f(get(src))); react(src, { $0_290_16 => put(sink, f($0_290_16)); () }); sink }
assert_equals({ 1 }, { 
                    src = box(0);
                    track = dep(src, id);
                    src <- inc;
                    *track;
                    });

// deps = { sources, f => sink = box(f(map(sources, get))); updater = { v => do({ put(sink, f(map(sources, get))); () }); () }; map(sources, { $0_305_15 => react($0_305_15, updater) }); sink }
assert_equals({ 3 }, { 
                    x = box(0);
                    y = box(1);
                    z = deps([x, y], sum);
                    x := 2; // z should now be sum(2, 1)
                    *z;
                    });








