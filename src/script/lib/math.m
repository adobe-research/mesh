//
// not-quite-core math and float point functions
// TODO refactor once we have interfaces
//

import i2f from integer;
import * from list;
import reduce, scan from loop;

/**
 * Wraps {@link Math#atan2(double, double)}.
 * @param y the ordinate coordinate
 * @param x the abscissa coordinate
 * @return 
 */
intrinsic atan2(y:Double, x:Double) -> Double;

/**
 * Wraps {@link Math#cos(double)}.
 * @param x the angle
 * @return cosine of the angle
 */
intrinsic cos(x:Double) -> Double;

/**
 * Generate a list of n random ints in the range 0..max - 1
 * @param x Size of list to return
 * @param y Upper limit of random value minus 1
 * @return List of n random ints in the range 0..max - 1
 */
intrinsic draw(x:Int, y:Int) -> [Int];

/**
 * {@link Math#exp(double)}
 * @param x the exponent to raise e to
 * @return Euler's number number raised to the power of x.
 */
intrinsic exp(x:Double) -> Double;

/**
 * Convert double value to int
 * @param x double value
 * @return take floor of double, return int
 */
intrinsic f2i(x:Double) -> Int;

/**
 * Convert float to string value.
 * @param x double value
 * @return String representation of double.
 */
intrinsic f2s(x:Double) -> String;

/**
 * {@link Math#random()}
 * @return float value greater than or equal to 0.0 and less than 1.0.
 */
intrinsic frand() -> Double;

/**
 * Natural log
 * @param x
 * @return the natural logarithm of x
 */
intrinsic ln(x:Double) -> Double;

/**
 * Generate a random integer between 0 and x - 1
 * @param x max value
 * @return An integer between 0 and x - 1
 */
intrinsic rand(x:Int) -> Int;

/**
 * Wraps {@link Math#sin(double)}.
 * @param x the angle
 * @return sine of the angle
 */
intrinsic sin(x:Double) -> Double;

/**
 * {@link Math#sqrt(double)}
 * @param x value
 * @return square root of value.
 */
intrinsic sqrt(x:Double) -> Double;

/**
 * Wraps {@link Math#tan(double)}.
 * @param x angle
 * @return tangent of angle x
 */
intrinsic tan(x:Double) -> Double;

/**
 * @param n int number.
 * @return absolute value of n.
 */
abs(n) { guard(n >= 0, n, {-n}) };

/**
 * @param f float number.
 * @return float absolute value of f.
 */
fabs(f) { guard(f >=. 0.0, f, {0.0 -. f}) };

/**
 * sign of float.
 * @param f float number.
 * @return 1 for all positive values larger than 0.0,
 * -1 for all negative values smaller than 0.0
 * 0 for 0.0 and -0.0
 */
fsign(f) { guard(f >. 0.0, 1, {iif(f <. 0.0, -1, 0)}) };

/**
 * 
 * @param x float number
 * @param y float number
 * @return minimum of x,y
 */
fmin(x,y) { iif(x >=. y, y, x) };

/**
 * @param x float number
 * @param y float number
 * @return maximum of x,y
 */
fmax(x,y) { iif(x >=. y, x, y) };

/**
 * @code
 * inrange(4, 2, 5) // (4 >= 2) && (4 < (2+5)) -> true 
 * @endcode
 * @param x int number
 * @param base int base of range
 * @param extent int extent of range
 * @return x within range [base, base + extent).
 */
inrange(x, base, extent) { x >= base && { x < base + extent } };

/**
 * @code
 * finrange(4.0, 2.0, 5.0) // (4.0 >= 2.0) && (4.0 < (2.0+5.0)) -> true 
 * @endcode
 * @param f float number
 * @param fbase float base of range
 * @param fextent float extent of range
 * @return float f within range [base, base + extent).
 */
finrange(f, fbase, fextent) { f >=. fbase && { f <. fbase + fextent } };

/**
 * (/) with divide-by-zero guard.
 * 
 * @param n int number
 * @param d int divisor
 * @return n/d except when d equals 0 when it returns 0
 */
