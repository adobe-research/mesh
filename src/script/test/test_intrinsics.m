import * from std;
import * from unittest;

/* INTRINSICS */

//////////////////////////////////////////////////
// logic
//////////////////////////////////////////////////
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

// not : Bool -> Bool = <intrinsic>
assert_false({ not(true) });
assert_true({ not(false) });


//////////////////////////////////////////////////
// int relops
//////////////////////////////////////////////////
//eq : (T => (T, T) -> Bool) = <intrinsic>
assert_true({ eq(2, 2) });
assert_false({ eq(2, 1) });

//ne : (T => (T, T) -> Bool) = <intrinsic>
assert_true({ ne(1, 2) });
assert_false({ ne(1, 1) });

// gt : (Int, Int) -> Bool = <intrinsic>
assert_true({ gt(10, 9) });
assert_false({ gt(8, 9) });

// ge : (Int, Int) -> Bool = <intrinsic>
assert_true({ ge(10, 9) });
assert_true({ ge(9, 9) });

// lt : (Int, Int) -> Bool = <intrinsic>
assert_false({ lt(10, 9) });
assert_true({ lt(8, 9) });

// le : (Int, Int) -> Bool = <intrinsic>
assert_false({ le(10, 9) });
assert_true({ le(9, 9) });


//////////////////////////////////////////////////
// int arith ops
//////////////////////////////////////////////////
// plus : (Int, Int) -> Int = <intrinsic>
assert_equals({ 7 }, { plus(4, 3) });

// minus : (Int, Int) -> Int = <intrinsic>
assert_equals({ 1 }, { minus(4, 3) });

// times : (Int, Int) -> Int = <intrinsic>
assert_equals({ 12 }, { times(3, 4) });

// div : (Int, Int) -> Int = <intrinsic>
assert_equals({ 2 }, { div(10, 5) });

// max : (Int, Int) -> Int = <intrinsic>
assert_equals({ 4 }, { max(3, 4) });
assert_equals({ 4 }, { max(4, 3) });
assert_equals({ 4 }, { max(4, 4) });

// min : (Int, Int) -> Int = <intrinsic>
assert_equals({ 3 }, { min(3, 4) });
assert_equals({ 3 }, { min(4, 3) });
assert_equals({ 4 }, { min(4, 4) });

// mod : (Int, Int) -> Int = <intrinsic>
assert_equals({ 1 }, { mod(13, 3) });

// neg : Int -> Int = <intrinsic>
assert_equals({ -1 }, { neg(1) });
assert_equals({ 1 }, { neg(-1) });

// pow : (Int, Int) -> Int = <intrinsic>
assert_equals({ 16 }, { pow(2, 4) });

// sign : Int -> Int = <intrinsic>
assert_equals({ 0 }, { sign(0) });
assert_equals({ -1 }, { sign(-2) });
assert_equals({ 1 }, { sign(2) });


//////////////////////////////////////////////////
// int bitwise ops
//////////////////////////////////////////////////
// band : (Int, Int) -> Int = <intrinsic>
assert_equals({ 1 }, { band(5, 3) });

// bor : (Int, Int) -> Int = <intrinsic>
assert_equals({ 7 }, { bor(5, 3) });

//////////////////////////////////////////////////
// long arith ops
//////////////////////////////////////////////////
// lminus : (Long, Long) -> Long = <intrinsic>
assert_equals({ 1L }, { lminus(4L, 3L) });



//////////////////////////////////////////////////
// fp relops
//////////////////////////////////////////////////
// fgt : (Double, Double) -> Bool = <intrinsic>
assert_true({ fgt(10.0, 9.8) });
assert_false({ fgt(9.7, 9.8) });

// fge : (Double, Double) -> Bool = <intrinsic>
assert_true({ fge(10.0, 9.8) });
assert_true({ fge(9.8, 9.8) });

// flt : (Double, Double) -> Bool = <intrinsic>
assert_false({ flt(10.0, 9.8) });
assert_true({ flt(9.7, 9.8) });

// fle : (Double, Double) -> Bool = <intrinsic>
assert_false({ fle(10.0, 9.8) });
assert_true({ fle(9.8, 9.8) });


//////////////////////////////////////////////////
// fp arith ops
//////////////////////////////////////////////////
// fdiv : (Double, Double) -> Double = <intrinsic>
assert_equals({ 6.6 }, { fdiv(8.25, 1.25) });

// fminus : (Double, Double) -> Double = <intrinsic>
assert_equals({ 6.925 }, { fminus(10.125, 3.2) });

// fmod : (Double, Double) -> Double = <intrinsic>
assert_equals({ 0.125 }, { fmod(12.125, 4.0) });

