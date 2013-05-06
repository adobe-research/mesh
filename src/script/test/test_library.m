
import * from std;
import * from unittest;

// abs = { n => guard(ge(n, 0), n, { neg(n) }) }
assert_equals({ 1 }, { abs(-1) });
assert_equals({ 1 }, { abs(1) });
assert_equals({ 0 }, { abs(0) });
assert_equals({ 0 }, { abs(-0) });

// act = <s, r> { b: *s, f: s -> (s, r) => do({ _94_9 = f(own(b)); next = _94_9.0; result = _94_9.1; put(b, next); result }) }
// first part of tuple is the new value for the box and the other value is the value returned
assert_equals({ 2 }, { x = box(2); act(x, {(inc($0), $0)}) });
assert_equals({ 3 }, { x = box(2); act(x, {(inc($0), $0)}); *x });

// all = { vals, pred => evolve_while(id, true, compose(snd, pred), vals) }
assert_equals({ true }, { all([3,2,4,3], { lt(1, $0) }) });
assert_equals({ false }, { all([3,2,4,3], { gt(1, $0) }) });

// any = { vals, pred => evolve_while(not, false, compose(snd, pred), vals) }
assert_equals({ true }, { any([1,2,1], { eq(2, $0) }) });
assert_equals({ false }, { any([3,2,4,3], { eq(1, $0) }) });

// apply = { f, x => f(x) }
assert_equals({ 4 }, { apply(inc, 3) });

// async = { f, cb => spawn({ cb(f()) }); () }

// avg = { lst => divz(sum(lst), list:size(lst)) }
assert_equals({ 5 }, { avg([3,3,3,11]) });

