
import * from std;
import * from unittest;

//
// logic
//

// any : (A => ([A], A -> Bool) -> Bool)
assert_equals({ true }, { any([1,2,1], { eq(2, $0) }) });
assert_equals({ false }, { any([3,2,4,3], { eq(1, $0) }) });

// all : (A => ([A], A -> Bool) -> Bool)
assert_equals({ true }, { all([3,2,4,3], { lt(1, $0) }) });
assert_equals({ false }, { all([3,2,4,3], { gt(1, $0) }) });


//
// conditionals
//
// switch : (K, V => (K, [K : () -> V]) -> V)
assert_equals({ "0" }, { switch(0, [ 0:{ "0" }, 1:{ "1" }]) });

//
// arithmetic
//

// inc : Int -> Int
assert_equals({ 0 }, { inc(-1) });
assert_equals({ 2 }, { inc(1) });

// dec : Int -> Int
assert_equals({ -1 }, { dec(0) });
assert_equals({ 0 }, { dec(1) });

// abs : Int -> Int
assert_equals({ 1 }, { abs(-1) });
assert_equals({ 1 }, { abs(1) });
assert_equals({ 0 }, { abs(0) });
assert_equals({ 0 }, { abs(-0) });

// fabs : Double -> Double
assert_equals({ 1.0 }, { fabs(-1.0) });
assert_equals({ 1.0 }, { fabs(1.0) });
assert_equals({ 0.0 }, { fabs(0.0) });
assert_equals({ 0.0 }, { fabs(-0.0) });

// fsign : Double -> Int
assert_equals({ 1 }, { fsign(1.0) });
assert_equals({ -1 }, { fsign(-1.0) });
assert_equals({ 0 }, { fsign(-0.0) });
assert_equals({ 0 }, { fsign(0.0) });

// fmin : (Double, Double) -> Double
assert_equals({ 1.0 }, { fmin(1.0, 1.0) });
assert_equals({ -1.0 }, { fmin(1.0, -1.0) });
assert_equals({ -0.0 }, { fmin(-0.0, 1.0) });
assert_equals({ -0.0 }, { fmin(0.0, -0.0) });

// fmax : (Double, Double) -> Double
assert_equals({ 1.0 }, { fmax(1.0, 1.0) });
assert_equals({ 1.0 }, { fmax(1.0, -1.0) });
assert_equals({ 1.0 }, { fmax(-0.0, 1.0) });
assert_equals({ 0.0 }, { fmax(0.0, -0.0) });

// inrange : (Int, Int, Int) -> Bool
assert_equals({ true }, { inrange(4, 2, 5) });
assert_equals({ true }, { inrange(2, 2, 1) });
assert_equals({ false }, { inrange(1, 2, 1) });
assert_equals({ false }, { inrange(3, 2, 1) });

// finrange : (Double, Double, Double) -> Bool
assert_equals({ true }, { finrange(4.0, 2.0, 5.0) });
assert_equals({ true }, { finrange(2.0, 2.0, 1.0) });
assert_equals({ false }, { finrange(1.0, 2.0, 1.0) });
assert_equals({ false }, { finrange(3.0, 2.0, 1.0) });

// divz : (Int, Int) -> Int
assert_equals({ 2 }, { divz(4, 2) });
assert_equals({ 0 }, { divz(0, 2) });
assert_equals({ 0 }, { divz(3, 0) });


// fdivz : (Double, Double) -> Double
assert_equals({ 2.0 }, { fdivz(4.0, 2.0) });
assert_equals({ 0.0 }, { fdivz(0.0, 2.0) });
assert_equals({ 0.0 }, { fdivz(3.0, 0.0) });

