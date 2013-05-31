import * from std;
import * from unittest;

// Inliners
assert_true({ atan2(1.0, i2f(1)); true });
assert_true({ and(i2b(1), { i2b(1) }); true });
assert_true({ band(inc(3), 1+3); true });
assert_true({ bor(inc(3), 1+3); true });
assert_true({ bxor(inc(3), 1+3); true });
assert_true({ cos(i2f(1)); true });
assert_true({ div(10, inc(4)); true });
assert_true({ eq(i2f(1), i2f(1)); true });
assert_true({ f2i(plus(1.0, 1.1)); true });
assert_true({ fdiv(10.0, 1.0+3.0); true });
assert_true({ fge(10.0, 1.0+3.0); true });
assert_true({ fgt(10.0, 1.0+3.0); true });
assert_true({ fle(10.0, 1.0+3.0); true });
assert_true({ flt(10.0, 1.0+3.0); true });
assert_true({ fminus(10.0, 1.0+3.0); true });
assert_true({ fmod(10.0, 1.0+3.0); true });
assert_true({ fneg(1.0+3.0); true });
// ForInliner
assert_true({ for(index([1,2,3]), { $0 }); true }); // index for index arg
assert_true({ for(count(s2i("2")), inc); true });   // count with applyTerm
assert_true({ for(count(2), neg ); true });         // lambdaBody == null
assert_true({ fpow(10.0, 1.0+3.0); true });
assert_true({ ftimes(10.0, 1.0+3.0); true });
assert_true({ ge(4, 1+3); true });
assert_true({ gt(4, 1+3); true });
assert_true({ i2f(inc(0)); true });
assert_true({ if(i2b(1), {1}, {2}); true });
assert_true({ le(4, 1+3); true });
assert_true({ lt(4, 1+3); true });
assert_true({ ln(1.0+1.1); true });

// Map inliner
assert_equals({ [("a", 1), ("b", 2)] }, { zip(["a", "b"], [1,2]) | { a:(String, Int) => (a.0, a.1) } });
assert_equals({ [(1, 3, 5), (2, 4, 6)] }, { zip([1,2], [3,4], [5,6]) | { a:(Int, Int, Int) => (a.0, a.1, a.2) } });

assert_true({ max(inc(3), 1+3); true });
assert_true({ min(inc(3), 1+3); true });
assert_true({ minus(inc(3), 1+3); true });
assert_true({ mod(inc(3), 1+3); true });
assert_true({ ne(4, 1+3); true });
assert_true({ neg(1+3); true });
assert_true({ not(i2b(1)); true });
assert_true({ or(i2b(1), { i2b(1) }); true });
assert_true({ plus(4, 1+3); true });
assert_true({ pow(4, 1+3); true });
assert_true({ shiftl(inc(3), 1+3); true });
assert_true({ shiftr(inc(3), 1+3); true });
assert_true({ sin(i2f(1)); true });
assert_true({ sqrt(1.0+2.0); true });
assert_true({ tan(i2f(1)); true });
assert_true({ times(inc(3), 1+3); true });
assert_true({ ushiftr(inc(3), 1+3); true });
