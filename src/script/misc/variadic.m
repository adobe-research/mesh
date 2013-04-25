
import * from std;

// test variadic tuple calls using intrinsic
// zip(lists:Tup(Each(List, <Members>)) . List(Tup(<Members>))) { ... }

// should give same sig as zip
z(ls) { zip(ls) };

// annotated version
<Ts:[*]> za(ls : Tup(Ts | List)) -> [Tup(Ts)] { zip(ls) };

// implicit zip2
test1a(x, y) { zip(x, y) };

// explicit params zip2
<A, B> test1b(x:[A], y:[B]) { zip(x, y) };

// explicit params and result type zip2
<A, B> test1c(x:[A], y:[B]) -> [(A, B)] { zip(x, y) };

// implicit zip3 with tuple member order reversed
test1d(x, y, z) { zip(z, y, x) };