// fneg : (Double, Double) -> Double = <intrinsic>
assert_equals({ -1.125 }, { fneg(1.125) });
assert_equals({ 1.125 }, { fneg(-1.125) });

// fpow : (Double, Double) -> Double = <intrinsic>
assert_equals({ 27.0 }, { fpow(9.0, 1.5) });

//ftimes : (Double, Double) -> Double = <intrinsic>
assert_equals({ 10.15625 }, { ftimes(8.125, 1.25) });




//////////////////////////////////////////////////
// other math
//////////////////////////////////////////////////
// atan2 : (Double, Double) -> Double = <intrinsic>
assert_equals({ 0.0 }, { atan2(0.0, 0.0) });

// cos : Double -> Double = <intrinsic>
assert_equals({ 1.0 }, { cos(0.0) });

// draw : (Int, Int) -> [Int] = <intrinsic>
assert_equals({ [0, 0, 0] }, { draw(3, 1) });

// exp : Double -> Double = <intrinsic>
assert_equals({ 7.38905609893065 }, { exp(2.0) });

// frand : () -> Double = <intrinsic>
assert_true({ flt(frand(), 1.0) });

// ln : Double -> Double = <intrinsic>
assert_equals({ 0.0 }, { ln(1.0) });

// rand : Int -> Int = <intrinsic>
assert_equals({ 0 }, { rand(1) });

// sin : Double -> Double = <intrinsic>
assert_equals({ 0.0 }, { sin(0.0) });
assert_equals({ 1.0 }, { sin(1.570796326795) });

// sqrt : Double -> Double = <intrinsic>
assert_equals({ 3.0 }, { sqrt(9.0) });

// tan : Double -> Double = <intrinsic>
assert_equals({ 0.0 }, { tan(0.0) });
assert_equals({ 1.5574077246549023 }, { tan(1.0) });



//////////////////////////////////////////////////
// string
//////////////////////////////////////////////////
// endswith : (String, String) -> Bool = <intrinsic>
assert_true({ endswith("hello world", "world") });

// startswith : (String, String) -> Bool = <intrinsic>
assert_true({ startswith("hello world", "hello") });

// strcat : [String] -> String = <intrinsic>
assert_equals({ "hello world" }, { strcat(["hello", " ", "world"]) });

// strcmp : (String, String) -> Int = <intrinsic>
assert_equals({ -1 }, { strcmp("a", "b") });
assert_equals({ 2 }, { strcmp("c", "a") });

// strcut : (String, [Int]) -> [String] = <intrinsic>
assert_equals({ ["ab", "cd", "ef", "gh", "ij"] }, { strcut("abcdefghij", [0,2,4,6,8]) });

// strdrop : (Int, String) -> String = <intrinsic>
assert_equals({ "efghij" }, { strdrop(4, "abcdefghij") });

// strfind : (String, String) -> Int = <intrinsic>
assert_equals({ 2 }, { strfind("abcdef", "cd") });

// strjoin : ([String], String) -> String = <intrinsic>
assert_equals({ "hi,there,neighbor" }, { strjoin(["hi", "there", "neighbor"], ",") });

// strlen : String -> Int = <intrinsic>
assert_equals({ 11 }, { strlen("hello world") });

// strsplit : (String, String) -> [String] = <intrinsic>
assert_equals({ ["hi", "there", "neighbor"] }, { strsplit("hi,there,neighbor", ",") });

// strtake : (Int, String) -> String = <intrinsic>
assert_equals({ "hello" }, { strtake(5, "hello world") });
assert_equals({ "world" }, { strtake(-5, "hello world") });
assert_equals({ "hello worldhello" }, { strtake(16, "hello world") });

// strwhere : (String, String -> Bool) -> [Int] = <intrinsic>
assert_equals({ [3,7,10,12] }, { strwhere("abcZdefZghZiZjk", { $0 == "Z" }) });

// substr : (String, Int, Int) -> String = <intrinsic>
assert_equals({ "lo w" }, { substr("hello world", 3, 4) });

// tolower : String -> String = <intrinsic>
assert_equals({ "hello world" }, { tolower("HelLo WoRLd") });


//toupper : String -> String = <intrinsic>
assert_equals({ "HELLO WORLD" }, { toupper("HelLo WoRLd") });


//////////////////////////////////////////////////
// converters
//////////////////////////////////////////////////
// b2i : Int -> Bool = <intrinsic>
assert_equals({ 1 }, { b2i(true) });
assert_equals({ 0 }, { b2i(false) });

// f2i : Double -> Int = <intrinsic>
assert_equals({ 1 }, { f2i(1.8) });
assert_equals({ -2 }, { f2i(-1.8) });
assert_equals({ 0 }, { f2i(-0.0) });

