// fibonacci

import std;

// recurse
fib0(n) {
    guard(n < 2, n, { fib0(n - 1) + fib0(n - 2) })
};

// iterate
fib1(n) {
    cyclen(n, (0, 1), { ($1, $0 + $1) }).0
};

n = 25;

print(fib0(n), fib1(n));
