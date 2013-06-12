
import lib.sort;
import unittest;

// insertion_point = { list, val => first_where({ $0_199_31 => le(val, $0_199_31) }, list) }
assert_equals({ 1 }, { insertion_point(1, [0,2,4]) });
assert_equals({ 1 }, { insertion_point(2, [0,2,4]) });
assert_equals({ 2 }, { insertion_point(3, [0,2,4]) });

// iqsort = { lst, cmp => qsort(list:index(lst), { $0_82_27, $1_82_27 => cmp(lst[$0_82_27], lst[$1_82_27]) }) }
assert_equals({ [3, 1, 0, 2] }, { iqsort([3,2,4,1], (-)) });

// isort = { lst, cmp => sort(list:index(lst), { $0_75_27, $1_75_27 => cmp(lst[$0_75_27], lst[$1_75_27]) }) }
assert_equals({ [3, 1, 0, 2] }, { isort([3,2,4,1], (-)) });

// qsort = { lst, cmp => subsort = { lst, njobs => lang:guard(le(list:size(lst), 1), lst, { pivot = lst[math:rand(list:size(lst))]; pivcmp = { val => sign(cmp(val, pivot)) }; parts = plus([-1: [], 0: [], 1: []], list:part(lst, pivcmp)); subn = div(njobs, 2); args = [(parts[-1], subn), (parts[1], subn)]; subs = if(gt(njobs, 1), { pmap(args, subsort) }, { map(args, subsort) }); plus(plus(subs[0], parts[0]), subs[1]) }) }; subsort(lst, plus(availprocs(), 2)) }
assert_equals({ [0, 1, 2, 3, 3, 4] }, { qsort([3,2,1,3,4,0], (-)) });

// sort = { lst, cmp => merge = { a, b => asize = list:size(a); bsize = list:size(b); _43_9 = loop:cycle(([], 0, 0), { m, i, j => and(lt(i, asize), { lt(j, bsize) }) }, { m, i, j => lang:if(le(cmp(a[i], b[j]), 0), { (list:append(m, a[i]), plus(i, 1), j) }, { (list:append(m, b[j]), i, plus(j, 1)) }) }); merged = _43_9.0; asuf = _43_9.1; bsuf = _43_9.2; plus(plus(merged, list:drop(asuf, a)), list:drop(bsuf, b)) }; subsort = { lst, njobs => lsize = list:size(lst); lang:guard(le(lsize, 1), lst, { half = div(lsize, 2); subn = div(njobs, 2); args = [(list:take(half, lst), subn), (list:drop(half, lst), subn)]; subs = if(gt(njobs, 1), { pmap(args, subsort) }, { map(args, subsort) }); merge(subs[0], subs[1]) }) }; subsort(lst, availprocs()) }
assert_equals({ [0, 1, 2, 3, 4, 6] }, { sort([4,1,2,3,6,0], (-)) });
assert_equals({ [6, 4, 3, 2, 1, 0] }, { sort([4,1,2,3,6,0], minus $ neg) });