// f2s : Double -> String = <intrinsic>
assert_equals({ "3.12" }, { f2s(3.12) });

// i2b : Int -> Bool = <intrinsic>
assert_true({ i2b(1) });
assert_false({ i2b(0) });
assert_true({ i2b(2) });
assert_true({ i2b(-3) });
assert_false({ i2b(-0) });

// i2f : Int -> Double = <intrinsic>
assert_equals({ 1.0 }, { i2f(1) });
assert_equals({ 0.0 }, { i2f(-0) });
assert_equals({ 0.0 }, { i2f(0) });
assert_equals({ -3.0 }, { i2f(-3) });

// i2s : Int -> String = <intrinsic>
assert_equals({ "1" }, { i2s(1) });

// l2f : Long -> Double = <intrinsic>
assert_equals({ 5.0 }, { l2f(5L) });

// l2i : Long -> Int = <intrinsic>
assert_equals({ 5 }, { l2i(5L) });

// l2s : Long -> String = <intrinsic>
assert_equals({ "5" }, { l2s(5L) });

// s2f : String -> Double = <intrinsic>
assert_equals({ 1.0 }, { s2f("1") });

// s2i : String -> Int = <intrinsic>
assert_equals({ 1 }, { s2i("1") });

// s2l : String -> Long = <intrinsic>
assert_equals({ 5L }, { s2l("5") });

// s2sym : String -> Symbol = <intrinsic>
// sym2s : Symbol -> String = <intrinsic>