divz(n,d) { guard(d == 0, 0, {n / d}) };

/**
 * float (/) with divide-by-zero guard.
 * 
 * @param n float number
 * @param d float divisor
 * @return n/d except when d equals 0.0 when it returns 0.0
 */
fdivz(n,d) { guard(d == 0.0, 0.0, {n /. d}) };

/**
 * (%) with divide-by-zero guard.
 * 
 * @param n int number
 * @param d int divisor
 * @return n % d except when d equals 0 when it returns 0
 */
modz(n,d) { guard(d == 0, 0, {n % d}) };

/**
 *   float (%) with divide-by-zero guard.
 * 
 * @param n float number
 * @param d float divisor
 * @return n % d except when d equals 0.0 when it returns 0.0
 */
fmodz(n,d) { guard(d == 0.0, 0.0, {n %. d}) };

/**
 * round.
 * 
 * @param f float number
 * @return f rounded to nearest int.
 */
round(f) { f2i(f + 0.5) };

/**
 * @param i int 
 * @return int i * i
 */
sq(i) { i * i };

/**
 * @param f float number
 * @return float f * f
 */
fsq(f) { f *. f };

//
// log base b
// TODO uncomment when rebuilt
// log(b, n) { ln(n) /. ln(b) };

/**
 * convert a vector of digits in a given radix to an integer.
 * 
 * TODO i2vec, need shift ops.
 * 
 * @param vec vector of digits
 * @param radix
 * @return Int value representation of vector in specified radix
 * @code vec2i([1, 0, 2, 4]) // returns 1024 @endcode
 */
vec2i(vec, radix)
{
    sum(zip(vec, reverse(index(vec))) | { d, p => d * (radix ^ p) })
};

/**
 * Sum of a list of integers. E.g.,
 * <code>sum([1, 2, 3]) == 6</code>
 * @param is list of integers
 * @return sum of list elements
 */
sum(is : [Int]) -> Int
{
    reduce((+), 0, is)
};

/**
 * Sum a list of doubles. E.g.,
 * <code>fsum([1.0, 2.0, 3.0]) == 6.0</code>
 * @param fs list of doubles
 * @return sum of list elements
 */
fsum(fs : [Double]) -> Double
{
    reduce((+.), 0.0, fs)
};

/**
 * Running total of a list of integers. E.g.,
 * <code>runtot([1, 2, 3]) == [1, 3, 6]</code>
 * @param is list of integers
 * @return list of running total of input
 */
runtot(is : [Int]) -> [Int]
{
    drop(1, scan((+), 0, is))
};

/**
 * Running total of a list of doubles. E.g.,
 * <code>fruntot([1.0, 2.0, 3.0]) == [1.0, 3.0, 6.0]</code>
 * @param fs list of doubles
 * @return list of running total of input
 */
fruntot(fs : [Double]) -> [Double]
{
    drop(1, scan((+), 0.0, fs))
};

/**
 * Multiply a list of integers. E.g.,
 * <code>product([2, 3, 4]) == 24</code>
 * @param is list of integers
 * @return product of elements in list
 */
product(is : [Int]) -> Int
{
    reduce((*), 1, is)
};

/**
 * Multiply a list of doubles. E.g.,
 * <code>product([2.0, 3.0, 4.0]) == 24.0</code>
 * @param fs list of doubles
 * @return product of elements in list
 */
fproduct(fs : [Double]) -> Double
{
    reduce((*.), 1.0, fs)
};

/**
 * @param lst list of int
 * @return arith mean over list of ints, 0 for empty list.
 * @code avg([2,3,4]) //returns 3 @endcode
 */
avg(lst)
{
    divz(sum(lst), size(lst))
};

/**
 * @param lst list of float
 * @return arith mean over list of floats, 0 for empty list.
 * @code favg([2.0,3.0,4.0]) //returns 3.0 @endcode
 */
favg(lst)
{
    fdivz(fsum(lst), i2f(size(lst)))
};