/*
act = { T, X :: box: *T, action: T -> (T, X) => do({ _565_9 = action(own(box)); next = _565_9.0; result = _565_9.1; put(box, next); result }) }
all = { vals, pred => evolve_while(id, true, { _, val => pred(val) }, vals) }
any = { vals, pred => evolve_while(not, false, { _, val => pred(val) }, vals) }
apply = { f, x => f(x) }
assert_equals = { expected, actual => if(eq(expected(), actual()), { print(strcat(["[PASSED] ", tostr(expected), " == ", tostr(actual)])); true }, { logerror(strcat(["[FAILED] ", tostr(expected), " == ", tostr(actual), " Expected: ", tostr(expected()), " Actual: ", tostr(actual())])); false }) }
async = { f, cb => spawn({ cb(f()) }); () }
avg = { list => divz(sum(list), size(list)) }
bench = { b => t0 = millitime(); result = b(); (#time: l2i(lminus(millitime(), t0)), #result: result) }
benchn = { n, f => timer = { start = millitime(); f(); l2i(lminus(millitime(), start)) }; avg(repeat(n, timer)) }
cas = { b, o, n => do({ and(eq(own(b), o), { put(b, n); true }) }) }
cast = { bs, os, ns => do({ and(eq(owns(bs), os), { puts(bs, ns); true }) }) }
cau = { b, o, f => do({ and(eq(own(b), o), { put(b, f(o)); true }) }) }
caut = { bs, os, f => do({ and(eq(owns(bs), os), { puts(bs, f(os)); true }) }) }
chunks = { list, i => n = size(list); ravel(list, plus(div(n, i), sign(mod(n, i)))) }
compose = { f, g => { x => g(f(x)) } }
consume = { c, q, f => ne = { l => gt(size(l), 0) }; while({ get(c) }, { awaits((c, q), { b, l => or(not(b), { ne(l) }) }); when(get(c), { _753_13 = tau(q, ne, tail); b = _753_13.0; l = _753_13.1; when(b, { f(head(l)) }); () }); () }); () }
contains = { list, item => lt(find(list, item), size(list)) }
converge = { func, init => diff = { x, y => and(ne(x, y), { ne(y, init) }) }; next = { x, y => (y, func(y)) }; cycle(diff, (init, func(init)), next).0 }
countn = { extent, n => rangen(0, extent, n) }
counts = { list => inckey = { map, key => mapset(map, key, plus(1, mapgetd(map, key, 0))) }; reduce(inckey, [:], list) }
cross = { xs, ys => xn = size(xs); ixs = count(times(xn, size(ys))); zip(lleach(xs, map(ixs, { $0 => mod($0, xn) })), lleach(ys, map(ixs, { $0 => div($0, xn) }))) }

dep = { src, f => sink = box(f(get(src))); react(src, { $0 => put(sink, f($0)); () }); sink }
deps = { sources, f => sink = box(f(map(sources, get))); updater = { v => do({ put(sink, f(map(sources, get))); () }); () }; map(sources, { $0 => react($0, updater) }); sink }
difference = { list1, list2 => map2 = assoc(list2, [()]); unique(filter(list1, { $0 => not(iskey(map2, $0)) })) }
divz = { n, d => guard(eq(d, 0), 0, { div(n, d) }) }
eachpair = { init, f, args => list = plus([init], args); map(index(args), { i => f(list[i], list[plus(i, 1)]) }) }
edges = { T :: list: [T] => plus([0], filter(drop(1, index(list)), { $0 => ne(list[minus($0, 1)], list[$0]) })) }
enlist = { v => [v] }
evolve = { init, f, list => reduce(f, init, list) }

fan = { f, g => { $0 => (f($0), g($0)) } }
favg = { list => fdivz(fsum(list), i2f(size(list))) }
fdivz = { n, d => guard(eq(d, 0.0), 0.0, { fdiv(n, d) }) }
finrange = { x, base, extent => and(fge(x, base), { flt(x, plus(base, extent)) }) }
finspt = { list, val => firstwhere({ $0 => fle(val, $0) }, list) }
firstwhere = { pred, vals => n = size(vals); cycle({ i => and(lt(i, n), { not(pred(vals[i])) }) }, 0, inc) }
fmax = { x, y => iif(fge(x, y), x, y) }
fmin = { x, y => iif(fge(x, y), y, x) }
fmodz = { n, d => guard(eq(d, 0.0), 0.0, { fmod(n, d) }) }
fproduct = { ns => reduce(ftimes, 1.0, ns) }
fromton = { x, y, n => rangen(x, minus(y, x), n) }
fsign = { f => guard(fgt(f, 0.0), 1, { iif(flt(f, 0.0), -1, 0) }) }
fsq = { f => ftimes(f, f) }
fst = { X, Y :: p: (X, Y) => p.0 }
fsum = { ns => reduce(plus, 0.0, ns) }
fuse = { f, g => { $0, $1 => (f($0), g($1)) } }
head = first
id = { v => v }

index = { list => count(size(list)) }
inrange = { x, base, extent => and(ge(x, base), { lt(x, plus(base, extent)) }) }
inspt = { list, val => firstwhere({ $0 => le(val, $0) }, list) }
instr = { f => num_recs = box(0); avg_time = box(0.0); wrapped_f = { x => t0 = nanotime(); y = f(x); updates((num_recs, avg_time), { num, avg => elapsed = lminus(nanotime(), t0); new_num = plus(num, 1); new_avg = fdiv(plus(ftimes(avg, l2f(num)), l2f(elapsed)), l2f(new_num)); (new_num, new_avg) }); y }; (wrapped_f, (#num: num_recs, #avg: avg_time)) }
intersection = { list1, list2 => map2 = assoc(list2, [()]); unique(filter(list1, { $0 => iskey(map2, $0) })) }
iqsort = { list, cmp => qsort(index(list), { $0, $1 => cmp(list[$0], list[$1]) }) }
isort = { list, cmp => sort(index(list), { $0, $1 => cmp(list[$0], list[$1]) }) }
iter = { p => while(p, { () }); () }
mapgetd = { map, key, default => guard(not(iskey(map, key)), default, { map[key] }) }
modz = { n, d => guard(eq(d, 0), 0, { mod(n, d) }) }
part = { vals, f => group(map(vals, f), vals) }
peek = peekback
peekback = { blst => last(get(blst)) }
peekfront = { blst => first(get(blst)) }
peekq = peekfront
isperm = { a, b => eq(counts(a), counts(b)) }
pfiltern = { list, pred, n => flatten(pmap(chunks(list, n), { $0 => filter($0, pred) })) }
pforn = { list, f, n => pfor(chunks(list, n), { $0 => for($0, f); () }); () }
pmapn = { list, f, n => flatten(pmap(chunks(list, n), { $0 => map($0, f) })) }
pop = popback
popback = { blst => act(blst, { $0 => (drop(-1, $0), last($0)) }) }
popfront = { blst => act(blst, { $0 => (rest($0), first($0)) }) }
popq = popfront
postdec = { b => act(b, { n => (dec(n), n) }) }
postinc = { b => act(b, { n => (inc(n), n) }) }
postput = { b, v => do({ old = get(b); put(b, v); old }) }
postupdate = { b, f => do({ old = get(b); update(b, f); old }) }
ppart = { vals, f => group(pmap(vals, f), vals) }
ppartn = { vals, f, n => group(flatten(pmap(chunks(vals, n), { $0 => map($0, f) })), vals) }
predec = { b => act(b, compose(dec, twin)) }
preinc = { b => act(b, compose(inc, twin)) }
produce = { c, q, n, f => nf = { l => lt(size(l), n) }; while({ get(c) }, { awaits((c, q), { b, l => or(not(b), { nf(l) }) }); when(get(c), { v = f(); while({ and(get(c), { not(tau(q, nf, { $0 => append($0, v) }).0) }) }, { () }); () }); () }); () }
product = { ns => reduce(times, 1, ns) }
push = pushback
pushback = { blst, v => update(blst, { $0 => append($0, v) }); () }
pushfront = { blst, v => update(blst, { $0 => plus([v], $0) }); () }
pushq = pushback
pwheren = { list, pred, n => flatten(pmap(chunks(list, n), { $0 => where($0, pred) })) }
qsort = { list, cmp => subsort = { list, njobs => guard(le(size(list), 1), list, { pivot = list[rand(size(list))]; pivcmp = { val => sign(cmp(val, pivot)) }; parts = plus([-1: [], 0: [], 1: []], part(list, pivcmp)); subn = div(njobs, 2); args = [(parts[-1], subn), (parts[1], subn)]; subs = if(gt(njobs, 1), { pmap(args, subsort) }, { map(args, subsort) }); plus(plus(subs[0], parts[0]), subs[1]) }) }; subsort(list, plus(availprocs(), 2)) }
rangen = { start, extent, n => steps = divz(plus(extent, times(sign(extent), minus(n, 1))), n); map(count(steps), { $0 => plus(start, times($0, n)) }) }
ravel = { list, n => cut(list, countn(size(list), n)) }
react = { b, f => watch(b, { old, new => f(new) }) }
repeat = { n, f => map(rep(n, ()), f) }
reverse = { list => n = size(list); lleach(list, range(minus(n, 1), neg(n))) }
rotate = { list, n => if(ge(n, 0), { plus(take(neg(n), list), drop(neg(n), list)) }, { plus(drop(neg(n), list), take(neg(n), list)) }) }
round = { f => f2i(plus(f, 0.5)) }
run = { b => b() }
runlens = { list => drop(1, eachpair(0, { x, y => minus(y, x) }, append(edges(list), size(list)))) }
runs = { list => cut(list, edges(list)) }
scan_while = { pred, init, f, args => result = evolve_while(compose(last, pred), [init], { as, b => append(as, f(last(as), b)) }, args); drop(1, result) }
snd = { X, Y :: p: (X, Y) => p.1 }
sort = { T :: list: [T], cmp => merge = { a: [T], b: [T] => asize = size(a); bsize = size(b); _403_9 = cycle({ m, i, j => and(lt(i, asize), { lt(j, bsize) }) }, ([], 0, 0), { m, i, j => if(le(cmp(a[i], b[j]), 0), { (append(m, a[i]), plus(i, 1), j) }, { (append(m, b[j]), i, plus(j, 1)) }) }); merged = _403_9.0; asuf = _403_9.1; bsuf = _403_9.2; plus(plus(merged, drop(asuf, a)), drop(bsuf, b)) }; subsort = { list, njobs => lsize = size(list); guard(le(lsize, 1), list, { half = div(lsize, 2); subn = div(njobs, 2); args = [(take(half, list), subn), (drop(half, list), subn)]; subs = if(gt(njobs, 1), { pmap(args, subsort) }, { map(args, subsort) }); merge(subs[0], subs[1]) }) }; subsort(list, availprocs()) }
sq = { i => times(i, i) }
sum = { ns => reduce(plus, 0, ns) }
swap = { s, v => act(s, { lst => (append(drop(-1, lst), v), last(lst)) }) }
switch = { K, V :: sel: K, cases: [K : () -> V] => cases[sel]() }
tail = rest
tas = { b, p, n => do({ o = own(b); (and(p(o), { put(b, n); true }), o) }) }
tast = { bs, p, ns => do({ os = owns(bs); (and(p(os), { puts(bs, ns); true }), os) }) }
tau = { b, p, f => do({ o = own(b); (and(p(o), { put(b, f(o)); true }), o) }) }
taut = { bs, p, f => do({ os = owns(bs); (and(p(os), { puts(bs, f(os)); true }), os) }) }
toggle = { b => update(b, not); () }
trace = { func, init => diff = { accum, x, y => and(ne(x, y), { ne(y, init) }) }; next = { accum, x, y => (append(accum, y), y, func(y)) }; cycle(diff, ([init], init, func(init)), next).0 }
twin = { v => (v, v) }
union = { list1, list2 => unique(plus(list1, list2)) }
vec2i = { vec, radix => sum(map(zip(vec, reverse(index(vec))), { d, p => times(d, pow(radix, p)) })) }
wtau = { b, p, f => await(b, p); tau(b, p, f) }
wtaut = { bs, p, f => awaits(bs, p); taut(bs, p, f) }
*/
