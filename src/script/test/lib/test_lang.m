
import unittest;


// Type testing
// ChainedLists
assert_equals({ [] }, { flatten([]) }); // create an empty list
assert_equals({ [1, 2, 3] }, { flatten([[1, 2, 3]]) }); // create a list from a single list
assert_equals({ [1, 2, 3, 4] }, { flatten([[1,2], [], [3,4]]) }); // need to skip the empty list


// args : () -> [String] = <intrinsic>
assert_equals({ [] }, { args() });
assert_equals({ [] }, { apply(args, ()) });

// getenv : (String) -> String = <intrinsic>
assert_equals({ "" }, { getenv("NoSuchValue") });
assert_equals({ "" }, { apply(getenv, ("NoSuchValue")) });
assert_false({ "" == getenv("PATH") });


// guard : (T => (Bool, T, () -> T) -> T) = <intrinsic>
assert_equals({ 0 }, {
        guard_data = box(0);
        guard(true, *guard_data, { guard_data <- inc; *guard_data });
        });
assert_equals({ 1 }, {
        guard_data = box(0);
        guard(false, *guard_data, { guard_data <- inc; *guard_data });
        });
assert_equals({ 0 }, {
        guard_data = box(0);
        apply(guard, (true, *guard_data, { guard_data <- inc; *guard_data }));
        });


// if : (T => (Bool, () -> T, () -> T) -> T) = <intrinsic>
assert_equals({ "true" }, { if(true, { "true" }, { "false" }) });
assert_equals({ "true" }, { if(true, { 1+1; "true" }, { "false" }) });
assert_equals({ "false" }, { if(false, { "true" }, { "false" }) });
assert_equals({ "false" }, { if(false, { "true" }, { 1+1; "false" }) });
assert_equals({ "true" }, { apply(if, (true, { "true" }, { "false" })) });


// ifelse = <T> { (cases: [(() -> Bool, () -> T)], default: () -> T) -> T => cascade(cases, default)() }
assert_equals({ ["neg", "little", "med", "big"] }, {
    [-1, 0, 10, 100] | { x =>
        ifelse([
            ({x < 0}, {"neg"}),
            ({x < 10}, {"little"}),
            ({x < 100}, {"med"})
        ], {"big"})
    }
});


// iif : (T => (Bool, T, T) -> T) = <intrinsic>
assert_equals({ "true" }, { iif(true, "true", "false") });
assert_equals({ "false" }, { iif(false, "true", "false") });
assert_equals({ "true" }, { apply(iif, (true, "true", "false")) });

// switch = <K, V> { sel: K, cases: [K : () -> V] => cases[sel]() }
assert_equals({ "0" }, { switch(0, [ 0:{ "0" }, 1:{ "1" }]) });

// when : (T => (Bool, () -> T) -> ()) = <intrinsic>
assert_equals({ 1 }, {
        when_data = box(0);
        when(true, {when_data <- inc});
        *when_data;
        });
assert_equals({ 0 }, {
        when_data = box(0);
        when(false, {when_data <- inc});
        *when_data;
        });

assert_equals({ 1 }, {
        when_data = box(0);
        apply(when, (true, {when_data <- inc}));
        *when_data;
        });

// converge = { func, init => diff = { x, y => and(ne(x, y), { ne(y, init) }) }; next = { x, y => (y, func(y)) }; cycle((init, diff, func(init)), next).0 }
assert_equals({ 9 }, { converge({inc($0) % 10}, 0) });


// cycle : (T => (T, T -> Bool, T -> T) -> T) = <intrinsic>
assert_equals({ 4 }, { cycle(0, { $0 < 4 }, inc) });
assert_equals({ 4 }, { apply(cycle, (0, { $0 < 4 }, inc)) });

// cyclen : (T => (T, Int, T -> T) -> T) = <intrinsic>
assert_equals({ 4 }, { cyclen(0, 4, inc) });
assert_equals({ 4 }, { apply(cyclen, (0, 4, inc)) });

// evolve = { init, f, list => reduce(f, init, list) }
assert_equals({ 6 }, { evolve(0, { a, b => a+b  }, [1,2,3]) });

// evolve_while : (A, B => (A -> Bool, A, (A, B) -> A, [B]) -> A) = <intrinsic>
assert_equals({ 4 }, { evolve_while({$0 < 4}, 1, (+), [1,1,1,1,1]) });
assert_equals({ 4 }, { apply(evolve_while, ({$0 < 4}, 1, (+), [1,1,1,1,1])) });


// for : (X, Y => ([X], X -> Y) -> ()) = <intrinsic>
assert_equals({ 6 }, {
        for_data = box(0);
        for([1,2,3], { for_data <- {$$0 + $0} });
        *for_data
        });
assert_equals({ 6 }, {
        for_data = box(0);
        apply(for, ([1,2,3], { for_data <- {$$0 + $0} }));
        *for_data
        });
// for over various list types, note the for function simple udates the box value with the current list item
assert_equals({ 1 }, { for_data = box(0); for([1], { for_data <- {$0; $$0} }); *for_data }); // singletonlist
assert_equals({ 3 }, {
                        for_data = box(0);
                        sublist = take(3, take(4, [1,2,3]));
                        for(sublist, { for_data <- {$0; $$0} });
                        *for_data }); // sublist
assert_equals({ 0 }, {
                        for_data = box(0);
                        biglist = take(34, count(33));
                        for(biglist, { for_data <- {$0; $$0} });
                        *for_data }); // biglist
assert_equals({ 0 }, { for_data = box(0); for([], { for_data <- {$0; $$0} }); *for_data }); // emptylist
assert_equals({ 1 }, { for_data = box(0); for(rep(3, 1), { for_data <- {$0; $$0} }); *for_data }); // repeatedlist
assert_equals({ 3 }, { for_data = box(0); for(fromto(6, 3), { for_data <- {$0; $$0} }); *for_data }); // reverseIntlist
assert_equals({ 3 }, { for_data = box(0); for(flatten([[0,1], [2,3]]), { for_data <- {$0; $$0} }); *for_data }); // ChainedListPair.run()
assert_equals({ 3 }, { for_data = box(0); for(flatten([[0,1], [2], [3]]), { for_data <- {$0; $$0} }); *for_data }); // ChainedLists.run()
assert_equals({ 5 }, { for_data = box(0); for(flatten([[0,1] ,[2,3], [4,5]]), { for_data <- {$0; $$0} }); *for_data }); // MatrixList.run()


// iter = { p => while(p, { () }); () }
assert_equals({ 10 }, { b = box(0); iter({ b <- inc; *b < 10 }); *b });


// pfiltern = { lst, pred, n => list:flatten(pmap(chunks(lst, n), { $0_245_36 => list:filter($0_245_36, pred) })) }
assert_equals({ [0, 2, 4, 6, 8, 10, 12, 14, 16, 18] }, { pfiltern(count(20), {($0 % 2) == 0}, 4) });
assert_equals({ filter(count(20), {($0 % 2) == 0}) }, { pfiltern(count(20), {($0 % 2) == 0}, 4) });

// pfor : (X, Y => ([X], X -> Y) -> ()) = <intrinsic>
assert_equals({ 6 }, {
        pfor_data = box(0);
        pfor([1,2,3], { pfor_data <- {$$0 + $0} });
        *pfor_data;
        });

assert_equals({
        pfor_data = box(0);
        pfor([1,2,3], { pfor_data <- {$$0 + $0} });
        *pfor_data;
        },
        {
        for_data = box(0);
        for([1,2,3], { for_data <- {$$0 + $0} });
        *for_data
        });

assert_equals({ 6 }, {
        pfor_data = box(0);
        apply(pfor, ([1,2,3], { pfor_data <- {$$0 + $0} }));
        *pfor_data;
        });

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

// pwheren = { lst, pred, n => list:flatten(pmap(chunks(lst, n), { $0_253_36 => list:where($0_253_36, pred) })) }
assert_equals({ [0, 1, 3] }, { pwheren([4,4,7,4,7], {$0 < 5}, 3) });
assert_equals({ where([4,4,7,4,7], {$0 < 5}) }, { pwheren([4,4,7,4,7], {$0 < 5}, 3) });

// reduce : (A, B => ((A, B) -> A, A, [B]) -> A) = <intrinsic>
assert_equals({ 13 }, { reduce((+), 1, [2,4,6]) });
assert_equals({ 13 }, { apply(reduce, ((+), 1, [2,4,6])) });

// scan : (A, B => ((A, B) -> A, A, [B]) -> [A]) = <intrinsic>
// same as reduce but provides list of (initial and) intermediate values
assert_equals({ [1, 3, 7, 13] }, { scan((+), 1, [2,4,6]) });
assert_equals({ [1, 3, 7, 13] }, { apply(scan, ((+), 1, [2,4,6])) });

// scan_while = { pred, init, f, args => result = evolve_while(compose(list:last, pred), [init], { as, b => list:append(as, f(list:last(as), b)) }, args); list:drop(1, result) }
assert_equals({ [2, 3, 4] }, { scan_while({$0 < 4}, 1, (+), [1,1,1,1,1]) });

// tconverge = { func, init => diff = { accum, x, y => and(ne(x, y), { ne(y, init) }) }; next = { accum, x, y => (list:append(accum, y), y, func(y)) }; cycle(([init], diff, init, func(init)), next).0 }
assert_equals({ [0, 1, 2, 3, 4, 5, 6, 7, 8, 9] }, { tconverge({inc($0) % 10}, 0) });

// trace = { p, v, f => cycle([v], compose(last, p), { $0_43_26 => append($0_43_26, f(last($0_43_26))) }) }
assert_equals({ [0, 1, 2, 3, 4] }, { trace(0, { $0 < 4 }, inc) });

// tracen = { n, v, f => cyclen([v], n, { $0_57_20 => append($0_57_20, f(last($0_57_20))) }) }
assert_equals({ [0, 1, 2, 3, 4] }, { tracen(0, 4, inc) });

// while : (T => (() -> Bool, () -> T) -> ()) = <intrinsic>
assert_equals({ 5 }, {
        while_data = box(0);
        while({ *while_data < 5 }, { while_data <- inc });
        *while_data;
        });
assert_equals({ 5 }, {
        while_data = box(0);
        apply(while, ({ *while_data < 5 }, { while_data <- inc }));
        *while_data;
        });

// apply = { f, x => f(x) }
assert_equals({ 4 }, { apply(inc, 3) });

// compose = { f, g => { x => g(f(x)) } }
assert_equals({ 4 }, { compose(inc, inc)(2); });

// compl : <X, Y> (X -> Int, [Y]) -> (X -> Y)
assert_equals({ "odd" },
              {
              compl_func = compl({ $0 % 2 }, ["even", "odd"]);
              compl_func(7)
              });
assert_equals({ "odd" },
              {
              compl_func = apply(compl, ({ $0 % 2 }, ["even", "odd"]));
              compl_func(7)
              });