//tostr : (T => T -> String) = <intrinsic>
assert_equals({ "\"String\"" }, { tostr("String") });       // string
assert_equals({ "[#a: 2]" }, { tostr([#a:2]) });            // symbol, this will hit Symbol after map
assert_equals({ "[1, 2, 3]" }, { tostr([1,2,3]) });         // list
assert_equals({ "[#a: 2]" }, { tostr([#a:2]) });            // map
assert_equals({ "(1, \"b\", 3)" }, { tostr((1, "b", 3)) }); // tuple
assert_equals({ "(#a: 2)" }, { tostr((#a:2)) });            // record
// variant
assert_equals({ "box(3)" }, { tostr(box(3)) });             // box





//////////////////////////////////////////////////
// map/compose
//////////////////////////////////////////////////
// compl : <X, Y> (X -> Int, [Y]) -> (X -> Y)
assert_equals({ "odd" },
              {
              compl_func = compl({ $0 % 2 }, ["even", "odd"]);
              compl_func(7)
              });

// compm : <X, K, Y> (X -> K, [K : Y]) -> (X -> Y)
assert_equals({ "ODD" },
              {
              compm_func = compm({ if(eq(0, $0 % 2), {#even}, {#odd}) }, [#even: "EVEN", #odd: "ODD"]);
              compm_func(7)
              });

// eachleft : <A, B, C> ((A, B) -> C) -> (([A], B) -> [C])
assert_equals({ [5, 6, 7] }, { [0, 1, 2] ~+ 5 });
assert_equals({ [5, 6, 7] }, { eachleft(plus)([0, 1, 2], 5) });
assert_equals({ [[5, 6], [7, 8]] }, { [[0, 1], [2, 3]] ~~+ 5 });
assert_equals({ [[5, 6], [7, 8]] }, { eachleft(eachleft(plus))([[0, 1], [2, 3]], 5) });
assert_equals({ [5, 6, 7] }, { eachleft(plus)([0, 1, 2], 5) });
assert_equals({ [[10, 9, 8], [10, 9, 8], [10, 9, 8]] }, { [10, 10, 10] ~(-~) [0, 1, 2] });
assert_equals({ [[10, 9, 8], [10, 9, 8], [10, 9, 8]] }, { eachleft(eachright(minus))([10, 10, 10], [0, 1, 2]) });

// eachright : <A, B, C> ((A, B) -> C) -> ((A, [B]) -> [C])
assert_equals({ [5, 6, 7] }, { 5 +~ [0, 1, 2] });
assert_equals({ [5, 6, 7] }, { eachright(plus)(5, [0, 1, 2]) });
assert_equals({ [[5, 6], [7, 8]] }, { 5 +~~ [[0, 1], [2, 3]] });
assert_equals({ [[5, 6], [7, 8]] }, { eachright(eachright(plus))(5, [[0, 1], [2, 3]]) });
assert_equals({ [[10, 10, 10], [9, 9, 9], [8, 8, 8]] }, { [10, 10, 10] ~-~ [0, 1, 2] });
assert_equals({ [[10, 10, 10], [9, 9, 9], [8, 8, 8]] }, { [10, 10, 10] (~-)~ [0, 1, 2] });
assert_equals({ [[10, 10, 10], [9, 9, 9], [8, 8, 8]] }, { eachright(eachleft(minus))([10, 10, 10], [0, 1, 2]) });

// map : <X, Y> ([X], X -> Y) -> [Y]
assert_equals({ [2,3,4] }, { map([1,2,3], inc) });
assert_equals({ [2,3,4] }, { [1,2,3] | inc });

// mapll : <T> ([Int], [T]) -> [T]
assert_equals({ ["zero", "two"] }, { mapll([0,2], ["zero", "one", "two"]) });

// maplm : <K, V> ([K], [K : V]) -> [V]
assert_equals({ ["ZERO", "TWO"] }, { maplm([#zero, #two], [#zero:"ZERO", #one:"ONE", #two:"TWO"]) });

// mapmf : <X, Y, Z> ([X : Y], Y -> Z) -> [X : Z]
assert_equals({ [#a: 4, #b:6] }, { mapmf([#a: (2, 2), #b: (3, 3)], (+)) });

// mapml : <K, V> ([K : Int], [V]) -> [K : V]
assert_equals({ [#a: "Zero", #c: "Two"] }, { mapml([#a: 0, #c: 2], ["Zero", "One", "Two"]) });

// mapmm : <X, Y, Z> ([X : Y], [Y : Z]) -> [X : Z]
assert_equals({ [#a: "One", #b: "Two"] }, { mapmm([#a: 1, #b: 2], [1: "One", 2: "Two"]) });

// mapz : <T.., X> (Tup(List @ T), Tup(T) -> X) -> [X]
assert_equals({ [6, 8, 8, 10] }, { mapz(([5, 6], [1,2,3,4]) , (+)) });



//////////////////////////////////////////////////
// other h/o
//////////////////////////////////////////////////
// cond : (T, V.., R => (Sum(Assoc(T, V)), Rec(Assoc(T, (v => v -> R) @ V))) -> R) = <intrinsic>

// cycle : (T => (T -> Bool, T, T -> T) -> T) = <intrinsic>
assert_equals({ 4 }, { cycle({ $0 < 4 }, 0, inc) });

// cyclen : (T => (Int, T, T -> T) -> T) = <intrinsic>
assert_equals({ 4 }, { cyclen(4, 0, inc) });

// evolve_while : (A, B => (A -> Bool, A, (A, B) -> A, [B]) -> A) = <intrinsic>
assert_equals({ 4 }, { evolve_while({$0 < 4}, 1, (+), [1,1,1,1,1]) });



// filter : (T => ([T], T -> Bool) -> [T]) = <intrinsic>
assert_equals({ [0,1] } , { filter([0,1,2,3], {$0 < 2}) });

// for : (X, Y => ([X], X -> Y) -> ()) = <intrinsic>
assert_equals({ 6 }, {
        for_data = box(0);
        for([1,2,3], { for_data <- {$$0 + $0} });
        *for_data
        });

// guard : (T => (Bool, T, () -> T) -> T) = <intrinsic>
assert_equals({ 0 }, {
        guard_data = box(0);
        guard(true, *guard_data, { guard_data <- inc; *guard_data });
        });
assert_equals({ 1 }, {
        guard_data = box(0);
        guard(false, *guard_data, { guard_data <- inc; *guard_data });
        });

// if : (T => (Bool, () -> T, () -> T) -> T) = <intrinsic>
assert_equals({ "true" }, { if(true, { "true" }, { "false" }) });
assert_equals({ "false" }, { if(false, { "true" }, { "false" }) });

// iif : (T => (Bool, T, T) -> T) = <intrinsic>
assert_equals({ "true" }, { iif(true, "true", "false") });
assert_equals({ "false" }, { iif(false, "true", "false") });

// pfor : (X, Y => ([X], X -> Y) -> ()) = <intrinsic>
assert_equals({ 6 }, {
        pfor_data = box(0);
        pfor([1,2,3], { pfor_data <- {$$0 + $0} });
        *pfor_data;
        });

// reduce : (A, B => ((A, B) -> A, A, [B]) -> A) = <intrinsic>
assert_equals({ 13 }, { reduce((+), 1, [2,4,6]) });

// scan : (A, B => ((A, B) -> A, A, [B]) -> [A]) = <intrinsic>
// same as reduce but provides list of (initial and) intermediate values
assert_equals({ [1, 3, 7, 13] }, { scan((+), 1, [2,4,6]) });

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

// where : (T => ([T], T -> Bool) -> [Int]) = <intrinsic>
assert_equals({ [0, 1, 3] }, { where([4,4,7,4,7], {$0 < 5}) });

// while : (T => (() -> Bool, () -> T) -> ()) = <intrinsic>
assert_equals({ 5 }, {
        while_data = box(0);
        while({ *while_data < 5 }, { while_data <- inc });
        *while_data;
        });


//////////////////////////////////////////////////
// list
//////////////////////////////////////////////////
// append : (T => ([T], T) -> [T]) = <intrinsic>
assert_equals({ [1,2,3,4] }, { append([1,2,3], 4) });

// count : Int -> [Int] = <intrinsic>
assert_equals({ [0,1,2] }, { count(3) });

// cut : (T => ([T], [Int]) -> [[T]]) = <intrinsic>
assert_equals({ [[1, 2], [3, 4], [5, 6], [7, 8], [9, 10]] }, { cut([1,2,3,4,5,6,7,8,9,10], [0,2,4,6,8]) });

// distinct : (T => [T] -> [T]) = <intrinsic>
assert_equals({ [1,2,3] }, { distinct([1,2,2,1,3]) });

// drop : (T => (Int, [T]) -> [T]) = <intrinsic>
assert_equals({ [4, 5] }, { drop(3, [1,2,3,4,5]) });

// find : (T => ([T], T) -> Int) = <intrinsic>
assert_equals({ 4 }, { find(["cat", "dog", "bird", "snake"], "fox") });
assert_equals({ 1 }, { find(["cat", "dog", "bird", "snake"], "dog") });

// first : (T => [T] -> T) = <intrinsic>
assert_equals({ "cat" }, { first(["cat", "dog", "bird", "dog", "snake"]) });

// flatten : (A => [[A]] -> [A]) = <intrinsic>
assert_equals({ [1,2,3,4,9,11] }, { flatten([[1,2,3,4], [9,11]]) });

// fromto : (Int, Int) -> [Int] = <intrinsic>
assert_equals({ [1, 2, 3] }, { fromto(1,3) });
assert_equals({ [-1, 0, 1, 2, 3] }, { fromto(-1,3) });
assert_equals({ [-1, -2, -3] }, { fromto(-1,-3) });
assert_equals({ [3, 2, 1] }, { fromto(3, 1) });


// group : (K, V => ([K], [V]) -> [K : [V]]) = <intrinsic>
assert_equals({ [1: ["a"], 2: ["b"], 3: ["c"], 4: ["d"]] }, { group([1,2,3,4], ["a", "b", "c", "d"]) });
assert_equals({ [1: ["rep1", "rep3"], 2: ["rep2", "rep4"]] }, { group([1,2], ["rep1", "rep2", "rep3", "rep4"]) });
assert_equals({ [1: ["a"], 2: ["b"]] }, { group([1,2,3,4], ["a", "b"]) });

// isindex : (T => (Int, [T]) -> Bool) = <intrinsic>
assert_true({ isindex(3, ["a", "b", "c", "d", "e"]) });
assert_false({ isindex(9, ["a", "b", "c", "d", "e"]) });
assert_false({ isindex(-2, ["a", "b", "c", "d", "e"]) });

// last : (T => [T] -> T) = <intrinsic>
assert_equals({ "e" }, { last(["a", "b", "c", "d", "e"]) });

// lplus : (T => ([T], [T]) -> [T]) = <intrinsic>
assert_equals({ [3, 5, 7, 2, 4, 6] }, { lplus([3,5,7], [2,4,6]) });

// listset : (T => ([T], Int, T) -> [T]) = <intrinsic>
assert_equals({ [3, 9, 7] }, { listset([3,5,7], 1, 9) });

// listsets : (T => ([T], [Int], [T]) -> [T]) = <intrinsic>
assert_equals({ [1, 2, 7, 1, 11] }, { listsets([3,5,7,9,11], [0,1,3], [1,2]) });

// range : (Int, Int) -> [Int] = <intrinsic>
assert_equals({ [1, 2, 3, 4] }, { range(1,4) });
assert_equals({ [-1, 0, 1, 2] }, { range(-1,4) });

// remove : (T => ([T], T) -> [T]) = <intrinsic>
assert_equals({ ["a", "c"] }, { remove(["a", "b", "c"], "b") });

// rep : (T => (Int, T) -> [T]) = <intrinsic>
assert_equals({ ["cat", "cat", "cat"] }, { rep(3, "cat") });

// rest : (T => [T] -> [T]) = <intrinsic>
assert_equals({ ["dog", "fish"] }, { rest(["cat", "dog", "fish"]) });


// shuffle : (T => [T] -> [T]) = <intrinsic>
// fake this a little, shuffle list will be the same lexical value as original list
assert_equals({ [1, 1, 1] }, { shuffle([1, 1, 1]) });

// size : (T => [T] -> Int) = <intrinsic>
assert_equals({ 3 }, { size(["a", "b", "c"]) });

// take : (T => (Int, [T]) -> [T]) = <intrinsic>
assert_equals({ ["a", "b"] }, { take(2, ["a", "b", "c"]) });
assert_equals({ ["b", "c"] }, { take(-2, ["a", "b", "c"]) });
assert_equals({ ["c"] }, { take(-1, ["a", "b", "c"]) });

// unique : (T => [T] -> [T]) = <intrinsic>
assert_equals({ [1, 2, 3] }, { unique([1,2,2,3,1,2]) });

// unzip : (Types.. => [Tup(Types)] -> Tup(List @ Types)) = <intrinsic>
assert_equals({ (["a", "b", "a", "b"], [1, 2, 3, 4], [true, false, true, true]) },
              { unzip([("a", 1, true), ("b", 2, false), ("a", 3, true), ("b", 4, true)]) });

// zip : (Types.. => Tup(List @ Types) -> [Tup(Types)]) = <intrinsic>
assert_equals({ [("a", 1), ("b", 2), ("a", 3), ("b", 4)] }, { zip(["a", "b"], [1,2,3,4]) });




//////////////////////////////////////////////////
// map
//////////////////////////////////////////////////
// assoc : (K, V => ([K], [V]) -> [K : V]) = <intrinsic>
assert_equals({ [1: "a", 2: "b", 3: "c", 4: "d"] }, { assoc([1,2,3,4], ["a", "b", "c", "d"]) });
assert_equals({ [1: "rep1", 2: "rep2", 3: "rep1", 4: "rep2"] }, { assoc([1,2,3,4], ["rep1", "rep2"]) });

// entries : (K, V => [K : V] -> [(K, V)]) = <intrinsic>
assert_true({ isperm([(#a, "A"), (#b, "B")], entries([#a:"A", #b:"B"])) });

// iskey : (K, V => ([K : V], K) -> Bool) = <intrinsic>
assert_true({ iskey([#a:"A", #b:"B"], #a) });
assert_false({ iskey([#a:"A", #b:"B"], #c) });

// keys : (K, V => [K : V] -> [K]) = <intrinsic>
assert_true({ isperm([#a, #b, #c], keys([#a:"A", #b:"B", #c:"C"])) });

// mapdel : (K, V => ([K : V], K) -> [K : V]) = <intrinsic>
assert_equals({ [#a: "A", #c: "C"] }, { mapdel([#a:"A", #b:"B", #c:"C"], #b) });

// mapset : (K, V => ([K : V], K, V) -> [K : V]) = <intrinsic>
assert_equals({ [#a: "A", #b:"B", #c: "C"] }, { mapset([#a:"A", #b:"B"], #c, "C") });

// mapsets : (K, V => ([K : V], [K], [V]) -> [K : V]) = <intrinsic>
assert_equals({ [#a: "A", #b:"B", #c: "C"] }, { mapsets([#a:"A", #b:"B"], [#c], ["C"]) });
assert_equals({ [#a: "A", #b:"B", #c: "C", #d: "D"] }, { mapsets([#a:"A", #b:"B"], [#c,#d], ["C","D"]) });
assert_equals({ [#a: "A", #b:"B", #c: "C"] }, { mapsets([#a:"A", #b:"B"], [#c], ["C","D"]) });
assert_equals({ [#a: "A", #b:"B", #c: "CD", #d: "CD"] }, { mapsets([#a:"A", #b:"B"], [#c,#d], ["CD"]) });

// mplus : (K, V => ([K : V], [K : V]) -> [K : V])
assert_equals({ [#c: "C", #b: "B", #a: "A"] }, { mplus([#a:"A", #b:"B"], [#c:"C"]) });
assert_equals({ [#b: "b", #a: "A"] }, { mplus([#a:"A", #b:"B"], [#b:"b"]) });

// values : (K, V => [K : V] -> [V]) = <intrinsic>
assert_true({ isperm(["A", "B", "C"], values([#a:"A", #b:"B", #c: "C"])) });



//////////////////////////////////////////////////
// misc poly
//////////////////////////////////////////////////
// empty : (T => T -> Bool) = <intrinsic>
assert_equals({ true }, { empty([]) });
assert_equals({ true }, { empty([:]) });
assert_equals({ true }, { empty(()) });
assert_equals({ false }, { empty([1,2]) });
assert_equals({ false }, { empty([1:1]) });
assert_equals({ false }, { empty((1,1)) });

// hash : (T => T -> Int) = <intrinsic>
assert_equals({ 1 }, { hash(1) });
assert_equals({ 114801 }, { hash("the") });

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

//////////////////////////////////////////////////
// system
//////////////////////////////////////////////////
// assert : (Bool, String) -> () = <intrinsic>
assert(true, "no error");

// availprocs : () -> Int = <intrinsic>
assert_equals({ true }, { ge(availprocs(), 1) });

// millitime : () -> Long = <intrinsic>
assert_equals({ true }, { fge(l2f(millitime()), 1.0) });

// nanotime : () -> Long = <intrinsic>
assert_equals({ true }, { fge(l2f(nanotime()), 1.0) });

// print : (T => T -> ()) = <intrinsic>
// printstr : String -> () = <intrinsic>


//////////////////////////////////////////////////
// logging
//////////////////////////////////////////////////
// logdebug : (T => T -> ()) = <intrinsic>
// logerror : (T => T -> ()) = <intrinsic>
// loginfo : (T => T -> ()) = <intrinsic>
// logwarning : (T => T -> ()) = <intrinsic>


//////////////////////////////////////////////////
// threads
//////////////////////////////////////////////////
// future : (X => (() -> X) -> (() -> X)) = <intrinsic>

// pmap : (X, Y => ([X], X -> Y) -> [Y]) = <intrinsic>
assert_equals({ [2,3,4] }, { pmap([1,2,3], inc) });
assert_equals({ [2,3,4] }, { [1,2,3] |: inc });

// sleep : Int -> () = <intrinsic>
assert_equals({ true }, { sleep(3); true });

// spawn : (T => (() -> T) -> ()) = <intrinsic>
assert_equals({ 5 }, {
                    spawndata = box(0);
                    spawn { while({ *spawndata < 5 }, { sleep(100); spawndata <- inc }); };
                    await(spawndata, { $0 == 5 });
                    *spawndata;
                    });

// taskid : () -> Long = <intrinsic>
assert_equals({ 1L }, { taskid() });


//////////////////////////////////////////////////
// trans/boxes
//////////////////////////////////////////////////
// await : (T => (*T, T -> Bool) -> ()) = <intrinsic>
assert_equals({ 5 }, {
                    awaitdata = box(0);
                    spawn { while({ *awaitdata < 5 }, { sleep(100); awaitdata <- inc }); };
                    await(awaitdata, { $0 == 5 });
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

// box : (T => T -> *T) = <intrinsic>
assert_equals({ *box(2) }, { *box(2) });

// do : (T => (() -> T) -> T) = <intrinsic>
assert_equals({ true }, { do { intran(); }; });

// get : (T => *T -> T) = <intrinsic>
assert_equals({ 12 }, { a = box(12); get(a) });
assert_equals({ 12 }, { a = box(12); *a });

// gets : (Vals.. => Tup(Box @ Vals) -> Tup(Vals)) = <intrinsic>
assert_equals({ (12, 3) }, { a = box(12); b = box(3); gets((a, b)) });
assert_equals({ (12, 3) }, { a = box(12); b = box(3); **(a, b) });

// intran : () -> Bool = <intrinsic>
assert_equals({ true }, { do { intran(); }; });
assert_equals({ false }, { intran(); });

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

// snap : (T => *T -> T) = <intrinsic>
assert_equals({ 1 }, {
                      data1 = box(1);
                      x = snap(data1);
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

// transfer : (B, A => (*B, A -> B, *A) -> ()) = <intrinsic>
assert_equals({ 2 }, {
                    source = box(1);
                    target = box(9);
                    transfer(target, inc, source);
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

// unwatch : (T, X => (*T, (T, T) -> X) -> *T) = <intrinsic>
assert_equals({ 2 }, {
                    status = box(0);
                    stat(old, new) { put(status, new) };
                    f = box(0);
                    w = watch(f, stat);
                    f <- inc;
                    f <- inc;
                    unwatch(f, w);
                    f <- inc;
                    *status;
                });

// update : (T => (*T, T -> T) -> ()) = <intrinsic>
assert_equals({ 1 }, { data = box(0); update(data, inc); *data; });
assert_equals({ 1 }, { data = box(0); data <- inc; *data; });

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

// watch : (T, X => (*T, (T, T) -> X) -> ((T, T) -> X)) = <intrinsic>
assert_equals({ 2 }, {
                    status = box(0);
                    stat(old, new) { put(status, new) };
                    f = box(0);
                    w = watch(f, stat);
                    f <- inc;
                    f <- inc;
                    *status;
                });



// DEMO SUPPORT

//////////////////////////////////////////////////
// processing
//////////////////////////////////////////////////
// prarc : (Double, Double, Double, Double, Double, Double) -> () = <intrinsic>
// prbackground : Int -> () = <intrinsic>
// prbeginshape : () -> () = <intrinsic>
// prbeginshapemode : Symbol -> () = <intrinsic>
// prclose : () -> () = <intrinsic>
// prcolor : (Int, Int, Int, Int) -> Int = <intrinsic>
// prcreatefont : (String, Int) -> Font = <intrinsic>
// prcurvevertex : (Double, Double) -> () = <intrinsic>
// prdirectionallight : (Double, Double, Double, Double, Double, Double) -> () = <intrinsic>
// prellipse : (Double, Double, Double, Double) -> () = <intrinsic>
// prendshape : () -> () = <intrinsic>
// prfill : Int -> () = <intrinsic>
// prfillrgba : (Int, Int, Int, Int) -> () = <intrinsic>
// primage : (Image, Double, Double, Double, Double) -> () = <intrinsic>
// primageheight : Image -> Int = <intrinsic>
// primagewidth : Image -> Int = <intrinsic>
// prisopen : () -> Bool = <intrinsic>
// prkey : () -> String = <intrinsic>
// prkeycode : () -> Int = <intrinsic>
// prlights : () -> () = <intrinsic>
// prline3d : (Double, Double, Double, Double, Double, Double) -> () = <intrinsic>
// prloadfont : String -> Font = <intrinsic>
// prloadimage : String -> Image = <intrinsic>
// prloop : Bool -> () = <intrinsic>
// prmatmult : (Matrix3D, Double, Double, Double) -> (Double, Double, Double) = <intrinsic>
// prmatrotatex : (Matrix3D, Double) -> Matrix3D = <intrinsic>
// prmatrotatey : (Matrix3D, Double) -> Matrix3D = <intrinsic>
// prmatrotatez : (Matrix3D, Double) -> Matrix3D = <intrinsic>
// prmattranslate : (Matrix3D, Double, Double, Double) -> Matrix3D = <intrinsic>
// prmouse : () -> (Int, Int) = <intrinsic>
// prmousebutton : () -> Symbol = <intrinsic>
// prmousepressed : () -> Bool = <intrinsic>
// prnostroke : () -> () = <intrinsic>
// propen : (String, [Symbol : () -> ()]) -> () = <intrinsic>
// prpmatrix3d : () -> Matrix3D = <intrinsic>
// prpmouse : () -> (Int, Int) = <intrinsic>
// prrect : (Double, Double, Double, Double) -> () = <intrinsic>
// prredraw : () -> () = <intrinsic>
// prrotatex : Double -> () = <intrinsic>
// prrotatey : Double -> () = <intrinsic>
// prrotatez : Double -> () = <intrinsic>
// prscreenheight : () -> Int = <intrinsic>
// prscreenwidth : () -> Int = <intrinsic>
// prsize : (Int, Int) -> () = <intrinsic>
// prsizemode : (Int, Int, Symbol) -> () = <intrinsic>
// prsmooth : () -> () = <intrinsic>
// prstroke : (Int, Int, Int) -> () = <intrinsic>
// prstrokeweight : Int -> () = <intrinsic>
// prtext : (String, Double, Double) -> () = <intrinsic>
// prtextfont : Font -> () = <intrinsic>
// prtextwidth : String -> Double = <intrinsic>
// prtranslate : (Double, Double) -> () = <intrinsic>
// prtriangle : (Double, Double, Double, Double, Double, Double) -> () = <intrinsic>
// prvertex : (Double, Double) -> () = <intrinsic>
// prvertex3d : (Double, Double, Double) -> () = <intrinsic>





//////////////////////////////////////////////////
// socket
//////////////////////////////////////////////////
// accept : (ServerSocket, String -> String) -> () = <intrinsic>
// close : ServerSocket -> () = <intrinsic>
// closed : ServerSocket -> Bool = <intrinsic>
// ssocket : Int -> ServerSocket = <intrinsic>


//////////////////////////////////////////////////
// file
//////////////////////////////////////////////////
// appendfile : (String, String) -> Bool = <intrinsic>
// readfile : String -> String = <intrinsic>
// writefile : (String, String) -> Bool = <intrinsic>


//////////////////////////////////////////////////
// misc
//////////////////////////////////////////////////
// hsb2rgb : (Double, Double, Double) -> Int = <intrinsic>
// httpget : String -> String = <intrinsic>
// httphead : String -> ?(true: [String], false: String) = <intrinsic>
// parsexml : String -> XNode = <intrinsic>
// rgb2hsb : Int -> (Double, Double, Double) = <intrinsic>



// TEST

//////////////////////////////////////////////////
// foreign interface
//////////////////////////////////////////////////
myArray = array(4, 0);
// TODO: public static final ALen _alen = new ALen();
// TODO: javassist.CannotCompileException
// alen(myArray);
assert_equals({ 0 }, { aget(myArray, 0) });
aset(myArray, 0, 1);
assert_equals({ 1 }, { aget(myArray, 0) });

// public static final ASet _aset = new ASet();