// bench = { b => t0 = millitime(); result = b(); (#result: result, #time: integer:l2i(lang:lminus(millitime(), t0))) }
assert_equals({ true }, {
                        res = bench({ sleep(15); inc(2) });
                        eq(3, res.#result);
                        });

// benchn = { n, f => timer = { start = millitime(); f(); integer:l2i(lang:lminus(millitime(), start)) }; math:avg(list:repeat(n, timer)) }
assert_equals({ true }, {
                        res = benchn(3, { sleep(15) });
                        gt(res, 1);
                        });

// cas = { b, o, n => do({ and(eq(own(b), o), { put(b, n); true }) }) }
assert_equals({ true }, { x = box(1); cas(x, 1, 3) });
assert_equals({ 3 }, { x = box(1); cas(x, 1, 3); *x });
assert_equals({ false }, { x = box(1); cas(x, 2, 3) });
assert_equals({ 1 }, { x = box(1); cas(x, 2, 3); *x });

// cast = { bs, os, ns => do({ and(eq(owns(bs), os), { puts(bs, ns); true }) }) }
assert_equals({ true }, { x = box(3); y = box(6); cast((x,y), (3,6), (4,7)) });
assert_equals({ (4, 7) }, { x = box(3); y = box(6); cast((x,y), (3,6), (4,7)); (*x, *y) });
assert_equals({ false }, { x = box(3); y = box(6); cast((x,y), (1,1), (4,7)) });
assert_equals({ (3, 6) }, { x = box(3); y = box(6); cast((x,y), (1,1), (4,7)); (*x, *y) });

// cau = { b, o, f => do({ and(eq(own(b), o), { put(b, f(o)); true }) }) }
assert_equals({ true }, { x = box(1); cau(x, 1, inc) });
assert_equals({ 2 }, { x = box(1); cau(x, 1, inc); *x });
assert_equals({ false }, { x = box(1); cau(x, 2, inc) });
assert_equals({ 1 }, { x = box(1); cau(x, 2, inc); *x });

// caut = { bs, os, f => do({ and(eq(owns(bs), os), { puts(bs, f(os)); true }) }) }
assert_equals({ true }, { x = box(3); y = box(6); caut((x,y), (3,6), {a,b=> (inc(a), inc(b))}) });
assert_equals({ (4, 7) }, { x = box(3); y = box(6); caut((x,y), (3,6), {a,b=> (inc(a), inc(b))}); (*x, *y) });
assert_equals({ false }, { x = box(3); y = box(6); caut((x,y), (1,1), {a,b=> (inc(a), inc(b))}) });
assert_equals({ (3, 6) }, { x = box(3); y = box(6); caut((x,y), (1,1), {a,b=> (inc(a), inc(b))}); (*x, *y) });

// chunks = <T> { (list: [T], nchunks: Int) -> [[T]] => cut(list, cutpoints(list, nchunks)) }assert_equals({ [[]] }, { cutpoints(count(5), 0) });
assert_equals({ [] }, { chunks(count(5), 0) });
assert_equals({ [[0, 1, 2, 3, 4]] }, { chunks(count(5), 1) });
assert_equals({ [[0, 1], [2, 3, 4]] }, { chunks(count(5), 2) });
assert_equals({ [[0], [1, 2], [3, 4]] }, { chunks(count(5), 3) });
assert_equals({ [[0], [1], [2], [3, 4]] }, { chunks(count(5), 4) });
assert_equals({ [[0], [1], [2], [3], [4]] }, { chunks(count(5), 5) });

// compose = { f, g => { x => g(f(x)) } }
assert_equals({ 4 }, { compose(inc, inc)(2); });

// constrain = { lo, n, hi => max(lo, min(hi, n)) }
assert_equals({ 2 }, { constrain(2, 1, 4) });
assert_equals({ 3 }, { constrain(2, 3, 4) });
assert_equals({ 4 }, { constrain(2, 5, 4) });

// consume = { c, q, f => ne = { l => gt(list:size(l), 0) }; while({ get(c) }, { mutate:awaits((c, q), { b, l => or(not(b), { ne(l) }) }); loop:when(get(c), { _118_13 = mutate:tau(q, ne, list:tail); b = _118_13.0; l = _118_13.1; loop:when(b, { f(list:head(l)) }); () }); () }); () }

// contains = { list, item => lt(find(list, item), size(list)) }
assert_equals({ false }, { contains([3,4,5], 2) });
assert_equals({ true }, { contains([3,4,5], 4) });
assert_equals({ false }, { contains(["cat", "dog", "fish"], "mouse") });
assert_equals({ true }, { contains(["cat", "dog", "fish"], "fish") });

// converge = { func, init => diff = { x, y => and(ne(x, y), { ne(y, init) }) }; next = { x, y => (y, func(y)) }; cycle(diff, (init, func(init)), next).0 }
assert_equals({ 9 }, { converge({inc($0) % 10}, 0) });

// counts = { list => inckey = { map, key => mapset(map, key, plus(1, mapgetd(map, key, 0))) }; loop:reduce(inckey, [:], list) }
assert_equals({ [1: 2, 2: 1, 3: 3] }, { counts([1,2,3,1,3,3]) });

// cross = { xs, ys => xn = size(xs); ixs = count(times(xn, size(ys))); zip(mapll(map(ixs, { $0_47_21 => mod($0_47_21, xn) }), xs), mapll(map(ixs, { $0_47_51 => div($0_47_51, xn) }), ys)) }
assert_equals({ [(1, 1), (2, 1), (3, 1)] }, { cross([1,2,3], [1]) });
assert_equals({ [] }, { cross([1,2,3], []) });

// cutpoints = <T> { (list: [T], ncuts: Int) -> [Int] => s = size(list); n = constrain(0, ncuts, s); c = divz(s, n); r = modz(s, n); sizes = plus(rep(minus(n, r), c), rep(r, plus(c, 1))); starts(sizes) }
assert_equals({ [] }, { cutpoints(count(5), 0) });
assert_equals({ [0] }, { cutpoints(count(5), 1) });
assert_equals({ [0, 2] }, { cutpoints(count(5), 2) });
assert_equals({ [0, 1, 3] }, { cutpoints(count(5), 3) });
assert_equals({ [0, 1, 2, 3] }, { cutpoints(count(5), 4) });
assert_equals({ [0, 1, 2, 3, 4] }, { cutpoints(count(5), 5) });

// dec = { n => minus(n, 1) }
assert_equals({ -1 }, { dec(0) });
assert_equals({ 0 }, { dec(1) });

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


// difference = { list1, list2 => map2 = mapns:assoc(list2, [()]); list:unique(list:filter(list1, { $0_30_36 => not(mapns:iskey(map2, $0_30_36)) })) }
assert_equals({ [2] }, { difference([2,3,4], [3,4,5]) });

// divz = { n, d => guard(eq(d, 0), 0, { div(n, d) }) }
assert_equals({ 2 }, { divz(4, 2) });
assert_equals({ 0 }, { divz(0, 2) });
assert_equals({ 0 }, { divz(3, 0) });

// eachpair = { init, f, args => list = plus([init], args); map(index(args), { i => f(list[i], list[plus(i, 1)]) }) }
assert_equals({ [false, false, true, false, false] }, { eachpair(0, gt, [1,3,2,4,5]) });

// edges = { list => plus([0], filter(drop(1, index(list)), { $0_92_40 => ne(list[minus($0_92_40, 1)], list[$0_92_40]) })) }
assert_equals({ [0, 1, 4] }, { edges([3,2,2,2,8,8]) });

// enlist = { v => [v] }
assert_equals({ [2] }, { enlist(2) });

// evolve = { init, f, list => reduce(f, init, list) }
assert_equals({ 6 }, { evolve(0, { a, b => a+b  }, [1,2,3]) });

// fabs = { f => guard(fge(f, 0.0), f, { fminus(0.0, f) }) }
assert_equals({ 1.0 }, { fabs(-1.0) });
assert_equals({ 1.0 }, { fabs(1.0) });
assert_equals({ 0.0 }, { fabs(0.0) });
assert_equals({ 0.0 }, { fabs(-0.0) });

// fan = { f, g => { $0_30_13 => (f($0_30_13), g($0_30_13)) } }
assert_equals({ (4,2) }, { x = fan(inc, dec); x(3) });

// favg = { lst => fdivz(fsum(lst), integer:i2f(list:size(lst))) }
assert_equals({ 3.0 }, { favg([2.0,3.0,4.0]) });

// fdivz = { n, d => guard(eq(d, 0.0), 0.0, { fdiv(n, d) }) }
assert_equals({ 2.0 }, { fdivz(4.0, 2.0) });
assert_equals({ 0.0 }, { fdivz(0.0, 2.0) });
assert_equals({ 0.0 }, { fdivz(3.0, 0.0) });

// filet = { list, n => s = size(list); cut(list, eachleft(times)(count(plus(divz(s, n), sign(modz(s, n)))), n)) }
assert_equals({ [[0, 1], [2, 3], [4, 5], [6, 7], [8, 9]] }, { filet(count(10), 2) });
assert_equals({ [[0, 1, 2, 3], [4]] }, { filet(count(5), 4) });
assert_equals({ [[0, 1, 2, 3, 4]]}, { filet(count(5), 5) });
assert_equals({ [[0], [1], [2]] }, { filet(count(3), -1) });
assert_equals({ [[0, 1, 2, 3, 4]]}, { filet(count(5), 6) });
assert_equals({ [] }, { filet(count(3), 0) });

// finrange = { f, fbase, fextent => and(fge(f, fbase), { flt(f, plus(fbase, fextent)) }) }
assert_equals({ true }, { finrange(4.0, 2.0, 5.0) });
assert_equals({ true }, { finrange(2.0, 2.0, 1.0) });
assert_equals({ false }, { finrange(1.0, 2.0, 1.0) });
assert_equals({ false }, { finrange(3.0, 2.0, 1.0) });

// finspt = { list, val => firstwhere({ $0_200_32 => fle(val, $0_200_32) }, list) }
assert_equals({ 1 }, { finspt([1.0,2.2,3.1], 2.0) });
assert_equals({ 1 }, { finspt([1.0,2.2,3.1], 2.2) });
assert_equals({ 2 }, { finspt([1.0,2.2,3.1], 2.3) });

// firstwhere = { pred, vals => n = list:size(vals); cycle({ i => and(lt(i, n), { not(pred(vals[i])) }) }, 0, inc) }
assert_equals({ 4 }, { firstwhere({ 3 < $0 }, [0,1,2,3,4,5]) });

// fmax = { x, y => iif(fge(x, y), x, y) }
assert_equals({ 1.0 }, { fmax(1.0, 1.0) });
assert_equals({ 1.0 }, { fmax(1.0, -1.0) });
assert_equals({ 1.0 }, { fmax(-0.0, 1.0) });
assert_equals({ 0.0 }, { fmax(0.0, -0.0) });

// fmin = { x, y => iif(fge(x, y), y, x) }
assert_equals({ 1.0 }, { fmin(1.0, 1.0) });
assert_equals({ -1.0 }, { fmin(1.0, -1.0) });
assert_equals({ -0.0 }, { fmin(-0.0, 1.0) });
assert_equals({ -0.0 }, { fmin(0.0, -0.0) });

// fmodz = { n, d => guard(eq(d, 0.0), 0.0, { fmod(n, d) }) }
assert_equals({ 0.125 }, { fmodz(3.125, 1.0) });
assert_equals({ 0.0 }, { fmodz(3.125, 0.0) });

// fproduct = { ns => loop:reduce(ftimes, 1.0, ns) }
assert_equals({ 24.0 }, { fproduct([2.0,3.0,4.0]) });

// fruntot = { (fs: [Double]) -> [Double] => drop(1, scan(plus, 0.0, fs)) }
assert_equals({ [1.0, 3.0, 6.0] }, { fruntot([1.0, 2.0, 3.0]) });

// fsign = { f => guard(fgt(f, 0.0), 1, { iif(flt(f, 0.0), -1, 0) }) }
assert_equals({ 1 }, { fsign(1.0) });
assert_equals({ -1 }, { fsign(-1.0) });
assert_equals({ 0 }, { fsign(-0.0) });
assert_equals({ 0 }, { fsign(0.0) });

// fsq = { f => ftimes(f, f) }
assert_equals({ 9.0 }, { fsq(3.0) });

// fst = <A, B> { p: (A, B) => p.0 }
assert_equals({ "a" }, { fst(("a","b")) });
assert_equals({ 1 }, { fst((1,2)) });

// fsum = { ns => loop:reduce(plus, 0.0, ns) }
assert_equals({ 6.0 }, { fsum([1.0, 2.0, 3.0]) });

// fuse = { f, g => { $0_39_14, $1_39_14 => (f($0_39_14), g($1_39_14)) } }
assert_equals({ (4, 4) }, { x = fuse(inc, dec); x(3, 5); });
assert_equals({ (4, "a") }, { x = fuse(inc, id); x(3, "a"); });

// id = { v => v }
assert_equals({ "a" }, { id("a") });
assert_equals({ 3 }, { id(3) });

// inc = { n => plus(n, 1) }
assert_equals({ 0 }, { inc(-1) });
assert_equals({ 2 }, { inc(1) });


// index = { list => count(size(list)) }
assert_equals({ [0,1,2] }, { index(["a", "b", "c"]) });

// inrange = { x, base, extent => and(ge(x, base), { lt(x, plus(base, extent)) }) }
assert_equals({ true }, { inrange(4, 2, 5) });
assert_equals({ true }, { inrange(2, 2, 1) });
assert_equals({ false }, { inrange(1, 2, 1) });
assert_equals({ false }, { inrange(3, 2, 1) });

// inspt = { list, val => firstwhere({ $0_199_31 => le(val, $0_199_31) }, list) }
assert_equals({ 1 }, { inspt([0,2,4], 1) });
assert_equals({ 1 }, { inspt([0,2,4], 2) });
assert_equals({ 2 }, { inspt([0,2,4], 3) });

// instr = { f => num_recs = lang:box(0); avg_time = lang:box(0.0); wrapped_f = { x => t0 = nanotime(); y = f(x); mutate:updates((num_recs, avg_time), { num, avg => elapsed = lang:lminus(nanotime(), t0); new_num = plus(num, 1); new_avg = fdiv(plus(ftimes(avg, integer:l2f(num)), integer:l2f(elapsed)), integer:l2f(new_num)); (new_num, new_avg) }); y }; (wrapped_f, (#num: num_recs, #avg: avg_time)) }
// intersection = { list1, list2 => map2 = mapns:assoc(list2, [()]); list:unique(list:filter(list1, { $0_16_36 => mapns:iskey(map2, $0_16_36) })) }
assert_equals({ [2, 4] }, { intersection([2,2,3,4], [5,4,4,2,1]) });

// iqsort = { lst, cmp => qsort(list:index(lst), { $0_82_27, $1_82_27 => cmp(lst[$0_82_27], lst[$1_82_27]) }) }
assert_equals({ [3, 1, 0, 2] }, { iqsort([3,2,4,1], (-)) });

// isort = { lst, cmp => sort(list:index(lst), { $0_75_27, $1_75_27 => cmp(lst[$0_75_27], lst[$1_75_27]) }) }
assert_equals({ [3, 1, 0, 2] }, { isort([3,2,4,1], (-)) });

// isperm = { a, b => eq(mapns:counts(a), mapns:counts(b)) }
assert_equals({ true }, { isperm([1,2,3], [3,2,1]) });
assert_equals({ false }, { isperm([1,2,3], [3,3,1]) });
assert_equals({ true }, { isperm(["a", "b", "c"], ["b", "c", "a"]) });
assert_equals({ false }, { isperm(["a", "b", "c"], ["B", "c", "a"]) });

// iter = { p => while(p, { () }); () }

// mapgetd = { map, key, default => guard(not(iskey(map, key)), default, { map[key] }) }
assert_equals({ "charlie" }, { mapgetd(["a":"alpha", "b":"bee"], "c", "charlie") });
assert_equals({ "bee" }, { mapgetd(["a":"alpha", "b":"bee"], "b", "beta") });

// modz = { n, d => guard(eq(d, 0), 0, { mod(n, d) }) }
assert_equals({ 1 }, { modz(4, 3) });
assert_equals({ 0 }, { modz(4, 0) });

// part = { vals, f => group(map(vals, f), vals) }
assert_equals({ [true: [1, 2], false: [3, 4, 5]] }, { part([1,2,3,4,5], { gt(3, $0) }); });

// peekback = { blst => list:last(get(blst)) }
assert_equals({ 3 }, { peekback(box([1,2,3])) });

// peekfront = { blst => list:first(get(blst)) }
assert_equals({ 1 }, { peekfront(box([1,2,3])) });

// pfiltern = { lst, pred, n => list:flatten(pmap(chunks(lst, n), { $0_245_36 => list:filter($0_245_36, pred) })) }
assert_equals({ [0, 2, 4, 6, 8, 10, 12, 14, 16, 18] }, { pfiltern(count(20), {($0 % 2) == 0}, 4) });
assert_equals({ filter(count(20), {($0 % 2) == 0}) }, { pfiltern(count(20), {($0 % 2) == 0}, 4) });

// pforn = { lst, f, n => lang:pfor(chunks(lst, n), { $0_235_31 => list:for($0_235_31, f); () }); () }
assert_equals({ 45 }, {
        for_data = box(0);
        pforn(count(10), { for_data <- {$$0 + $0} }, 4);
        *for_data
        });

assert_equals({
        for_data = box(0);
        for(count(10), { for_data <- {$$0 + $0} });
        *for_data
        }, {
        for_data = box(0);
        pforn(count(10), { for_data <- {$$0 + $0} }, 4);
        *for_data
        });

assert_equals({
        for_data = box(0);
        pfor(count(10), { for_data <- {$$0 + $0} });
        *for_data
        }, {
        for_data = box(0);
        pforn(count(10), { for_data <- {$$0 + $0} }, 4);
        *for_data
        });


// pmapn = { lst, f, n => list:flatten(pmap(chunks(lst, n), { $0_227_36 => map($0_227_36, f) })) }
assert_equals({ [1,2,3,4,5] }, { pmapn(count(5), inc, 2) });
assert_equals({ map(count(5), inc) }, { pmapn(count(5), inc, 2) });
assert_equals({ pmap(count(5), inc) }, { pmapn(count(5), inc, 2) });

// popback = { blst => act(blst, { $0_249_27 => (list:drop(-1, $0_249_27), list:last($0_249_27)) }) }
assert_equals({ 4 }, { x = box([1,2,3,4]); popback(x) });
assert_equals({ [1,2,3] }, { x = box([1,2,3,4]); popback(x); *x }); // make sure that the box is modified

// popfront = { blst => act(blst, { $0_248_28 => (list:rest($0_248_28), list:first($0_248_28)) }) }
assert_equals({ 1 }, { x = box([1,2,3,4]); popfront(x) });
assert_equals({ [2,3,4] }, { x = box([1,2,3,4]); popfront(x); *x }); // make sure that the box is modified

// postdec = { b => postupdate(b, dec) }
assert_equals({ (2, 2, 1) }, { x = box(2); (*x, postdec(x), *x) });

// postinc = { b => postupdate(b, inc) }
assert_equals({ (2, 2, 3) }, { x = box(2); (*x, postinc(x), *x) });


// postput = { b, v => act(b, { $0_103_24 => (v, $0_103_24) }) }
assert_equals({ 2 }, { x = box(2); postput(x, 12) });
assert_equals({ 12 }, { x = box(2); postput(x, 12); *x; });

// postupdate = { b, f => act(b, { $0_108_27 => (f($0_108_27), $0_108_27) }) }
assert_equals({ 2 }, { x = box(2); postupdate(x, inc) });
assert_equals({ 3 }, { x = box(2); postupdate(x, inc); *x; });

// ppart = { vals, f => list:group(pmap(vals, f), vals) }
assert_equals({ [true: [1, 2], false: [3, 4, 5]] }, { ppart([1,2,3,4,5], { gt(3, $0) }); });
assert_equals({ part([1,2,3,4,5], { gt(3, $0) }) }, { ppart([1,2,3,4,5], { gt(3, $0) }); });

// ppartn = { vals, f, n => list:group(list:flatten(pmap(chunks(vals, n), { $0_266_48 => map($0_266_48, f) })), vals) }
assert_equals({ [true: [1, 2], false: [3, 4, 5]] }, { ppartn([1,2,3,4,5], { gt(3, $0) }, 2); });
assert_equals({ part([1,2,3,4,5], { gt(3, $0) }) }, { ppartn([1,2,3,4,5], { gt(3, $0) }, 2); });
assert_equals({ ppart([1,2,3,4,5], { gt(3, $0) }) }, { ppartn([1,2,3,4,5], { gt(3, $0) }, 2); });

// predec = { b => act(b, compose(dec, twin)) }
assert_equals({ (2, 1, 1) }, { x = box(2); (*x, predec(x), *x) });

// preinc = { b => act(b, compose(inc, twin)) }
assert_equals({ (2, 3, 3) }, { x = box(2); (*x, preinc(x), *x) });

// produce = { c, q, n, f => nf = { l => lt(list:size(l), n) }; while({ get(c) }, { mutate:awaits((c, q), { b, l => or(not(b), { nf(l) }) }); loop:when(get(c), { v = f(); while({ and(get(c), { not(mutate:tau(q, nf, { $0_90_36 => list:append($0_90_36, v) }).0) }) }, { () }); () }); () }); () }

// product = { ns => loop:reduce(times, 1, ns) }
assert_equals({ 24 }, { product([2,3,4]) });

// pushback = { blst, v => update(blst, { $0_246_29 => list:append($0_246_29, v) }); () }
assert_equals({ [1,2,3,4] }, { x = box([1,2,3]); pushback(x, 4); *x });

// pushfront = { blst, v => update(blst, { $0_245_30 => plus([v], $0_245_30) }); () }
assert_equals({ [4,1,2,3] }, { x = box([1,2,3]); pushfront(x, 4); *x });

// pwheren = { lst, pred, n => list:flatten(pmap(chunks(lst, n), { $0_253_36 => list:where($0_253_36, pred) })) }
assert_equals({ [0, 1, 3] }, { pwheren([4,4,7,4,7], {$0 < 5}, 3) });
assert_equals({ where([4,4,7,4,7], {$0 < 5}) }, { pwheren([4,4,7,4,7], {$0 < 5}, 3) });

// qsort = { lst, cmp => subsort = { lst, njobs => lang:guard(le(list:size(lst), 1), lst, { pivot = lst[math:rand(list:size(lst))]; pivcmp = { val => sign(cmp(val, pivot)) }; parts = plus([-1: [], 0: [], 1: []], list:part(lst, pivcmp)); subn = div(njobs, 2); args = [(parts[-1], subn), (parts[1], subn)]; subs = if(gt(njobs, 1), { pmap(args, subsort) }, { map(args, subsort) }); plus(plus(subs[0], parts[0]), subs[1]) }) }; subsort(lst, plus(availprocs(), 2)) }
assert_equals({ [0, 1, 2, 3, 3, 4] }, { qsort([3,2,1,3,4,0], (-)) });

// react = { b, f => watch(b, { old, new => f(new) }) }
assert_equals({ 2 }, { 
                    status = box(0);
                    stat(new) { put(status, new) };
                    f = box(0);
                    w = react(f, stat);
                    f <- inc;
                    f <- inc;
                    unwatch(f, w);
                    *status;
                     });

// repeat = { n, f => map(lang:rep(n, ()), f) }
assert_equals({ [2, 2, 2] }, { repeat(3, { 2 }) });

// reverse = { list => n = size(list); mapll(range(minus(n, 1), neg(n)), list) }
assert_equals({ [3, 2, 1, 0] }, { reverse(count(4)) });
assert_equals({ [-1, -2, -3, -4] }, { reverse([-4,-3,-2,-1]) });
assert_equals({ [2, 3, 1, 4] }, { reverse([4,1,3,2]) });

// rotate = { list, n => if(ge(n, 0), { plus(take(neg(n), list), drop(neg(n), list)) }, { plus(drop(neg(n), list), take(neg(n), list)) }) }
assert_equals({ [4, 0, 1, 2, 3] }, { rotate(count(5), 1) });
assert_equals({ [1, 2, 3, 4, 0] }, { rotate(count(5), -1) });
assert_equals({ [0, 1, 2, 3, 4] }, { rotate(count(5), 0) });

// round = { f => f2i(plus(f, 0.5)) }
assert_equals({ 1 }, { round(1.49) });
assert_equals({ 2 }, { round(1.50) });
assert_equals({ 0 }, { round(0.0) });
assert_equals({ 0 }, { round(-0.49) });
assert_equals({ -1 }, { round(-0.51) });

// run = { b => b() }
assert_equals({ 3 }, { run({3}) });

// runlens = { list => drop(1, eachpair(0, { x, y => minus(y, x) }, append(edges(list), size(list)))) }
assert_equals({ [2, 1, 3] }, { runlens([1,1,4,2,2,2]) });

// runs = { list => cut(list, edges(list)) }
assert_equals({ [[1, 1], [4], [2, 2, 2]] }, { runs([1,1,4,2,2,2]) });

// runtot = { (is: [Int]) -> [Int] => drop(1, scan(plus, 0, is)) }
assert_equals({ [1, 3, 6] }, { runtot([1, 2, 3]) });

// scan_while = { pred, init, f, args => result = evolve_while(compose(list:last, pred), [init], { as, b => list:append(as, f(list:last(as), b)) }, args); list:drop(1, result) }
assert_equals({ [2, 3, 4] }, { scan_while({$0 < 4}, 1, (+), [1,1,1,1,1]) });

// snd = <A, B> { p: (A, B) => p.1 }
assert_equals({ "b" }, { snd(("a","b")) });
assert_equals({ 2 }, { snd((1,2)) });

// sort = { lst, cmp => merge = { a, b => asize = list:size(a); bsize = list:size(b); _43_9 = loop:cycle({ m, i, j => and(lt(i, asize), { lt(j, bsize) }) }, ([], 0, 0), { m, i, j => lang:if(le(cmp(a[i], b[j]), 0), { (list:append(m, a[i]), plus(i, 1), j) }, { (list:append(m, b[j]), i, plus(j, 1)) }) }); merged = _43_9.0; asuf = _43_9.1; bsuf = _43_9.2; plus(plus(merged, list:drop(asuf, a)), list:drop(bsuf, b)) }; subsort = { lst, njobs => lsize = list:size(lst); lang:guard(le(lsize, 1), lst, { half = div(lsize, 2); subn = div(njobs, 2); args = [(list:take(half, lst), subn), (list:drop(half, lst), subn)]; subs = if(gt(njobs, 1), { pmap(args, subsort) }, { map(args, subsort) }); merge(subs[0], subs[1]) }) }; subsort(lst, availprocs()) }
assert_equals({ [0, 1, 2, 3, 4, 6] }, { sort([4,1,2,3,6,0], (-)) });
assert_equals({ [6, 4, 3, 2, 1, 0] }, { sort([4,1,2,3,6,0], minus $ neg) });

// sq = { i => times(i, i) }
assert_equals({ 4 }, { sq(2) });

// starts = { (sizes: [Int]) -> [Int] => drop(-1, scan(plus, 0, sizes)) }
assert_equals({ [0, 1, 3] }, { starts([1, 2, 3]) });
assert_equals({ [0, 1, 1] }, { starts([1, 0, 2]) });
assert_equals({ [] }, { starts([]) });

// sum = { ns => loop:reduce(plus, 0, ns) }
assert_equals({ 6 }, { sum([1,2,3]) });

// swap = { s, v => act(s, { lst => (list:append(list:drop(-1, lst), v), list:last(lst)) }) }
assert_equals({ 3 }, { x = box([1,2,3]); swap(x, 2); });
assert_equals({ [1,2,2] }, { x = box([1,2,3]); swap(x, 2); *x });

// switch = <K, V> { sel: K, cases: [K : () -> V] => cases[sel]() }
assert_equals({ "0" }, { switch(0, [ 0:{ "0" }, 1:{ "1" }]) });

// tas = { b, p, n => do({ o = own(b); (and(p(o), { put(b, n); true }), o) }) }
assert_equals({ (true, 2) }, { x = box(2); tas(x, { $0 > 1 }, 3) });
assert_equals({ 3 }, { x = box(2); tas(x, { $0 > 1 }, 3); *x });
assert_equals({ (false, 1) }, { x = box(1); tas(x, { $0 > 1 }, 3) });
assert_equals({ 1 }, { x = box(1); tas(x, { $0 > 1 }, 3); *x });

// tast = { bs, p, ns => do({ os = owns(bs); (and(p(os), { puts(bs, ns); true }), os) }) }
assert_equals({ (true, (2, 3)) }, { x = box(2); y = box(3); tast((x, y), { a:(Int, Int) => a.0 > 1 && {a.1 > 2} }, (7, 8)) });
assert_equals({ (7, 8) }, { x = box(2); y = box(3); tast((x, y), { a:(Int, Int) => a.0 > 1 && {a.1 > 2} }, (7, 8)); (*x, *y) });
assert_equals({ (false, (2, 3)) }, { x = box(2); y = box(3); tast((x, y), { a:(Int, Int) => a.0 < 1 && {a.1 > 2} }, (7, 8)) });
assert_equals({ (2, 3) }, { x = box(2); y = box(3); tast((x, y), { a:(Int, Int) => a.0 < 1 && {a.1 > 2} }, (7, 8)); (*x, *y) });

// tau = { b, p, f => do({ o = own(b); (and(p(o), { put(b, f(o)); true }), o) }) }
assert_equals({ (true, 2) }, { x = box(2); tau(x, { $0 > 1 }, inc) });
assert_equals({ 3 }, { x = box(2); tau(x, { $0 > 1 }, inc); *x });
assert_equals({ (false, 1) }, { x = box(1); tau(x, { $0 > 1 }, inc) });
assert_equals({ 1 }, { x = box(1); tau(x, { $0 > 1 }, inc); *x });

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


// tconverge = { func, init => diff = { accum, x, y => and(ne(x, y), { ne(y, init) }) }; next = { accum, x, y => (list:append(accum, y), y, func(y)) }; cycle(diff, ([init], init, func(init)), next).0 }
assert_equals({ [0, 1, 2, 3, 4, 5, 6, 7, 8, 9] }, { tconverge({inc($0) % 10}, 0) });

// trace = { p, v, f => cycle(compose(last, p), [v], { $0_43_26 => append($0_43_26, f(last($0_43_26))) }) }
assert_equals({ [0, 1, 2, 3, 4] }, { trace({ $0 < 4 }, 0, inc) });

// tracen = { n, v, f => cyclen(n, [v], { $0_57_20 => append($0_57_20, f(last($0_57_20))) }) }
assert_equals({ [0, 1, 2, 3, 4] }, { tracen(4, 0, inc) });

// twin = { v => (v, v) }
assert_equals({ (1, 1) }, { twin(1) });

// union = { list1, list2 => list:unique(plus(list1, list2)) }
assert_equals({ [0, 1, 2, 3, 4] }, { union([1,1,2,3], [0,2,4]) });

// vec2i = { vec, radix => sum(map(list:zip(vec, list:reverse(list:index(vec))), { d, p => times(d, pow(radix, p)) })) }
assert_equals({ 1024 }, { vec2i([1, 0, 2, 4], 10) });
assert_equals({ 1024 }, { vec2i([2, 0, 0, 0], 8) });

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