// compm : <X, K, Y> (X -> K, [K : Y]) -> (X -> Y)
assert_equals({ "ODD" },
              {
              compm_func = compm({ if(eq(0, $0 % 2), {#even}, {#odd}) }, [#even: "EVEN", #odd: "ODD"]);
              compm_func(7)
              });
assert_equals({ "ODD" },
              {
              compm_func = apply(compm, ({ if(eq(0, $0 % 2), {#even}, {#odd}) }, [#even: "EVEN", #odd: "ODD"]));
              compm_func(7)
              });

// eachleft : <A, B, C> ((A, B) -> C) -> (([A], B) -> [C])
assert_equals({ [5, 6, 7] }, { [0, 1, 2] @+ 5 });
assert_equals({ [5, 6, 7] }, { eachleft(plus)([0, 1, 2], 5) });
assert_equals({ [[5, 6], [7, 8]] }, { [[0, 1], [2, 3]] @@+ 5 });
assert_equals({ [[5, 6], [7, 8]] }, { eachleft(eachleft(plus))([[0, 1], [2, 3]], 5) });
assert_equals({ [5, 6, 7] }, { eachleft(plus)([0, 1, 2], 5) });
assert_equals({ [[10, 9, 8], [10, 9, 8], [10, 9, 8]] }, { [10, 10, 10] @(-@) [0, 1, 2] });
assert_equals({ [[10, 9, 8], [10, 9, 8], [10, 9, 8]] }, { eachleft(eachright(minus))([10, 10, 10], [0, 1, 2]) });

// eachright : <A, B, C> ((A, B) -> C) -> ((A, [B]) -> [C])
assert_equals({ [5, 6, 7] }, { 5 +@ [0, 1, 2] });
assert_equals({ [5, 6, 7] }, { eachright(plus)(5, [0, 1, 2]) });
assert_equals({ [[5, 6], [7, 8]] }, { 5 +@@ [[0, 1], [2, 3]] });
assert_equals({ [[5, 6], [7, 8]] }, { eachright(eachright(plus))(5, [[0, 1], [2, 3]]) });
assert_equals({ [[10, 10, 10], [9, 9, 9], [8, 8, 8]] }, { [10, 10, 10] @-@ [0, 1, 2] });
assert_equals({ [[10, 10, 10], [9, 9, 9], [8, 8, 8]] }, { [10, 10, 10] (@-)@ [0, 1, 2] });
assert_equals({ [[10, 10, 10], [9, 9, 9], [8, 8, 8]] }, { eachright(eachleft(minus))([10, 10, 10], [0, 1, 2]) });

// map : <X, Y> ([X], X -> Y) -> [Y]
assert_equals({ [2,3,4] }, { map([1,2,3], inc) });
assert_equals({ [2,3,4] }, { [1,2,3] | inc });
assert_equals({ [2,3,4] }, { apply(map, ([1,2,3], inc)) });
// map for lists
assert_equals({ [] }, { [] | inc }); // emptyList
assert_equals({ [4, 3, 2] }, { fromto(3, 1) | inc }); // reverseIntRange
assert_equals({ [2] }, { [1] | inc }); // singleton
assert_equals({ [1, 2, 3, 4] }, { flatten([[0,1], [2,3]]) | inc }); // ChainedListPair.apply()
assert_equals({ [1, 2, 3, 4] }, { flatten([[0,1], [2], [3]]) | inc }); // ChainedLists.apply()
assert_equals({ [1, 2, 3, 4, 5, 6] }, { flatten([[0,1] ,[2,3], [4,5]]) | inc }); // MatrixList.apply()


// mapll : <T> ([Int], [T]) -> [T]
assert_equals({ ["zero", "two"] }, { mapll([0,2], ["zero", "one", "two"]) });
assert_equals({ ["zero", "two"] }, { apply(mapll, ([0,2], ["zero", "one", "two"])) });
// mapll for various list types
assert_equals({ append(count(33), 0) }, { mapll(take(34, count(33)), count(34)) }); // biglist
assert_equals({ [] }, { mapll([], [1, 2]) }); // emptylist
assert_equals({ [1, 2] }, { mapll(fromto(0, 1), [1, 2]) }); // intRange
assert_equals({ [2, 2] }, { mapll(rep(2, 1), [1, 2]) }); // repeatList
assert_equals({ [2, 1] }, { mapll(fromto(1, 0), [1, 2]) }); // reverseIntRange
assert_equals({ [1] }, { mapll([0], [1, 2]) }); // singletonlist
assert_equals({ [2, 3, 4] }, { sublist = take(3, take(4, [1,2,3])); mapll(sublist, [1, 2, 3, 4]) }); // sublist
assert_equals({ [11, 22, 33, 44] }, { mapll(flatten([[0,1], [2,3]]), [11, 22, 33, 44]) }); // ChainedListPair.select(list)
assert_equals({ [11, 22, 33, 44] }, { mapll(flatten([[0,1], [2], [3]]), [11, 22, 33, 44]) }); // ChainedLists.select(list)
assert_equals({ [1, 1, 3, 3, 4, 5] }, { mapll(flatten([[1,1] ,[3,3], [4,5]]), count(10)) }); // MatrixList.select(list)


// maplm : <K, V> ([K], [K : V]) -> [V]
assert_equals({ ["ZERO", "TWO"] }, { maplm([#zero, #two], [#zero:"ZERO", #one:"ONE", #two:"TWO"]) });
assert_equals({ ["ZERO", "TWO"] }, { apply(maplm, ([#zero, #two], [#zero:"ZERO", #one:"ONE", #two:"TWO"])) });
// maplm for various list types
assert_equals({ rep(34, 1) }, { maplm(take(34, count(33)), counts(count(33))) }); // biglist
assert_equals({ [] }, { maplm([], [1:1, 2:2]) }); // emptylist
assert_equals({ [1, 2] }, { maplm(fromto(1, 2), [1:1, 2:2]) }); // intRange
assert_equals({ [1, 1] }, { maplm(rep(2, 1), [1:1, 2:2]) }); // repeatList
assert_equals({ [2, 1] }, { maplm(fromto(2, 1), [1:1, 2:2]) }); // reverseIntRange
assert_equals({ [1] }, { maplm([1], [1:1, 2:2]) }); // singletonlist
assert_equals({ [1, 2, 3] }, { sublist = take(3, take(4, [1,2,3])); maplm(sublist, [1:1, 2:2, 3:3, 4:4]) }); // sublist
assert_equals({ [0, 1, 2, 3] }, { maplm(flatten([[0,1], [2,3]]), [0:0, 1:1, 2:2, 3:3]) }); // ChainedListPair.select(map)
assert_equals({ [0, 1, 2, 3] }, { maplm(flatten([[0,1], [2], [3]]), [0:0, 1:1, 2:2, 3:3]) }); // ChainedLists.select(map)
assert_equals({ [1, 1, 3, 3, 2, 2] }, { maplm(flatten([[1,1] ,[3,3], [2,2]]), [0:0, 1:1, 2:2, 3:3]) }); // MatrixList.select(map)


// mapmf : <X, Y, Z> ([X : Y], Y -> Z) -> [X : Z]
assert_equals({ [#a: 4, #b:6] }, { mapmf([#a: (2, 2), #b: (3, 3)], (+)) });
assert_equals({ [#a: 4, #b:6] }, { apply(mapmf, ([#a: (2, 2), #b: (3, 3)], (+))) });

// mapml : <K, V> ([K : Int], [V]) -> [K : V]
assert_equals({ [#a: "Zero", #c: "Two"] }, { mapml([#a: 0, #c: 2], ["Zero", "One", "Two"]) });
assert_equals({ [#a: "Zero", #c: "Two"] }, { apply(mapml, ([#a: 0, #c: 2], ["Zero", "One", "Two"])) });

// mapmm : <X, Y, Z> ([X : Y], [Y : Z]) -> [X : Z]
assert_equals({ [#a: "One", #b: "Two"] }, { mapmm([#a: 1, #b: 2], [1: "One", 2: "Two"]) });
assert_equals({ [#a: "One", #b: "Two"] }, { apply(mapmm, ([#a: 1, #b: 2], [1: "One", 2: "Two"])) });

// mapz : <T.., X> (Tup(List @ T), Tup(T) -> X) -> [X]
assert_equals({ [6, 8, 8, 10] }, { mapz(([5, 6], [1,2,3,4]) , (+)) });
assert_equals({ [] }, { mapz(([5, 6], []) , (+)) });
assert_equals({ [6, 8, 8, 10] }, { apply(mapz, (([5, 6], [1,2,3,4]) , (+))) });
assert_equals({ [9, 12] }, { triplus(a, b, c) { a + b + c }; mapz(([1, 2], [3, 4], [5, 6]) , triplus) }); // wide lists
assert_equals({ [16, 20] }, { quadplus(a, b, c, d) { a + b + c + d }; mapz(([1, 2], [3, 4], [5, 6], [7, 8]) , quadplus) }); // wide lists
assert_equals({ [] }, { triplus(a, b, c) { a + b + c }; mapz(([1, 2], [], [5, 6]) , triplus) }); // empty list bailout

// pmap : (X, Y => ([X], X -> Y) -> [Y]) = <intrinsic>
assert_equals({ [2,3,4] }, { pmap([1,2,3], inc) });
assert_equals({ [2,3,4] }, { [1,2,3] |: inc });
assert_equals({ [] }, { pmap([], inc) });
assert_equals({ map([1,2,3], inc) }, { pmap([1,2,3], inc) });
assert_equals({ [2,3,4] }, { apply(pmap, ([1,2,3], inc)) });

// pmapn = { lst, f, n => list:flatten(pmap(chunks(lst, n), { $0_227_36 => map($0_227_36, f) })) }
assert_equals({ [1,2,3,4,5] }, { pmapn(count(5), inc, 2) });
assert_equals({ map(count(5), inc) }, { pmapn(count(5), inc, 2) });
assert_equals({ pmap(count(5), inc) }, { pmapn(count(5), inc, 2) });

// run = { b => b() }
assert_equals({ 3 }, { run({3}) });

// id = { v => v }
assert_equals({ "a" }, { id("a") });
assert_equals({ 3 }, { id(3) });

// and : (Bool, () -> Bool) -> Bool = <intrinsic>
andCount = box(0);

assert_true({ and(true, { andCount <- inc;1==1 }) });
assert_equals({ 1 }, { *andCount });
assert_false({ and(true, { andCount <- inc;1==2 }) });
assert_equals({ 2 }, { *andCount });
assert_false({ and(false, { andCount <- inc;1==1 }) });
assert_equals({ 2 }, { *andCount });
assert_false({ and(false, { andCount <- inc;1==2 }) });
assert_equals({ 2 }, { *andCount });
assert_true({ and(true, { true }) });
assert_false({ and(3>5, { true }) });
assert_false({ and(5>3, { false }) });
assert_true({ apply(and, (true, { true })) });

// or : (Bool, () -> Bool) -> Bool = <intrinsic>
orCount = box(0);
assert_true({ or(true, { orCount <- inc;1==1 }) });
assert_equals({ 0 }, { *orCount });
assert_true({ or(true, { orCount <- inc;1==2 }) });
assert_equals({ 0 }, { *orCount });
assert_true({ or(false, { orCount <- inc;1==1 }) });
assert_equals({ 1 }, { *orCount });
assert_false({ or(false, { orCount <- inc;1==2 }) });
assert_equals({ 2 }, { *orCount });
assert_true({ or(false, { true }) });
assert_true({ or(3>5, { 1+1; true }) });
assert_false({ or(3>5, { false }) });
assert_true({ or(3>5, { true }) });
assert_true({ apply(or, (true, { true })) });

// not : Bool -> Bool = <intrinsic>
assert_false({ not(true) });
assert_true({ not(false) });
assert_true({ apply(not, false) });

//eq : (T => (T, T) -> Bool) = <intrinsic>
assert_true({ eq(2, 2) });
assert_false({ eq(2, 1) });
assert_true({ r = (#a:1); eq(r, r) }); // record
assert_true({ apply(eq, (2, 2)) });
// eq for lists
assert_true({ l = fromto(1, 4); eq(l, l) });
assert_true({ l = fromto(4, 1); eq(l, l) });
assert_true({ l = rep(3, 1); eq(l, l) });
assert_true({ eq(fromto(1, 4), fromto(1, 4)) });
assert_true({ eq(fromto(4, 1), fromto(4, 1)) });
assert_true({ eq(fromto(4, 1), [4, 3, 2, 1]) });
assert_true({ eq(rep(3, 1), [1, 1, 1]) });
assert_true({ sublist = take(3, take(4, [1,2,3])); eq(sublist, sublist) });
assert_true({ sublist = take(3, take(4, [1,2,3])); eq(sublist, [1, 2, 3]) });
assert_true({ l = flatten([[0,1], [2,3]]); eq(l, l) });
assert_true({ l = flatten([[0,1], [2], [3]]); eq(l, l) }); // ChainedList.eq()
assert_true({ l = flatten([[0,1], [2], [3]]); eq(l, [0, 1, 2, 3]) }); // ChainedLists.eq()
assert_true({ l = flatten([[0,1] ,[2,3], [4,5]]); eq(l, l) }); // MatrixList.eq()
assert_true({ l = flatten([[0,1] ,[2,3], [4,5]]); eq(l, [0, 1, 2, 3, 4, 5]) }); // MatrixList.eq()

//ne : (T => (T, T) -> Bool) = <intrinsic>
assert_true({ ne(1, 2) });
assert_false({ ne(1, 1) });
assert_true({ apply(ne, (1, 2)) });

// empty : (T => T -> Bool) = <intrinsic>
assert_equals({ true }, { empty([]) }); // list
assert_equals({ true }, { empty([:]) }); // map
assert_equals({ true }, { empty(()) }); // tuple
assert_equals({ true }, { empty("") }); // string
assert_equals({ true }, { empty((:)) }); // record
assert_equals({ false }, { empty([1,2]) });
assert_equals({ false }, { empty([1:1]) });
assert_equals({ false }, { empty((1,1)) });
assert_equals({ false }, { empty("string") });
assert_equals({ false }, { empty((#a:1)) });
assert_equals({ false }, { empty(1) });
assert_equals({ true }, { apply(empty, []) });


// hash : (T => T -> Int) = <intrinsic>
assert_equals({ 1 }, { hash(1) });
assert_equals({ 114801 }, { hash("the") });
assert_equals({ 1 }, { apply(hash, 1) });
assert_equals({ 0 }, { hash((:)) }); // record

// gt : (Int, Int) -> Bool = <intrinsic>
assert_true({ gt(10, 9) });
assert_false({ gt(8, 9) });
assert_true({ apply(gt, (10, 9)) });

// ge : (Int, Int) -> Bool = <intrinsic>
assert_true({ ge(10, 9) });
assert_true({ ge(9, 9) });
assert_true({ apply(ge, (10, 9)) });

// lt : (Int, Int) -> Bool = <intrinsic>
assert_false({ lt(10, 9) });
assert_true({ lt(8, 9) });
assert_false({ apply(lt, (10, 9)) });

// le : (Int, Int) -> Bool = <intrinsic>
assert_false({ le(10, 9) });
assert_true({ le(9, 9) });
assert_false({ apply(le, (10, 9)) });


// div : (Int, Int) -> Int = <intrinsic>
assert_equals({ 2 }, { div(10, 5) });
assert_equals({ 10 }, { div(10, 1) });
assert_equals({ 10 }, { div(10, *box(1)) });
assert_equals({ 0 }, { div(0, *box(1)) });
assert_equals({ 2 }, { apply(div, (10, 5)) });


// minus : (Int, Int) -> Int = <intrinsic>
assert_equals({ 1 }, { minus(4, 3) });
assert_equals({ 1 }, { minus(4, *box(3)) });
assert_equals({ 1 }, { minus(*box(4), 3) });
assert_equals({ 4 }, { minus(*box(4), 0) });
assert_equals({ 1 }, { apply(minus, (4, 3)) });

// mod : (Int, Int) -> Int = <intrinsic>
assert_equals({ 1 }, { mod(13, 3) });
assert_equals({ 1 }, { apply(mod, (13, 3)) });

// neg : Int -> Int = <intrinsic>
assert_equals({ -1 }, { neg(1) });
assert_equals({ 1 }, { neg(-1) });
assert_equals({ -1 }, { apply(neg, 1) });

// plus : (Int, Int) -> Int = <intrinsic>
assert_equals({ 7 }, { plus(4, 3) });
assert_equals({ 7 }, { plus(4, *box(3)) });
assert_equals({ 3 }, { plus(0, *box(3)) });
assert_equals({ 7 }, { apply(plus, (4, 3)) });

// plus : (T => (T, T) -> T) = <intrinsic>
assert_equals({ 2 }, { plus(1, 1) }); // integer
assert_equals({ 2.0 }, { plus(1.0, 1.0) }); //double
assert_equals({ 2L }, { plus(1L, 1L) }); // long
assert_equals({ i2f(2) }, { plus(i2f(1), i2f(1)) }); // float TODO: this is actually double not float
assert_equals({ "one two" }, { plus("one", " two") }); // string
assert_equals({ [1,2,3] }, { plus([1], [2,3]) }); // list
assert_equals({ [1:1,2:2,3:3] }, { plus([1:1], [2:2,3:3]) }); // map
assert_equals({ true }, { plus(true, true) }); // boolean
assert_equals({ true }, { plus(false, true) }); // boolean
assert_equals({ true }, { plus(true, false) }); // boolean
assert_equals({ false }, { plus(false, false) }); // boolean
assert_equals({ 2 }, { apply(plus, (1, 1)) }); // integer

// pow : (Int, Int) -> Int = <intrinsic>
assert_equals({ 16 }, { pow(2, 4) });
assert_equals({ 16 }, { apply(pow, (2, 4)) });

// times : (Int, Int) -> Int = <intrinsic>
assert_equals({ 12 }, { times(3, 4) });
assert_equals({ 0 }, { times(0, 4) });
assert_equals({ 4 }, { times(1, 4) });
assert_equals({ 0 }, { times(0, *box(4)) });
assert_equals({ 4 }, { times(1, *box(4)) });
assert_equals({ 8 }, { times(2, *box(4)) });
assert_equals({ 0 }, { times(*box(4), 0) });
assert_equals({ 4 }, { times(*box(4), 1) });
assert_equals({ 8 }, { times(*box(4), 2) });
assert_equals({ 12 }, { apply(times, (3, 4)) });

// abs = { n => guard(ge(n, 0), n, { neg(n) }) }
assert_equals({ 1 }, { abs(-1) });
assert_equals({ 1 }, { abs(1) });
assert_equals({ 0 }, { abs(0) });
assert_equals({ 0 }, { abs(-0) });

// constrain = { lo, n, hi => max(lo, min(hi, n)) }
assert_equals({ 2 }, { constrain(2, 1, 4) });
assert_equals({ 3 }, { constrain(2, 3, 4) });
assert_equals({ 4 }, { constrain(2, 5, 4) });

// dec = { n => minus(n, 1) }
assert_equals({ -1 }, { dec(0) });
assert_equals({ 0 }, { dec(1) });

// divz = { n, d => guard(eq(d, 0), 0, { div(n, d) }) }
assert_equals({ 2 }, { divz(4, 2) });
assert_equals({ 0 }, { divz(0, 2) });
assert_equals({ 0 }, { divz(3, 0) });

// even : { n => eq(mod(n, 2), 0) }
assert_true({ even(12) });
assert_true({ even(0) });
assert_false({ even(1) });
assert_false({ even(-1) });

// inc = { n => plus(n, 1) }
assert_equals({ 0 }, { inc(-1) });
assert_equals({ 2 }, { inc(1) });

// inrange = { x, base, extent => and(ge(x, base), { lt(x, plus(base, extent)) }) }
assert_equals({ true }, { inrange(4, 2, 5) });
assert_equals({ true }, { inrange(2, 2, 1) });
assert_equals({ false }, { inrange(1, 2, 1) });
assert_equals({ false }, { inrange(3, 2, 1) });

// max : (Int, Int) -> Int = <intrinsic>
assert_equals({ 4 }, { max(3, 4) });
assert_equals({ 4 }, { max(4, 3) });
assert_equals({ 4 }, { max(4, 4) });
assert_equals({ 4 }, { apply(max, (3, 4)) });

// min : (Int, Int) -> Int = <intrinsic>
assert_equals({ 3 }, { min(3, 4) });
assert_equals({ 3 }, { min(4, 3) });
assert_equals({ 4 }, { min(4, 4) });
assert_equals({ 3 }, { apply(min, (3, 4)) });


// modz = { n, d => guard(eq(d, 0), 0, { mod(n, d) }) }
assert_equals({ 1 }, { modz(4, 3) });
assert_equals({ 0 }, { modz(4, 0) });

// odd : { n => ne(mod(n, 2), 0) }
assert_false({ odd(12) });
assert_false({ odd(0) });
assert_true({ odd(1) });
assert_true({ odd(-1) });

// sign : Int -> Int = <intrinsic>
assert_equals({ 0 }, { sign(0) });
assert_equals({ -1 }, { sign(-2) });
assert_equals({ 1 }, { sign(2) });
assert_equals({ 0 }, { apply(sign, 0) });

// sq = { i => times(i, i) }
assert_equals({ 4 }, { sq(2) });

// lminus : (Long, Long) -> Long = <intrinsic>
assert_equals({ 1L }, { lminus(4L, 3L) });
assert_equals({ 1L }, { apply(lminus, (4L, 3L)) });

//////////////////////////////////////////////////
// fp relops
//////////////////////////////////////////////////
// fgt : (Double, Double) -> Bool = <intrinsic>
assert_true({ fgt(10.0, 9.8) });
assert_false({ fgt(9.7, 9.8) });
assert_true({ apply(fgt, (10.0, 9.8)) });

// fge : (Double, Double) -> Bool = <intrinsic>
assert_true({ fge(10.0, 9.8) });
assert_true({ fge(9.8, 9.8) });
assert_true({ apply(fge, (10.0, 9.8)) });

// flt : (Double, Double) -> Bool = <intrinsic>
assert_false({ flt(10.0, 9.8) });
assert_true({ flt(9.7, 9.8) });
assert_false({ apply(flt, (10.0, 9.8)) });

// fle : (Double, Double) -> Bool = <intrinsic>
assert_false({ fle(10.0, 9.8) });
assert_true({ fle(9.8, 9.8) });
assert_false({ apply(fle, (10.0, 9.8)) });

// fminus : (Double, Double) -> Double = <intrinsic>
assert_equals({ 6.925 }, { fminus(10.125, 3.2) });
assert_equals({ 1.0 }, { fminus(4.0, *box(3.0)) });
assert_equals({ 1.0 }, { fminus(*box(4.0), 3.0) });
assert_equals({ 4.0 }, { fminus(*box(4.0), 0.0) });
assert_equals({ 6.925 }, { apply(fminus, (10.125, 3.2)) });


//ftimes : (Double, Double) -> Double = <intrinsic>
assert_equals({ 10.15625 }, { ftimes(8.125, 1.25) });
assert_equals({ 0.0 }, { ftimes(0.0, 4.0) });
assert_equals({ 4.0 }, { ftimes(1.0, 4.0) });
assert_equals({ 0.0 }, { ftimes(0.0, *box(4.0)) });
assert_equals({ 4.0 }, { ftimes(1.0, *box(4.0)) });
assert_equals({ 8.0 }, { ftimes(2.0, *box(4.0)) });
assert_equals({ 0.0 }, { ftimes(*box(4.0), 0.0) });
assert_equals({ 4.0 }, { ftimes(*box(4.0), 1.0) });
assert_equals({ 8.0 }, { ftimes(*box(4.0), 2.0) });
assert_equals({ 10.15625 }, { apply(ftimes, (8.125, 1.25)) });

// fdiv : (Double, Double) -> Double = <intrinsic>
assert_equals({ 6.6 }, { fdiv(8.25, 1.25) });
assert_equals({ 10.0 }, { fdiv(10.0, 1.0) });
assert_equals({ 6.6 }, { fdiv(*box(8.25), 1.25) });
assert_equals({ 10.0 }, { fdiv(10.0, *box(1.0)) });
assert_equals({ 0.0 }, { fdiv(0.0, *box(1.0)) });
assert_equals({ 6.6 }, { apply(fdiv, (8.25, 1.25)) });

// fmod : (Double, Double) -> Double = <intrinsic>
assert_equals({ 0.125 }, { fmod(12.125, 4.0) });
assert_equals({ 0.125 }, { apply(fmod, (12.125, 4.0)) });

// fneg : (Double, Double) -> Double = <intrinsic>
assert_equals({ -1.125 }, { fneg(1.125) });
assert_equals({ 1.125 }, { fneg(-1.125) });
assert_equals({ -1.125 }, { apply(fneg, 1.125) });

// fpow : (Double, Double) -> Double = <intrinsic>
assert_equals({ 27.0 }, { fpow(9.0, 1.5) });
assert_equals({ 27.0 }, { apply(fpow, (9.0, 1.5)) });

// exp : Double -> Double = <intrinsic>
assert_equals({ 7.38905609893065 }, { exp(2.0) });
assert_equals({ 7.38905609893065 }, { apply(exp, 2.0) });

// fabs = { f => guard(fge(f, 0.0), f, { fminus(0.0, f) }) }
assert_equals({ 1.0 }, { fabs(-1.0) });
assert_equals({ 1.0 }, { fabs(1.0) });
assert_equals({ 0.0 }, { fabs(0.0) });
assert_equals({ 0.0 }, { fabs(-0.0) });


// finrange = { f, fbase, fextent => and(fge(f, fbase), { flt(f, plus(fbase, fextent)) }) }
assert_equals({ true }, { finrange(4.0, 2.0, 5.0) });
assert_equals({ true }, { finrange(2.0, 2.0, 1.0) });
assert_equals({ false }, { finrange(1.0, 2.0, 1.0) });
assert_equals({ false }, { finrange(3.0, 2.0, 1.0) });

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

// fsign = { f => guard(fgt(f, 0.0), 1, { iif(flt(f, 0.0), -1, 0) }) }
assert_equals({ 1 }, { fsign(1.0) });
assert_equals({ -1 }, { fsign(-1.0) });
assert_equals({ 0 }, { fsign(-0.0) });
assert_equals({ 0 }, { fsign(0.0) });

// fdivz = { n, d => guard(eq(d, 0.0), 0.0, { fdiv(n, d) }) }
assert_equals({ 2.0 }, { fdivz(4.0, 2.0) });
assert_equals({ 0.0 }, { fdivz(0.0, 2.0) });
assert_equals({ 0.0 }, { fdivz(3.0, 0.0) });

// fmodz = { n, d => guard(eq(d, 0.0), 0.0, { fmod(n, d) }) }
assert_equals({ 0.125 }, { fmodz(3.125, 1.0) });
assert_equals({ 0.0 }, { fmodz(3.125, 0.0) });

// fsq = { f => ftimes(f, f) }
assert_equals({ 9.0 }, { fsq(3.0) });

// ln : Double -> Double = <intrinsic>
assert_equals({ 0.0 }, { ln(1.0) });
assert_equals({ 0.0 }, { apply(ln, 1.0) });

// log = { b, n => fdiv(ln(n), ln(b)) }
assert_equals({ 2.0 }, { log(10.0, 100.0) });

// ilog2 : (Int) -> Int = <intrinsic>
assert_equals({ 8 }, { ilog2(256) });
assert_equals({ 7 }, { ilog2(255) });
assert_equals({ 0 }, { ilog2(1) });
assert_equals({ 0 }, { ilog2(-1) });
assert_equals({ 30 }, { ilog2(0x40000000) });


// round = { f => f2i(plus(f, 0.5)) }
assert_equals({ 1 }, { round(1.49) });
assert_equals({ 2 }, { round(1.50) });
assert_equals({ 0 }, { round(0.0) });
assert_equals({ 0 }, { round(-0.49) });
assert_equals({ -1 }, { round(-0.51) });

// sqrt : Double -> Double = <intrinsic>
assert_equals({ 3.0 }, { sqrt(9.0) });
assert_equals({ 3.0 }, { sqrt(*box(9.0)) });
assert_equals({ 3.0 }, { apply(sqrt, 9.0) });

// atan2 : (Double, Double) -> Double = <intrinsic>
assert_equals({ 0.0 }, { atan2(0.0, 0.0) });
assert_equals({ 0.0 }, { apply(atan2, (0.0, 0.0)) });

// cos : Double -> Double = <intrinsic>
assert_equals({ 1.0 }, { cos(0.0) });
assert_equals({ 1.0 }, { apply(cos, 0.0) });

// sin : Double -> Double = <intrinsic>
assert_equals({ 0.0 }, { sin(0.0) });
assert_equals({ 1.0 }, { sin(1.570796326795) });
assert_equals({ 0.0 }, { apply(sin, 0.0) });

// tan : Double -> Double = <intrinsic>
assert_equals({ 0.0 }, { tan(0.0) });
assert_equals({ 1.5574077246549023 }, { tan(1.0) });
assert_equals({ 0.0 }, { apply(tan, 0.0) });

//////////////////////////////////////////////////
// int bitwise ops
//////////////////////////////////////////////////
// band : (Int, Int) -> Int = <intrinsic>
assert_equals({ 1 }, { band(5, 3) });
assert_equals({ 1 }, { apply(band, (5, 3)) });

// bor : (Int, Int) -> Int = <intrinsic>
assert_equals({ 7 }, { bor(5, 3) });
assert_equals({ 7 }, { apply(bor, (5, 3)) });

// bxor : (Int, Int) -> Int = <intrinsic>
assert_equals({ 6 }, { bxor(12, 10) });
assert_equals({ 6 }, { apply(bxor, (12, 10)) });

// shiftr : (Int, Int) -> Int = <intrinsic>
assert_equals({ 64 }, { shiftr(256, 2) });
assert_equals({ 64 }, { apply(shiftr, (256, 2)) });
assert_equals({ -1 }, { shiftr(l2i(0x80000000), 31) });
assert_equals({ -1 }, { apply(shiftr, (l2i(0x80000000), 31)) });

// ushiftr : (Int, Int) -> Int = <intrinsic>
assert_equals({ 64 }, { ushiftr(256, 2) });
assert_equals({ 64 }, { apply(ushiftr, (256, 2)) });
assert_equals({ 1 }, { ushiftr(l2i(0x80000000), 31) });
assert_equals({ 1 }, { apply(ushiftr, (l2i(0x80000000), 31)) });

// shiftl : (Int, Int) -> Int = <intrinsic>
assert_equals({ 40 }, { shiftl(5, 3) });
assert_equals({ 40 }, { apply(shiftl, (5, 3)) });

//////////////////////////////////////////////////
// string
//////////////////////////////////////////////////
// endswith : (String, String) -> Bool = <intrinsic>
assert_true({ endswith("hello world", "world") });
assert_true({ apply(endswith, ("hello world", "world")) });

// startswith : (String, String) -> Bool = <intrinsic>
assert_true({ startswith("hello world", "hello") });
assert_true({ apply(startswith, ("hello world", "hello")) });

// strcat : [String] -> String = <intrinsic>
assert_equals({ "hello world" }, { strcat(["hello", " ", "world"]) });
assert_equals({ "hello world" }, { apply(strcat, (["hello", " ", "world"]) )});

// strcmp : (String, String) -> Int = <intrinsic>
assert_equals({ -1 }, { strcmp("a", "b") });
assert_equals({ 2 }, { strcmp("c", "a") });
assert_equals({ -1 }, { apply(strcmp, ("a", "b")) });

// strcut : (String, [Int]) -> [String] = <intrinsic>
assert_equals({ ["ab", "cd", "ef", "gh", "ij"] }, { strcut("abcdefghij", [0,2,4,6,8]) });
assert_equals({ [] }, { strcut("abc", []) });
assert_equals({ ["ab", "cd", "ef", "gh", "ij"] }, { apply(strcut, ("abcdefghij", [0,2,4,6,8])) });

// strdrop : (Int, String) -> String = <intrinsic>
assert_equals({ "efghij" }, { strdrop(4, "abcdefghij") });
assert_equals({ "abcdef" }, { strdrop(-4, "abcdefghij") });
assert_equals({ "efghij" }, { apply(strdrop, (4, "abcdefghij")) });

// strfind : (String, String) -> Int = <intrinsic>
assert_equals({ 2 }, { strfind("abcdef", "cd") });
assert_equals({ 6 }, { strfind("abcdef", "xy") });
assert_equals({ 2 }, { apply(strfind, ("abcdef", "cd")) });

// strjoin : ([String], String) -> String = <intrinsic>
assert_equals({ "hi,there,neighbor" }, { strjoin(["hi", "there", "neighbor"], ",") });
assert_equals({ "hi,there,neighbor" }, { apply(strjoin, (["hi", "there", "neighbor"], ",")) });

// strlen : String -> Int = <intrinsic>
assert_equals({ 11 }, { strlen("hello world") });
assert_equals({ 11 }, { apply(strlen, "hello world") });

// strsplit : (String, String) -> [String] = <intrinsic>
assert_equals({ ["hi", "there", "neighbor"] }, { strsplit("hi,there,neighbor", ",") });
assert_equals({ ["hi", "there", "neighbor"] }, { apply(strsplit, ("hi,there,neighbor", ",")) });

// strtake : (Int, String) -> String = <intrinsic>
assert_equals({ "hello" }, { strtake(5, "hello world") });
assert_equals({ "world" }, { strtake(-5, "hello world") });
assert_equals({ "hello worldhello" }, { strtake(16, "hello world") });
assert_equals({ "" }, { strtake(5, "") });
assert_equals({ "abcde" }, { strtake(5, "abcde") });
assert_equals({ "eabcde" }, { strtake(-6, "abcde") });
assert_equals({ "hello" }, { apply(strtake, (5, "hello world")) });

// strwhere : (String, String -> Bool) -> [Int] = <intrinsic>
assert_equals({ [3,7,10,12] }, { strwhere("abcZdefZghZiZjk", { $0 == "Z" }) });
assert_equals({ [] }, { strwhere("", { $0 == "Z" }) });
assert_equals({ [3,7,10,12] }, { apply(strwhere, ("abcZdefZghZiZjk", { $0 == "Z" })) });

// substr : (String, Int, Int) -> String = <intrinsic>
assert_equals({ "lo w" }, { substr("hello world", 3, 4) });
assert_equals({ "lo w" }, { apply(substr, ("hello world", 3, 4)) });

// sym2s : Symbol -> String = <intrinsic>
// TODO

// tolower : String -> String = <intrinsic>
assert_equals({ "hello world" }, { tolower("HelLo WoRLd") });
assert_equals({ "hello world" }, { apply(tolower, "HelLo WoRLd") });


//toupper : String -> String = <intrinsic>
assert_equals({ "HELLO WORLD" }, { toupper("HelLo WoRLd") });
assert_equals({ "HELLO WORLD" }, { apply(toupper, "HelLo WoRLd") });

// b2i : Int -> Bool = <intrinsic>
assert_equals({ 1 }, { b2i(true) });
assert_equals({ 0 }, { b2i(false) });
assert_equals({ 1 }, { apply(b2i, true) });

// i2b : Int -> Bool = <intrinsic>
assert_true({ i2b(1) });
assert_false({ i2b(0) });
assert_true({ i2b(2) });
assert_true({ i2b(-3) });
assert_false({ i2b(-0) });
assert_true({ apply(i2b, 1) });

// i2f : Int -> Double = <intrinsic>
assert_equals({ 1.0 }, { i2f(1) });
assert_equals({ 0.0 }, { i2f(-0) });
assert_equals({ 0.0 }, { i2f(0) });
assert_equals({ -3.0 }, { i2f(-3) });
assert_equals({ 1.0 }, { apply(i2f, 1) });

// i2s : Int -> String = <intrinsic>
assert_equals({ "1" }, { i2s(1) });
assert_equals({ "1" }, { apply(i2s, 1) });

// f2i : Double -> Int = <intrinsic>
assert_equals({ 1 }, { f2i(1.8) });
assert_equals({ -2 }, { f2i(-1.8) });
assert_equals({ 0 }, { f2i(-0.0) });
assert_equals({ 1 }, { apply(f2i, 1.8) });

// f2s : Double -> String = <intrinsic>
assert_equals({ "3.12" }, { f2s(3.12) });
assert_equals({ "3.12" }, { apply(f2s, 3.12) });

// l2f : Long -> Double = <intrinsic>
assert_equals({ 5.0 }, { l2f(5L) });
assert_equals({ 5.0 }, { apply(l2f, 5L) });

// l2i : Long -> Int = <intrinsic>
assert_equals({ 5 }, { l2i(5L) });
assert_equals({ 5 }, { apply(l2i, 5L) });

// l2s : Long -> String = <intrinsic>
assert_equals({ "5" }, { l2s(5L) });
assert_equals({ "5" }, { apply(l2s, 5L) });

// s2f : String -> Double = <intrinsic>
assert_equals({ 1.0 }, { s2f("1") });
assert_equals({ 0.0 }, { s2f("string") });
assert_equals({ 1.0 }, { apply(s2f, "1") });

// s2i : String -> Int = <intrinsic>
assert_equals({ 1 }, { s2i("1") });
assert_equals({ 0 }, { s2i("string") });
assert_equals({ 1 }, { apply(s2i, "1") });

// s2l : String -> Long = <intrinsic>
assert_equals({ 5L }, { s2l("5") });
assert_equals({ 0L }, { s2l("string") });
assert_equals({ 5L }, { apply(s2l, "5") });

// s2sym : String -> Symbol = <intrinsic>
// TODO

//tostr : (T => T -> String) = <intrinsic>
assert_equals({ "\"String\"" }, { tostr("String") });       // string
assert_equals({ "[#a: 2]" }, { tostr([#a:2]) });            // symbol, this will hit Symbol after map
assert_equals({ "[1, 2, 3]" }, { tostr([1,2,3]) });         // list
assert_equals({ "[#a: 2]" }, { tostr([#a:2]) });            // map
assert_equals({ "[:]" }, { tostr([:]) });                   // empty map
assert_equals({ "(1, \"b\", 3)" }, { tostr((1, "b", 3)) }); // tuple
assert_equals({ "()" }, { tup=(); tostr(tup) }); // tuple
assert_equals({ "(1,)" }, { tup=(1,); tostr(tup) }); // tuple
assert_equals({ "(#a: 2)" }, { tostr((#a:2)) });            // record
assert_equals({ "(:)" }, { tostr((:)) });            // record
// variant
assert_equals({ "box(3)" }, { tostr(box(3)) });             // box
assert_equals({ "\"String\"" }, { apply(tostr, "String") });

// append : (T => ([T], T) -> [T]) = <intrinsic>
assert_equals({ [1,2,3,4] }, { append([1,2,3], 4) });
assert_equals({ [1,2,3,4,5] }, { append(fromto(1, 4), 5) });
assert_equals({ [4, 3, 2, 1, 0] }, { append(fromto(4, 1), 0) });
assert_equals({ [4] }, { append([], 4) });
assert_equals({ [1, 1, 1, 4] }, { append(rep(3, 1), 4) });
assert_equals({ [1,2,3,4] }, { apply(append, ([1,2,3], 4)) });
assert_equals({ [0, 1, 2, 3, 4] }, { append(flatten([[0,1], [2], [3]]), 4) }); // ChainedList.append()
assert_equals({ [0, 1, 2, 4, 3, 5, 6] }, { append(flatten([[0,1] ,[2,4], [3,5]]), 6) }); // MatrixList.append()

// chunks = <T> { (list: [T], nchunks: Int) -> [[T]] => cut(list, cutpoints(list, nchunks)) }assert_equals({ [[]] }, { cutpoints(count(5), 0) });
assert_equals({ [] }, { chunks(count(5), 0) });
assert_equals({ [[0, 1, 2, 3, 4]] }, { chunks(count(5), 1) });
assert_equals({ [[0, 1], [2, 3, 4]] }, { chunks(count(5), 2) });
assert_equals({ [[0], [1, 2], [3, 4]] }, { chunks(count(5), 3) });
assert_equals({ [[0], [1], [2], [3, 4]] }, { chunks(count(5), 4) });
assert_equals({ [[0], [1], [2], [3], [4]] }, { chunks(count(5), 5) });

// contains = { list, item => lt(find(list, item), size(list)) }
assert_equals({ false }, { contains([3,4,5], 2) });
assert_equals({ true }, { contains([3,4,5], 4) });
assert_equals({ false }, { contains(["cat", "dog", "fish"], "mouse") });
assert_equals({ true }, { contains(["cat", "dog", "fish"], "fish") });


// count : Int -> [Int] = <intrinsic>
assert_equals({ [0,1,2] }, { count(3) });
assert_equals({ [0] }, { count(-1) });
assert_equals({ [0,1,2] }, { apply(count, 3) });

// cut : (T => ([T], [Int]) -> [[T]]) = <intrinsic>
assert_equals({ [[1, 2], [3, 4], [5, 6], [7, 8], [9, 10]] }, { cut([1,2,3,4,5,6,7,8,9,10], [0,2,4,6,8]) });
assert_equals({ [[2, 3, 4], []] }, { cut(fromto(1, 4), [1, 4]) });
assert_equals({ [[3, 2, 1], []] }, { cut(fromto(4, 1), [1, 4]) });
assert_equals({ [[]] }, { cut([], [0]) });
assert_equals({ [[1, 1, 1]] }, { cut(rep(3, 1), [0]) });
assert_equals({ [[1, 2], [3, 4], [5, 6], [7, 8], [9, 10]] }, { apply(cut, ([1,2,3,4,5,6,7,8,9,10], [0,2,4,6,8])) });

// cutpoints = <T> { (list: [T], ncuts: Int) -> [Int] => s = size(list); n = constrain(0, ncuts, s); c = divz(s, n); r = modz(s, n); sizes = plus(rep(minus(n, r), c), rep(r, plus(c, 1))); starts(sizes) }
assert_equals({ [] }, { cutpoints(count(5), 0) });
assert_equals({ [0] }, { cutpoints(count(5), 1) });
assert_equals({ [0, 2] }, { cutpoints(count(5), 2) });
assert_equals({ [0, 1, 3] }, { cutpoints(count(5), 3) });
assert_equals({ [0, 1, 2, 3] }, { cutpoints(count(5), 4) });
assert_equals({ [0, 1, 2, 3, 4] }, { cutpoints(count(5), 5) });

// distinct : (T => [T] -> [T]) = <intrinsic>
assert_equals({ [1,2,3] }, { distinct([1,2,2,1,3]) });
assert_equals({ [1,2,3] }, { apply(distinct, [1,2,2,1,3]) });

// draw : (Int, Int) -> [Int] = <intrinsic>
assert_equals({ [0, 0, 0] }, { draw(3, 1) });
assert_equals({ [0, 0, 0] }, { apply(draw, (3, 1)) });


// drop : (T => (Int, [T]) -> [T]) = <intrinsic>
assert_equals({ [4, 5] }, { drop(3, [1,2,3,4,5]) });
assert_equals({ [4, 5] }, { apply(drop, (3, [1,2,3,4,5])) });

// eachpair = { init, f, args => list = plus([init], args); map(index(args), { i => f(list[i], list[plus(i, 1)]) }) }
assert_equals({ [false, false, true, false, false] }, { eachpair(0, gt, [1,3,2,4,5]) });

// edges = { list => plus([0], filter(drop(1, index(list)), { $0_92_40 => ne(list[minus($0_92_40, 1)], list[$0_92_40]) })) }
assert_equals({ [0, 1, 4] }, { edges([3,2,2,2,8,8]) });

// enlist = { v => [v] }
assert_equals({ [2] }, { enlist(2) });

// filet = { list, n => s = size(list); cut(list, eachleft(times)(count(plus(divz(s, n), sign(modz(s, n)))), n)) }
assert_equals({ [[0, 1], [2, 3], [4, 5], [6, 7], [8, 9]] }, { filet(count(10), 2) });
assert_equals({ [[0, 1, 2, 3], [4]] }, { filet(count(5), 4) });
assert_equals({ [[0, 1, 2, 3, 4]]}, { filet(count(5), 5) });
assert_equals({ [[0], [1], [2]] }, { filet(count(3), -1) });
assert_equals({ [[0, 1, 2, 3, 4]]}, { filet(count(5), 6) });
assert_equals({ [] }, { filet(count(3), 0) });


// filter : (T => ([T], T -> Bool) -> [T]) = <intrinsic>
assert_equals({ [0,1] } , { filter([0,1,2,3], {$0 < 2}) });
assert_equals({ [] } , { filter([], {$0 < 2}) });
assert_equals({ [] } , { filter([0,1,2,3], {$0 > 4}) });
assert_equals({ [0,1] } , { apply(filter, ([0,1,2,3], {$0 < 2})) });

// find : (T => ([T], T) -> Int) = <intrinsic>
assert_equals({ 4 }, { find(["cat", "dog", "bird", "snake"], "fox") });
assert_equals({ 1 }, { find(["cat", "dog", "bird", "snake"], "dog") });
assert_equals({ 4 }, { apply(find, (["cat", "dog", "bird", "snake"], "fox")) });
assert_equals({ 1 }, { find(fromto(4, 1), 3) });
assert_equals({ 4 }, { find(fromto(4, 1), 5) });
assert_equals({ 1 }, { apply(find, (fromto(4, 1), 3)) });
assert_equals({ 2 }, { find(fromto(1, 4), 3) });
assert_equals({ 4 }, { find(fromto(1, 4), 5) });
assert_equals({ 2 }, { apply(find, (fromto(1, 4), 3)) });
assert_equals({ 0 }, { find([], 1) });
assert_equals({ 0 }, { find(rep(3, "a"), "a") });
assert_equals({ 3 }, { find(rep(3, "a"), "b") });
assert_equals({ 0 }, { find([1], 1) });
assert_equals({ 1 }, { find([1], 0) });
assert_equals({ 1 }, { sublist = take(3, take(4, [1,2,3])); find(sublist, 2) }); // sublist
assert_equals({ 2 }, { find(take(34, count(33)), 2) }); // biglist
assert_equals({ 1 }, { find(flatten([[0,1], [2,3]]), 1) }); // ChainedListPair.find()
assert_equals({ 4 }, { find(flatten([[0,1], [2,3]]), 5) }); // ChainedListPair.find()
assert_equals({ 1 }, { find(flatten([[0,1], [2], [3]]), 1) }); // ChainedList.find()
assert_equals({ 4 }, { find(flatten([[0,1] ,[2], [3]]), 5) }); // ChainedList.find()
assert_equals({ 1 }, { find(flatten([[0,1], [2,4], [3,5]]), 1) }); // MatrixList.find()
assert_equals({ 6 }, { find(flatten([[0,1] ,[2,4], [3,5]]), 6) }); // MatrixList.find()



// first : (T => [T] -> T) = <intrinsic>
assert_equals({ "cat" }, { first(["cat", "dog", "bird", "dog", "snake"]) });
assert_equals({ "cat" }, { apply(first, ["cat", "dog", "bird", "dog", "snake"]) });


// first_where = { pred, vals => n = list:size(vals); cycle(0, { i => and(lt(i, n), { not(pred(vals[i])) }) }, inc) }
assert_equals({ 4 }, { first_where({ 3 < $0 }, [0,1,2,3,4,5]) });

// flatten : (A => [[A]] -> [A]) = <intrinsic>
assert_equals({ [1,2,3,4,9,11] }, { flatten([[1,2,3,4], [9,11]]) });
assert_equals({ [1,2,3,4,9,11] }, { apply(flatten, ([[1,2,3,4], [9,11]])) });

// fromto : (Int, Int) -> [Int] = <intrinsic>
assert_equals({ [1, 2, 3] }, { fromto(1,3) });
assert_equals({ [-1, 0, 1, 2, 3] }, { fromto(-1,3) });
assert_equals({ [-1, -2, -3] }, { fromto(-1,-3) });
assert_equals({ [3, 2, 1] }, { fromto(3, 1) });
assert_equals({ [1, 2, 3] }, { apply(fromto, (1,3)) });


// group : (K, V => ([K], [V]) -> [K : [V]]) = <intrinsic>
assert_equals({ [1: ["a"], 2: ["b"], 3: ["c"], 4: ["d"]] }, { group([1,2,3,4], ["a", "b", "c", "d"]) });
assert_equals({ [1: ["rep1", "rep3"], 2: ["rep2", "rep4"]] }, { group([1,2], ["rep1", "rep2", "rep3", "rep4"]) });
assert_equals({ [1: ["a"], 2: ["b"]] }, { group([1,2,3,4], ["a", "b"]) });
assert_equals({ [:] }, { group([1,2,3,4], []) });
assert_equals({ [1: ["a"], 2: ["b"], 3: ["c"], 4: ["d"]] }, { apply(group, ([1,2,3,4], ["a", "b", "c", "d"])) });

// index : { <T> [T] -> [Int] => <intrinsic> }
assert_equals({ [0, 1, 2, 3] }, { index([5, 3, 2, 1]) });
assert_equals({ [0, 1, 2] }, { index(["a", "dog", "barks"]) });
assert_equals({ [] }, { index([]) });
assert_equals({ [0, 1, 2, 3] }, { apply(index, ([5, 3, 2, 1])) });
assert_equals({ [0,1,2] }, { index(["a", "b", "c"]) });

// isindex : (T => (Int, [T]) -> Bool) = <intrinsic>
assert_true({ isindex(3, ["a", "b", "c", "d", "e"]) });
assert_false({ isindex(9, ["a", "b", "c", "d", "e"]) });
assert_false({ isindex(-2, ["a", "b", "c", "d", "e"]) });
assert_true({ apply(isindex, (3, ["a", "b", "c", "d", "e"])) });

// last : (T => [T] -> T) = <intrinsic>
assert_equals({ "e" }, { last(["a", "b", "c", "d", "e"]) });
assert_equals({ "e" }, { apply(last, ["a", "b", "c", "d", "e"]) });

// lplus : (T => ([T], [T]) -> [T]) = <intrinsic>
assert_equals({ [3, 5, 7, 2, 4, 6] }, { lplus([3,5,7], [2,4,6]) });
assert_equals({ [3, 5, 7, 2, 4, 6] }, { apply(lplus, ([3,5,7], [2,4,6])) });

// listset : (T => ([T], Int, T) -> [T]) = <intrinsic>
assert_equals({ [3, 9, 7] }, { listset([3,5,7], 1, 9) });
assert_equals({ [1, 2, 9, 4] }, { listset(fromto(1,4), 2, 9) });
assert_equals({ [4, 3, 9, 1] }, { listset(fromto(4,1), 2, 9) });
assert_equals({ [1, 1, 9] }, { listset(rep(3, 1), 2, 9) });
assert_equals({ [2] }, { listset([1], 0, 2) });
assert_equals({ [1, 2, 4] }, { sublist = take(3, take(4, [1,2,3])); listset(sublist, 2, 4) }); // sublist
assert_equals({ append(count(33), 0) }, { listset(take(34, count(33)), 2, 2) }); // biglist
assert_equals({ [2, 1, 2, 3] }, { listset(flatten([[0,1], [2,3]]), 0, 2) }); // ChainedListPair.update()
assert_equals({ [0, 1, 2, 5] }, { listset(flatten([[0,1], [2,3]]), 3, 5) }); // ChainedListPair.update()
assert_equals({ [0, 1, 5, 3] }, { listset(flatten([[0,1], [2], [3]]), 2, 5) }); // ChainedList.update()
assert_equals({ [0, 1, 6, 3, 4, 5] }, { listset(flatten([[0,1] ,[2,3], [4,5]]), 2, 6) }); // MatrixList.update()
assert_equals({ [3, 9, 7] }, { apply(listset, ([3,5,7], 1, 9)) });

// listsets : (T => ([T], [Int], [T]) -> [T]) = <intrinsic>
assert_equals({ [1, 2, 7, 1, 11] }, { listsets([3,5,7,9,11], [0,1,3], [1,2]) });
assert_equals({ [1, 2, 7, 1, 11] }, { apply(listsets, ([3,5,7,9,11], [0,1,3], [1,2])) });

// part = { vals, f => group(map(vals, f), vals) }
assert_equals({ [true: [1, 2], false: [3, 4, 5]] }, { part([1,2,3,4,5], { gt(3, $0) }); });

// ppart = { vals, f => list:group(pmap(vals, f), vals) }
assert_equals({ [true: [1, 2], false: [3, 4, 5]] }, { ppart([1,2,3,4,5], { gt(3, $0) }); });
assert_equals({ part([1,2,3,4,5], { gt(3, $0) }) }, { ppart([1,2,3,4,5], { gt(3, $0) }); });

// ppartn = { vals, f, n => list:group(list:flatten(pmap(chunks(vals, n), { $0_266_48 => map($0_266_48, f) })), vals) }
assert_equals({ [true: [1, 2], false: [3, 4, 5]] }, { ppartn([1,2,3,4,5], { gt(3, $0) }, 2); });
assert_equals({ part([1,2,3,4,5], { gt(3, $0) }) }, { ppartn([1,2,3,4,5], { gt(3, $0) }, 2); });
assert_equals({ ppart([1,2,3,4,5], { gt(3, $0) }) }, { ppartn([1,2,3,4,5], { gt(3, $0) }, 2); });

// range : (Int, Int) -> [Int] = <intrinsic>
assert_equals({ [1, 2, 3, 4] }, { range(1,4) });
assert_equals({ [-1, 0, 1, 2] }, { range(-1,4) });
assert_equals({ [1, 2, 3, 4] }, { apply(range, (1,4)) });

// repeat = { n, f => map(lang:rep(n, ()), f) }
assert_equals({ [2, 2, 2] }, { repeat(3, { 2 }) });

// remove : (T => ([T], T) -> [T]) = <intrinsic>
assert_equals({ ["a", "c"] }, { remove(["a", "b", "c"], "b") });
assert_equals({ [] }, { remove([], "b") });
assert_equals({ [] }, { remove(["a"], "a") });
assert_equals({ ["a", "c"] }, { apply(remove, (["a", "b", "c"], "b")) });

// rep : (T => (Int, T) -> [T]) = <intrinsic>
assert_equals({ ["cat", "cat", "cat"] }, { rep(3, "cat") });
assert_equals({ ["cat", "cat", "cat"] }, { apply(rep, (3, "cat")) });

// rest : (T => [T] -> [T]) = <intrinsic>
assert_equals({ ["dog", "fish"] }, { rest(["cat", "dog", "fish"]) });
assert_equals({ ["dog", "fish"] }, { apply(rest, ["cat", "dog", "fish"]) });


// reverse = { list => n = size(list); mapll(range(minus(n, 1), neg(n)), list) }
assert_equals({ [3, 2, 1, 0] }, { reverse(count(4)) });
assert_equals({ [-1, -2, -3, -4] }, { reverse([-4,-3,-2,-1]) });
assert_equals({ [2, 3, 1, 4] }, { reverse([4,1,3,2]) });

// rotate = { list, n => if(ge(n, 0), { plus(take(neg(n), list), drop(neg(n), list)) }, { plus(drop(neg(n), list), take(neg(n), list)) }) }
assert_equals({ [4, 0, 1, 2, 3] }, { rotate(count(5), 1) });
assert_equals({ [1, 2, 3, 4, 0] }, { rotate(count(5), -1) });
assert_equals({ [0, 1, 2, 3, 4] }, { rotate(count(5), 0) });

// runlens = { list => drop(1, eachpair(0, { x, y => minus(y, x) }, append(edges(list), size(list)))) }
assert_equals({ [2, 1, 3] }, { runlens([1,1,4,2,2,2]) });

// runs = { list => cut(list, edges(list)) }
assert_equals({ [[1, 1], [4], [2, 2, 2]] }, { runs([1,1,4,2,2,2]) });

// shuffle : (T => [T] -> [T]) = <intrinsic>
// fake this a little, shuffle list will be the same lexical value as original list
assert_equals({ [1, 1, 1] }, { shuffle([1, 1, 1]) });
assert_equals({ [1, 1, 1] }, { apply(shuffle, [1, 1, 1]) });

// size : (T => [T] -> Int) = <intrinsic>
assert_equals({ 3 }, { size(["a", "b", "c"]) });
assert_equals({ 3 }, { apply(size, ["a", "b", "c"]) });

// starts = { (sizes: [Int]) -> [Int] => drop(-1, scan(plus, 0, sizes)) }
assert_equals({ [0, 1, 3] }, { starts([1, 2, 3]) });
assert_equals({ [0, 1, 1] }, { starts([1, 0, 2]) });
assert_equals({ [] }, { starts([]) });

// take : (T => (Int, [T]) -> [T]) = <intrinsic>
assert_equals({ ["a", "b"] }, { take(2, ["a", "b", "c"]) });
assert_equals({ ["b", "c"] }, { take(-2, ["a", "b", "c"]) });
assert_equals({ ["c"] }, { take(-1, ["a", "b", "c"]) });
assert_equals({ [] }, { take(2, []) });
assert_equals({ ["a", "b", "c"] }, { take(3, ["a", "b", "c"]) });
assert_equals({ ["a", "b", "c", "a"] }, { take(4, ["a", "b", "c"]) });
assert_equals({ ["c", "a", "b", "c"] }, { take(-4, ["a", "b", "c"]) });
assert_equals({ ["a", "b"] }, { apply(take, (2, ["a", "b", "c"])) });
assert_equals({ [0, 1, 2] }, { take(3, flatten([[0,1], [2], [3]])) }); // ChainedList.sublist()
assert_equals({ [0, 1, 2, 3] }, { take(4, flatten([[0,1] ,[2,3], [4,5]])) }); // MatrixList.sublist()

// unique : (T => [T] -> [T]) = <intrinsic>
assert_equals({ [1, 2, 3] }, { unique([1,2,2,3,1,2]) });
assert_equals({ [1, 2, 3] }, { apply(unique, [1,2,2,3,1,2]) });

// unzip : (Types.. => [Tup(Types)] -> Tup(List @ Types)) = <intrinsic>
assert_equals({ (["a", "b", "a", "b"], [1, 2, 3, 4], [true, false, true, true]) },
              { unzip([("a", 1, true), ("b", 2, false), ("a", 3, true), ("b", 4, true)]) });
assert_equals({ () },
              { unzip([]) });
assert_equals({ (["a", "b", "a", "b"], [1, 2, 3, 4], [true, false, true, true]) },
              { apply(unzip, ([("a", 1, true), ("b", 2, false), ("a", 3, true), ("b", 4, true)])) });

// where : (T => ([T], T -> Bool) -> [Int]) = <intrinsic>
assert_equals({ [0, 1, 3] }, { where([4,4,7,4,7], {$0 < 5}) });
assert_equals({ [] }, { where([], {$0 < 5}) });
assert_equals({ [0, 1, 3] }, { apply(where, ([4,4,7,4,7], {$0 < 5})) });

// zip : (Types.. => Tup(List @ Types) -> [Tup(Types)]) = <intrinsic>
assert_equals({ [("a", 1), ("b", 2), ("a", 3), ("b", 4)] }, { zip(["a", "b"], [1,2,3,4]) });
assert_equals({ [] }, { zip(["a", "b"], [1,2], []) });
assert_equals({ [("a", 1), ("b", 2), ("a", 3), ("b", 4)] }, { apply(zip, (["a", "b"], [1,2,3,4])) });

// avg = { lst => divz(sum(lst), list:size(lst)) }
assert_equals({ 5 }, { avg([3,3,3,11]) });

// product = { ns => loop:reduce(times, 1, ns) }
assert_equals({ 24 }, { product([2,3,4]) });

// runtot = { (is: [Int]) -> [Int] => drop(1, scan(plus, 0, is)) }
assert_equals({ [1, 3, 6] }, { runtot([1, 2, 3]) });

// sum = { ns => loop:reduce(plus, 0, ns) }
assert_equals({ 6 }, { sum([1,2,3]) });

// favg = { lst => fdivz(fsum(lst), integer:i2f(list:size(lst))) }
assert_equals({ 3.0 }, { favg([2.0,3.0,4.0]) });

// fproduct = { ns => loop:reduce(ftimes, 1.0, ns) }
assert_equals({ 24.0 }, { fproduct([2.0,3.0,4.0]) });

// fruntot = { (fs: [Double]) -> [Double] => drop(1, scan(plus, 0.0, fs)) }
assert_equals({ [1.0, 3.0, 6.0] }, { fruntot([1.0, 2.0, 3.0]) });

// fsum = { ns => loop:reduce(plus, 0.0, ns) }
assert_equals({ 6.0 }, { fsum([1.0, 2.0, 3.0]) });

// any = { vals, pred => evolve_while(not, false, compose(snd, pred), vals) }
assert_equals({ true }, { any([1,2,1], { eq(2, $0) }) });
assert_equals({ false }, { any([3,2,4,3], { eq(1, $0) }) });

// all = { vals, pred => evolve_while(id, true, compose(snd, pred), vals) }
assert_equals({ true }, { all([3,2,4,3], { lt(1, $0) }) });
assert_equals({ false }, { all([3,2,4,3], { gt(1, $0) }) });

// intersection = { list1, list2 => map2 = mapns:assoc(list2, [()]); list:unique(list:filter(list1, { $0_16_36 => mapns:iskey(map2, $0_16_36) })) }
assert_equals({ [2, 4] }, { intersection([2,2,3,4], [5,4,4,2,1]) });

// isperm = { a, b => eq(mapns:counts(a), mapns:counts(b)) }
assert_equals({ true }, { isperm([1,2,3], [3,2,1]) });
assert_equals({ false }, { isperm([1,2,3], [3,3,1]) });
assert_equals({ true }, { isperm(["a", "b", "c"], ["b", "c", "a"]) });
assert_equals({ false }, { isperm(["a", "b", "c"], ["B", "c", "a"]) });


// difference = { list1, list2 => map2 = mapns:assoc(list2, [()]); list:unique(list:filter(list1, { $0_30_36 => not(mapns:iskey(map2, $0_30_36)) })) }
assert_equals({ [2] }, { difference([2,3,4], [3,4,5]) });

// union = { list1, list2 => list:unique(plus(list1, list2)) }
assert_equals({ [0, 1, 2, 3, 4] }, { union([1,1,2,3], [0,2,4]) });

//////////////////////////////////////////////////
// map
//////////////////////////////////////////////////
// assoc : (K, V => ([K], [V]) -> [K : V]) = <intrinsic>
assert_equals({ [1: "a", 2: "b", 3: "c", 4: "d"] }, { assoc([1,2,3,4], ["a", "b", "c", "d"]) });
assert_equals({ [1: "rep1", 2: "rep2", 3: "rep1", 4: "rep2"] }, { assoc([1,2,3,4], ["rep1", "rep2"]) });
assert_equals({ [:] }, { assoc([1,2,3,4], []) });
assert_equals({ [1: "a", 2: "b", 3: "c", 4: "d"] }, { apply(assoc, ([1,2,3,4], ["a", "b", "c", "d"])) });

// entries : (K, V => [K : V] -> [(K, V)]) = <intrinsic>
assert_true({ isperm([(#a, "A"), (#b, "B")], entries([#a:"A", #b:"B"])) });
assert_true({ isperm([(#a, "A"), (#b, "B")], apply(entries, [#a:"A", #b:"B"])) });

// iskey : (K, V => ([K : V], K) -> Bool) = <intrinsic>
assert_true({ iskey([#a:"A", #b:"B"], #a) });
assert_false({ iskey([#a:"A", #b:"B"], #c) });
assert_true({ apply(iskey, ([#a:"A", #b:"B"], #a)) });

// keys : (K, V => [K : V] -> [K]) = <intrinsic>
assert_true({ isperm([#a, #b, #c], keys([#a:"A", #b:"B", #c:"C"])) });
assert_true({ isperm([#a, #b, #c], apply(keys, [#a:"A", #b:"B", #c:"C"])) });

// mapdel : (K, V => ([K : V], K) -> [K : V]) = <intrinsic>
assert_equals({ [#a: "A", #c: "C"] }, { mapdel([#a:"A", #b:"B", #c:"C"], #b) });
assert_equals({ [#a: "A", #c: "C"] }, { apply(mapdel, ([#a:"A", #b:"B", #c:"C"], #b)) });

// mapset : (K, V => ([K : V], K, V) -> [K : V]) = <intrinsic>
assert_equals({ [#a: "A", #b:"B", #c: "C"] }, { mapset([#a:"A", #b:"B"], #c, "C") });
assert_equals({ [#a: "A", #b:"B", #c: "C"] }, { apply(mapset, ([#a:"A", #b:"B"], #c, "C")) });

// mapsets : (K, V => ([K : V], [K], [V]) -> [K : V]) = <intrinsic>
assert_equals({ [#a: "A", #b:"B", #c: "C"] }, { mapsets([#a:"A", #b:"B"], [#c], ["C"]) });
assert_equals({ [#a: "A", #b:"B", #c: "C", #d: "D"] }, { mapsets([#a:"A", #b:"B"], [#c,#d], ["C","D"]) });
assert_equals({ [#a: "A", #b:"B", #c: "C"] }, { mapsets([#a:"A", #b:"B"], [#c], ["C","D"]) });
assert_equals({ [#a: "A", #b:"B", #c: "CD", #d: "CD"] }, { mapsets([#a:"A", #b:"B"], [#c,#d], ["CD"]) });
assert_equals({ [#a: "A", #b:"B", #c: "C"] }, { apply(mapsets, ([#a:"A", #b:"B"], [#c], ["C"])) });

// mplus : (K, V => ([K : V], [K : V]) -> [K : V])
assert_equals({ [#c: "C", #b: "B", #a: "A"] }, { mplus([#a:"A", #b:"B"], [#c:"C"]) });
assert_equals({ [#b: "b", #a: "A"] }, { mplus([#a:"A", #b:"B"], [#b:"b"]) });
assert_equals({ [#c: "C", #b: "B", #a: "A"] }, { apply(mplus, ([#a:"A", #b:"B"], [#c:"C"])) });

// values : (K, V => [K : V] -> [V]) = <intrinsic>
assert_true({ isperm(["A", "B", "C"], values([#a:"A", #b:"B", #c: "C"])) });
assert_true({ isperm(["A", "B", "C"], apply(values, [#a:"A", #b:"B", #c: "C"])) });

// counts = { list => inckey = { map, key => mapset(map, key, plus(1, mapgetd(map, key, 0))) }; loop:reduce(inckey, [:], list) }
assert_equals({ [1: 2, 2: 1, 3: 3] }, { counts([1,2,3,1,3,3]) });

// mapgetd = { map, key, default => guard(not(iskey(map, key)), default, { map[key] }) }
assert_equals({ "charlie" }, { mapgetd(["a":"alpha", "b":"bee"], "c", "charlie") });
assert_equals({ "bee" }, { mapgetd(["a":"alpha", "b":"bee"], "b", "beta") });

// cross = { xs, ys => xn = size(xs); ixs = count(times(xn, size(ys))); zip(mapll(map(ixs, { $0_47_21 => mod($0_47_21, xn) }), xs), mapll(map(ixs, { $0_47_51 => div($0_47_51, xn) }), ys)) }
assert_equals({ [(1, 1), (2, 1), (3, 1)] }, { cross([1,2,3], [1]) });
assert_equals({ [] }, { cross([1,2,3], []) });

// fan = { f, g => { $0_30_13 => (f($0_30_13), g($0_30_13)) } }
assert_equals({ (4,2) }, { x = fan(inc, dec); x(3) });

// fst = <A, B> { p: (A, B) => p.0 }
assert_equals({ "a" }, { fst(("a","b")) });
assert_equals({ 1 }, { fst((1,2)) });


// fuse = { f, g => { $0_39_14, $1_39_14 => (f($0_39_14), g($1_39_14)) } }
assert_equals({ (4, 4) }, { x = fuse(inc, dec); x(3, 5); });
assert_equals({ (4, "a") }, { x = fuse(inc, id); x(3, "a"); });

// snd = <A, B> { p: (A, B) => p.1 }
assert_equals({ "b" }, { snd(("a","b")) });
assert_equals({ 2 }, { snd((1,2)) });

// twin = { v => (v, v) }
assert_equals({ (1, 1) }, { twin(1) });

// box : (T => T -> *T) = <intrinsic>
assert_equals({ *box(2) }, { *box(2) });
assert_equals({ *box(2) }, { *apply(box, 2) });

// boxes : { <Ts:[*]> Tup(Ts) -> Tup(Ts | Box) => <intrinsic> }
assert_equals({ (1, 3, 4) }, { tup = (1, 3, 4); b = boxes(tup); (*b.0, *b.1, *b.2) });
assert_equals({ (1, 3, 4) }, { tup = (1, 3, 4); b = apply(boxes, (tup)); (*b.0, *b.1, *b.2) });



// do : (T => (() -> T) -> T) = <intrinsic>
assert_equals({ true }, { do { intran(); }; });
assert_equals({ true }, { apply(do, { intran(); }); });
// Update more than runtime.tran.Transaction.UPDATED_CHUNK_SIZE boxes in a single transaction
assert_equals({ 66 }, {
                        zero = box(0);
                        one = box(1);
                        two = box(2);
                        three = box(3);
                        four = box(4);
                        five = box(5);
                        six = box(6);
                        seven = box(7);
                        eight = box(8);
                        nine = box(9);
                        ten = box(10);
                        do {
                            zero := 1;
                            zero := 1; // re-hit a cached update
                            one := 2;
                            two := 3;
                            three := 4;
                            four := 5;
                            five := 6;
                            six := 7;
                            seven := 8;
                            eight := 9;
                            nine := 10;
                            ten := 11;
                        };
                        *zero + *one + *two + *three + *four + *five + *six + *seven + *eight + *nine + *ten;
                    });


// Get more than runtime.tran.Transaction.PINNED_CHUNK_SIZE boxes in a single transaction
assert_equals({ 55 }, {
                        zero = box(0);
                        one = box(1);
                        two = box(2);
                        three = box(3);
                        four = box(4);
                        five = box(5);
                        six = box(6);
                        seven = box(7);
                        eight = box(8);
                        nine = box(9);
                        ten = box(10);
                        sum = box(0);
                        do {
                            sum := *zero + *one + *two + *three + *four + *five + *six + *seven + *eight + *nine + *ten + *zero;
                        };
                        *sum;
                    });

// Gets more than runtime.tran.Transaction.PINNED_CHUNK_SIZE boxes in a single transaction
assert_equals({ (0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0) }, {
                        zero = box(0);
                        one = box(1);
                        two = box(2);
                        three = box(3);
                        four = box(4);
                        five = box(5);
                        six = box(6);
                        seven = box(7);
                        eight = box(8);
                        nine = box(9);
                        ten = box(10);
                        sum = box(0);
                        do {
                            **(zero, one, two, three, four, five, six, seven, eight, nine, ten, zero);
                        };
                        });


// get : (T => *T -> T) = <intrinsic>
assert_equals({ 12 }, { a = box(12); get(a) });
assert_equals({ 12 }, { a = box(12); *a });
assert_equals({ 12 }, { a = box(12); apply(get, a) });

// gets : (Vals.. => Tup(Box @ Vals) -> Tup(Vals)) = <intrinsic>
assert_equals({ (12, 3) }, { a = box(12); b = box(3); gets((a, b)) });
assert_equals({ (12, 3) }, { a = box(12); b = box(3); **(a, b) });
assert_equals({ (12, 3) }, { a = box(12); b = box(3); apply(gets, ((a, b))) });

// intran : () -> Bool = <intrinsic>
assert_equals({ true }, { do { intran(); }; });
assert_equals({ false }, { intran(); });
assert_equals({ true }, { do { apply(intran, ()); }; });

// own : (T => *T -> T) = <intrinsic>
assert_equals({ "abc" }, {
                        done = box(false);
                        b = box("a");
                        // Spawn a thread that will update an owned box after sleeping
                        spawn { do{ own(b); sleep(1000); b <- {$0 + "b"}; done := true; } };
                        // Need to sleep a little here so that the thread has time to spawn
                        // and own the box "b".
                        sleep(50);
                        b <- {$0 + "c"};
                        await(done, { eq(true, $0) });
                        *b;
                        });
assert_equals({ "abc" }, {
                        done = box(false);
                        b = box("a");
                        // Spawn a thread that will update an owned box after sleeping
                        spawn { do{ own(b); sleep(1000); own(b); b <- {$0 + "b"}; done := true; } };
                        // Need to sleep a little here so that the thread has time to spawn
                        // and own the box "b".
                        sleep(50);
                        b <- {$0 + "c"};
                        await(done, { eq(true, $0) });
                        *b;
                        });
assert_equals({ "abc" }, {
                        done = box(false);
                        b = box("a");
                        // Spawn a thread that will update an owned box after sleeping
                        spawn { do{ apply(own, b); sleep(1000); b <- {$0 + "b"}; done := true; } };
                        // Need to sleep a little here so that the thread has time to spawn
                        // and own the box "b".
                        sleep(50);
                        b <- {$0 + "c"};
                        await(done, { eq(true, $0) });
                        *b;
                        });

// owns : (Vals.. => Tup(Box @ Vals) -> Tup(Vals)) = <intrinsic>
assert_equals({ ("abc", "123") }, {
                        done = box(false);
                        box1 = box("a");
                        box2 = box("1");
                        // Spawn a thread that will update an owned box after sleeping
                        spawn { do{ owns((box1, box2)); sleep(1000); box1 <- {$0 + "b"}; box2 <- {$0 + "2"}; done := true; } };
                        // Need to sleep a little here so that the thread has time to spawn
                        // and own the boxes.
                        sleep(50);
                        box1 <- {$0 + "c"};
                        box2 <- {$0 + "3"};
                        await(done, { eq(true, $0) });
                        (*box1, *box2);
                        });
assert_equals({ ("abc", "123") }, {
                        done = box(false);
                        box1 = box("a");
                        box2 = box("1");
                        // Spawn a thread that will update an owned box after sleeping
                        spawn { do{ apply(owns, ((box1, box2))); sleep(1000); box1 <- {$0 + "b"}; box2 <- {$0 + "2"}; done := true; } };
                        // Need to sleep a little here so that the thread has time to spawn
                        // and own the boxes.
                        sleep(50);
                        box1 <- {$0 + "c"};
                        box2 <- {$0 + "3"};
                        await(done, { eq(true, $0) });
                        (*box1, *box2);
                        });

// put : (T => (*T, T) -> ()) = <intrinsic>
assert_equals({ 22 }, {
                    data1 = box(1);
                    put(data1, 22);
                    *data1;
                    });
assert_equals({ 22 }, {
                    data1 = box(1);
                    data1 := 22;
                    *data1;
                    });
assert_equals({ 22 }, {
                    data1 = box(1);
                    apply(put, (data1, 22));
                    *data1;
                    });

// puts : (T.. => (Tup(Box @ T), Tup(T)) -> ()) = <intrinsic>
assert_equals({ (11, 22) }, {
                    data1 = box(1);
                    data2 = box(2);
                    puts((data1, data2), (11, 22));
                    (*data1, *data2);
                    });
assert_equals({ (11, 22) }, {
                    data1 = box(1);
                    data2 = box(2);
                    (data1, data2) ::= (11, 22);
                    (*data1, *data2);
                    });
assert_equals({ (11, 22) }, {
                    data1 = box(1);
                    data2 = box(2);
                    apply(puts, ((data1, data2), (11, 22)));
                    (*data1, *data2);
                    });

// snap : (T => *T -> T) = <intrinsic>
assert_equals({ 1 }, {
                      data1 = box(1);
                      x = snap(data1);
                      data1 <- inc;
                      x; // x should be 1 not 2
                      });

// Snap a value when already in a transaction
assert_equals({ 1 }, {
                      data1 = box(1);
                      do {
                        x = snap(data1);
                        data1 <- inc;
                        x; // x should be 1 not 2
                      };
                      });

assert_equals({ 1 }, {
                      data1 = box(1);
                      x = apply(snap, data1);
                      data1 <- inc;
                      x; // x should be 1 not 2
                      });

// snaps : (Vals.. => Tup(Box @ Vals) -> Tup(Vals)) = <intrinsic>
assert_equals({ (1, 2) }, {
                        data1 = box(1);
                        data2 = box(2);
                        x = snaps(data1, data2);
                        data1 <- inc;
                        data2 <- inc;
                        x; // x should be 1 not 2
                        });
assert_equals({ (1, 2) }, {
                        data1 = box(1);
                        data2 = box(2);
                        x = apply(snaps, (data1, data2));
                        data1 <- inc;
                        data2 <- inc;
                        x; // x should be 1 not 2
                        });
assert_equals({ (1, 2) }, {
                        data1 = box(1);
                        data2 = box(2);
                        do {
                            x = snaps(data1, data2);
                            data1 <- inc;
                            data2 <- inc;
                            x; // x should be 1 not 2
                        };
                        });


// transfer : (B, A => (*B, A -> B, *A) -> ()) = <intrinsic>
assert_equals({ 2 }, {
                    source = box(1);
                    target = box(9);
                    transfer(target, inc, source);
                    *target;
                });
assert_equals({ 2 }, {
                    source = box(1);
                    target = box(9);
                    apply(transfer, (target, inc, source));
                    *target;
                });
assert_equals({ 2 }, {
                    source = box(1);
                    target = box(9);
                    do { transfer(target, inc, source); };
                    *target;
                });

// transfers : (Outs.., Ins.. => (Tup(Box @ Outs), Tup(Ins) -> Tup(Outs), Tup(Box @ Ins)) -> ()) = <intrinsic>
assert_equals({ (2, 3) }, {
                            source1 = box(1);
                            source2 = box(2);
                            target1 = box(9);
                            target2 = box(10);
                            transfers((target1, target2), { a, b => ( a+ 1, b + 1) }, (source1, source2));
                            (*target1, *target2);
                            });
assert_equals({ (2, 3) }, {
                            source1 = box(1);
                            source2 = box(2);
                            target1 = box(9);
                            target2 = box(10);
                            apply(transfers, ((target1, target2), { a, b => ( a+ 1, b + 1) }, (source1, source2)));
                            (*target1, *target2);
                            });
assert_equals({ (2, 3) }, {
                            source1 = box(1);
                            source2 = box(2);
                            target1 = box(9);
                            target2 = box(10);
                            do { 
                                transfers((target1, target2), { a, b => ( a+ 1, b + 1) }, (source1, source2));
                                (*target1, *target2);
                            }
                            });

// update : (T => (*T, T -> T) -> ()) = <intrinsic>
assert_equals({ 1 }, { data = box(0); update(data, inc); *data; });
assert_equals({ 1 }, { data = box(0); data <- inc; *data; });
assert_equals({ 1 }, { data = box(0); apply(update, (data, inc)); *data; });

// updates : (T.. => (Tup(Box @ T), Tup(T) -> Tup(T)) -> ()) = <intrinsic>
assert_equals({ (2, 3) }, {
                    data1 = box(1);
                    data2 = box(2);
                    foo(a, b)
                    {
                        (inc(a),  inc(b) )
                    };
                    updates((data1, data2), foo );
                    (*data1, *data2);
                    });
assert_equals({ (2, 3) }, {
                    data1 = box(1);
                    data2 = box(2);
                    foo(a, b)
                    {
                        (inc(a),  inc(b) )
                    };
                    (data1, data2) <<- foo;
                    (*data1, *data2);
                    });
assert_equals({ (2, 3) }, {
                    data1 = box(1);
                    data2 = box(2);
                    foo(a, b)
                    {
                        (inc(a),  inc(b) )
                    };
                    do { updates((data1, data2), foo ); };
                    (*data1, *data2);
                    });
assert_equals({ (2, 3) }, {
                    data1 = box(1);
                    data2 = box(2);
                    foo(a, b)
                    {
                        (inc(a),  inc(b) )
                    };
                    apply(updates, ((data1, data2), foo ));
                    (*data1, *data2);
                    });

// getput = { b, v => act(b, { $0_103_24 => (v, $0_103_24) }) }
assert_equals({ 2 }, { x = box(2); getput(x, 12) });
assert_equals({ 12 }, { x = box(2); getput(x, 12); *x; });

// preupdate : { b, f => act(b, { $0_269_26 => v = f($0_269_26); (v, v) }) }
assert_equals({ (2, 1, 1) }, { x = box(2); (*x, preupdate(x, dec), *x) });

// postupdate = { b, f => act(b, { $0_108_27 => (f($0_108_27), $0_108_27) }) }
assert_equals({ 2 }, { x = box(2); postupdate(x, inc) });
assert_equals({ 3 }, { x = box(2); postupdate(x, inc); *x; });

// preinc = { b => preupdate(b, inc) }
assert_equals({ (2, 3, 3) }, { x = box(2); (*x, preinc(x), *x) });

// predec = { b => preupdate(b, dec) }
assert_equals({ (2, 1, 1) }, { x = box(2); (*x, predec(x), *x) });

// postinc = { b => postupdate(b, inc) }
assert_equals({ (2, 2, 3) }, { x = box(2); (*x, postinc(x), *x) });

// postdec = { b => postupdate(b, dec) }
assert_equals({ (2, 2, 1) }, { x = box(2); (*x, postdec(x), *x) });

// dep = { src, f => sink = box(f(get(src))); react(src, { $0_290_16 => put(sink, f($0_290_16)); () }); sink }
assert_equals({ 1 }, {
                    src = box(0);
                    track = dep(src, id);
                    src <- inc;
                    *track;
                    });

// async = { f, cb => spawn({ cb(f()) }); () }
// TODO

// availprocs : () -> Int = <intrinsic>
assert_equals({ true }, { ge(availprocs(), 1) });
assert_equals({ true }, { ge(apply(availprocs, ()), 1) });

// future : (X => (() -> X) -> (() -> X)) = <intrinsic>
// TODO

// sleep : Int -> () = <intrinsic>
assert_equals({ true }, { sleep(3); true });
assert_equals({ true }, { apply(sleep, 3); true });

// spawn : (T => (() -> T) -> ()) = <intrinsic>
assert_equals({ 5 }, {
                    spawndata = box(0);
                    spawn { while({ *spawndata < 5 }, { sleep(100); spawndata <- inc }); };
                    await(spawndata, { $0 == 5 });
                    *spawndata;
                    });

// taskid : () -> Long = <intrinsic>
assert_equals({ 1L }, { taskid() });
assert_equals({ 1L }, { apply(taskid, ()) });

// await : (T => (*T, T -> Bool) -> ()) = <intrinsic>
assert_equals({ 5 }, {
                    awaitdata = box(0);
                    spawn { while({ *awaitdata < 5 }, { sleep(100); awaitdata <- inc }); };
                    await(awaitdata, { $0 == 5 });
                    *awaitdata;
                    });
/* FIXME: This test does not work properly
assert_equals({ 5 }, {
                    awaitdata = box(0);
                    spawn { while({ *awaitdata < 5 }, { sleep(100); awaitdata <- inc }); };
                    do { await(awaitdata, { $0 == 5 }) };
                    *awaitdata;
                    });
*/
assert_equals({ 5 }, {
                    awaitdata = box(0);
                    spawn { while({ *awaitdata < 5 }, { sleep(100); awaitdata <- inc }); };
                    apply(await, (awaitdata, { $0 == 5 }));
                    *awaitdata;
                    });

// awaits : (T.. => (Tup(Box @ T), Tup(T) -> Bool) -> ()) = <intrinsic>
assert_equals({ 10 }, {
                    awaitdata1 = box(0);
                    awaitdata2 = box(0);

                    spawn { while({ *awaitdata1 < 5 }, { sleep(rand(100)); awaitdata1 <- inc }); };
                    spawn { while({ *awaitdata2 < 5 }, { sleep(rand(100)); awaitdata2 <- inc }); };

                    awaits( (awaitdata1, awaitdata2), {
                                        a, b => a+b == 10;
                                        });
                    *awaitdata1 + *awaitdata2;
                    });
/* FIXME: This test does not work properly
assert_equals({ 10 }, {
                    awaitdata1 = box(0);
                    awaitdata2 = box(0);

                    spawn { while({ *awaitdata1 < 5 }, { sleep(rand(100)); awaitdata1 <- inc }); };
                    spawn { while({ *awaitdata2 < 5 }, { sleep(rand(100)); awaitdata2 <- inc }); };

                    do {
                        awaits( (awaitdata1, awaitdata2), {
                                        a, b => a+b == 10;
                                        });
                        };
                    *awaitdata1 + *awaitdata2;
                    });
*/
assert_equals({ 10 }, {
                    awaitdata1 = box(0);
                    awaitdata2 = box(0);

                    spawn { while({ *awaitdata1 < 5 }, { sleep(rand(100)); awaitdata1 <- inc }); };
                    spawn { while({ *awaitdata2 < 5 }, { sleep(rand(100)); awaitdata2 <- inc }); };

                    apply(awaits,   (
                                    (awaitdata1, awaitdata2), {
                                        a, b => a+b == 10;
                                        })
                                    );
                    *awaitdata1 + *awaitdata2;
                    });

// react : <T, X> (*T, T -> X) -> (T -> X)
assert_equals({ 2 }, { 
                    status = box(0);
                    stat(v) { put(status, v) };
                    f = box(0);
                    w = react(f, stat);
                    f <- inc;
                    f <- inc;
                    unreact(f, w);
                    *status;
                     });
assert_equals({ 2 }, {
                    status = box(0);
                    stat(v) { put(status, v) };
                    f = box(0);
                    w = apply(react, (f, stat));
                    f <- inc;
                    f <- inc;
                    *status;
                });

// unreact : <T, X> (*T, T -> X) -> *T
assert_equals({ 2 }, {
                    status = box(0);
                    stat(v) { put(status, v) };
                    f = box(0);
                    w = react(f, stat);
                    f <- inc;
                    f <- inc;
                    unreact(f, w);
                    f <- inc;
                    *status;
                });
assert_equals({ 2 }, {
                    status = box(0);
                    stat(v) { put(status, v) };
                    f = box(0);
                    w = react(f, stat);
                    f <- inc;
                    f <- inc;
                    apply(unreact, (f, w));
                    f <- inc;
                    *status;
                });


// assert : (Bool, String) -> () = <intrinsic>
assert(true, "no error");

// frand : () -> Double = <intrinsic>
assert_true({ flt(frand(), 1.0) });
assert_true({ flt(apply(frand, ()), 1.0) });

// millitime : () -> Long = <intrinsic>
assert_equals({ true }, { fge(l2f(millitime()), 1.0) });
assert_equals({ true }, { fge(l2f(apply(millitime, ())), 1.0) });

// nanotime : () -> Long = <intrinsic>
assert_equals({ true }, { fge(l2f(nanotime()), 1.0) });
assert_equals({ true }, { fge(l2f(apply(nanotime, ())), 1.0) });

// print : (T => T -> ()) = <intrinsic>
assert_true({ apply(print, ("hello")); true });

// printstr : String -> () = <intrinsic>
assert_true({ apply(printstr, ("hello")); true });

// rand : Int -> Int = <intrinsic>
assert_equals({ 0 }, { rand(1) });
assert_equals({ 0 }, { apply(rand, 1) });

// -------------------------------------------

// utterly minimal file i/o--waiting for variants
// appendfile : (String, String) -> Bool = <intrinsic>
// TODO
// readfile : String -> String = <intrinsic>
// TODO
// writefile : (String, String) -> Bool = <intrinsic>
// TODO


// XML parsing--ditto
// parsexml : String -> XNode = <intrinsic>
// TODO

// primitive server sockets, used in tests/demos
// accept : (ServerSocket, String -> String) -> () = <intrinsic>
// TODO
// close : ServerSocket -> () = <intrinsic>
// TODO
// closed : ServerSocket -> Bool = <intrinsic>
// TODO
// ssocket : Int -> ServerSocket = <intrinsic>
// TODO

// primitive http, used in tests/demos
// httpget : String -> String = <intrinsic>
// TODO
// httphead : String -> ?(true: [String], false: String) = <intrinsic>
// TODO


// simple array hookup, used in some interop tests
// array : { <T> (Int, T) -> Array(T) => <intrinsic> }
assert_equals({ (true, true, true) }, { a = array(3, true); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Boolean
assert_equals({ (2, 2, 2) }, { a = array(3, 2); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Int
assert_equals({ (2L, 2L, 2L) }, { a = array(3, 2L); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Long
assert_equals({ (2.1, 2.1, 2.1) }, { a = array(3, 2.1); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Double
assert_equals({ ((1, 2), (1, 2), (1, 2)) }, { a = array(3, (1,2)); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // Object
assert_equals({ (2, 2, 2) }, { a = apply(array, (3, 2)); (aget(a, 0), aget(a, 1), aget(a, 2)) }); // apply


// aget : { <T> (Array(T), Int) -> T => <intrinsic> }
assert_equals({ true }, { a = array(1, true); aget(a, 0) }); // Boolean
assert_equals({ 2 }, { a = array(1, 2); aget(a, 0) }); // Int
assert_equals({ 2L }, { a = array(1, 2L); aget(a, 0) }); // Long
assert_equals({ 2.1 }, { a = array(1, 2.1); aget(a, 0) }); // Double
assert_equals({ (1, 2) }, { a = array(1, (1,2)); aget(a, 0) }); // Object
assert_equals({ 2 }, { a = array(1, 2); apply(aget, (a, 0)) }); // apply

// aset : { <T> (Array(T), Int, T) -> Array(T) => <intrinsic> }
assert_equals({ false }, { a = array(1, true); aset(a, 0, false); aget(a, 0) }); // Boolean
assert_equals({ 1 }, { a = array(1, 2); aset(a, 0, 1); aget(a, 0) }); // Int
assert_equals({ 1L }, { a = array(1, 2L); aset(a, 0, 1L); aget(a, 0) }); // Long
assert_equals({ 1.1 }, { a = array(1, 2.1); aset(a, 0, 1.1); aget(a, 0) }); // Double
assert_equals({ (0, 0) }, { a = array(1, (1,2)); aset(a, 0, (0,0)); aget(a, 0) }); // Object
assert_equals({ 1 }, { a = array(1, 2); apply(aset, (a, 0, 1)); aget(a, 0) }); // apply

// alen : { <T> Array(T) -> Int => <intrinsic> }
assert_equals({ 1 }, { a = array(1, true); alen(a) }); // Boolean
assert_equals({ 1 }, { a = array(1, 2); alen(a) }); // Int
assert_equals({ 1 }, { a = array(1, 2L); alen(a) }); // Long
assert_equals({ 1 }, { a = array(1, 2.1); alen(a) }); // Double
assert_equals({ 1 }, { a = array(1, (1,2)); alen(a) }); // Object
assert_equals({ 1 }, { a = array(1, 2); apply(alen, (a)) }); // apply
